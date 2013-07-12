package net.kennux.cwinspect.networking;

import java.io.IOException;
import java.net.*;
import java.io.*;
import java.util.Date;

import net.kennux.cwinspect.hooks.AHook;
import net.kennux.cwinspect.main.EntryPoint;
import net.kennux.cwinspect.packets.APacket;
import net.kennux.cwinspect.packets.helpers.ByteUtils;

public class SocketProxy
{
	// Thread for sockets
	private Thread clientThread;
	
	// Information for sockets
	private String remoteHost;
	private int remotePort, localPort;
	
	private SocketThread clientThreadInstance;
	
	/**
	 * Cube World socket thread.
	 * Used for logging data and proxy them to client
	 * @author KennuX
	 *
	 */
	class CWThread implements Runnable
	{
		private Socket clientSocket, cwSocket;
		private InputStream inputStream;
		@SuppressWarnings("unused")
		private OutputStream outputStream;
		
		public CWThread(Socket clientSocket, Socket cwSocket, InputStream inputStream, OutputStream outputStream)
		{
			this.inputStream = inputStream;
			this.outputStream = outputStream;
			this.clientSocket = clientSocket;
			this.cwSocket = cwSocket;
		}
		
		@SuppressWarnings("deprecation")
		public void run()
		{
			// Get server logpath
			String serverLogPath = EntryPoint.configuration.getProperty("log_binary_server");
			String timestampLog = EntryPoint.configuration.getProperty("log_timestamps");
			String unknownLogFile = EntryPoint.configuration.getProperty("log_unknown");
			FileOutputStream logOut = null;
			try
			{
				logOut = new FileOutputStream(serverLogPath, true);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			while (this.cwSocket.isConnected())
			{
				// 16K Buffer
				byte[] buffer = new byte[16384];
				
				try
				{
					int packetLength = this.inputStream.read(buffer);
					
					// Read data
					byte[] data;
					if (packetLength > 0)
					{
						data = new byte[packetLength];
						
						for(int i = 0; i < packetLength; i++)
						{
							data[i] = buffer[i];
						}
					}
					else
					{
						continue;
					}
					
					// We got data!
					this.gotData(data, packetLength, logOut, timestampLog, unknownLogFile, serverLogPath);
				}
				catch (Exception e)
				{
					try
					{
						this.cwSocket.close();
					}
					catch (Exception ex)
					{
						// ex.printStackTrace();
					}
					
					// e.printStackTrace();
					return;
				}
			}

			try
			{
				logOut.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		/**
		 * Interprets data, loads packet classes or fires hooks.
		 * It also does logging and forwarding data.
		 * @param data
		 * @param packetLength
		 * @param logOut
		 * @param timestampLog
		 * @param unknownLogFile
		 * @param serverLogPath
		 * @throws Exception
		 */
		private void gotData(byte[] data, int packetLength, FileOutputStream logOut, String timestampLog, String unknownLogFile, String serverLogPath) throws Exception
		{
			byte[] dataStillAvail = null;
			// Get packet id (int32)
			int packetId = ByteUtils.readInt32(data, 0);
			
			// Identify packet
			APacket packet = PacketIdentifier.identifyServerPacket(packetId);
			int packetLen = packet.loadPacket(data);
			
			// Is packet len known?
			if (packetLen > 0)
			{
				// Set data still available
				dataStillAvail = new byte[data.length-packetLen];
				dataStillAvail = ByteUtils.getBytesRange(data, packetLen, packetLength-packetLen);
				data = ByteUtils.getBytesRange(data, 0, packetLen);
			}
			
			// Hooks
			AHook hook = HookIdentifier.identifyServerHook(packetId);
			if (hook != null)
			{
				hook.hook(data, this.clientSocket.getOutputStream(), this.cwSocket.getOutputStream());
				System.out.println("Hooked " + hook.getClass().getSimpleName() + " - Information:\r\n" + hook);
			}
			
			// If packet is unknown check for logfile
			if ( ! unknownLogFile.equals("") && packet.getPacketId() < 0)
			{
				// Open uknown logfile
				BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(unknownLogFile, true));
				bufferWriter.append("Server -> Client [" + new java.util.Date().toLocaleString() + "] [" + (System.currentTimeMillis()  % 1000) + "]\r\n" + packet);
				bufferWriter.flush();
				bufferWriter.close();
			}
			
			// Output
			System.out.print("Server -> Client [" + new java.util.Date().toLocaleString() + "] [" + (System.currentTimeMillis()  % 1000) + "]\r\n" + packet);
			
			// Should i log it binary?
			if ( ! serverLogPath.equals("") && logOut != null)
			{
				// Open logfile
				long beforeSize = new File(serverLogPath).length();
				logOut.write(data);
				logOut.flush();
				long afterSize = new File(serverLogPath).length();
				
				
				// Open timestamp logfile
				BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(timestampLog, true));
				bufferWriter.append("Server -> Client [" + new Date().toLocaleString() + "] [" + (System.currentTimeMillis()  % 1000) + "] 0x" + Long.toHexString(beforeSize).toUpperCase() +
						" - 0x" + Long.toHexString(afterSize).toUpperCase() + "\r\n");
				bufferWriter.flush();
				bufferWriter.close();
			}
			// Send to client
			this.clientSocket.getOutputStream().write(data);
			
			// Still data available?
			if (dataStillAvail != null)
			{
				// Call again
				this.gotData(dataStillAvail, packetLength, logOut, timestampLog, unknownLogFile, serverLogPath);
			}
		}
	}
	
	/**
	 * Local Socket thread, used to log data from client and send it to server
	 * @author KennuX
	 *
	 */
	class SocketThread implements Runnable
	{
		// Socketing
		private Socket remoteSocket;
		private Socket cwSocket; // Cubeworld Socket
		private ServerSocket listenerSocket;
		private boolean isRunning;
		
		// Remote socketing
		private String remoteHost;
		private int remotePort;
		
		// Streams
		private InputStream remoteInputStream;
		private OutputStream remoteOutputStream;
		private InputStream cwInputStream;
		private OutputStream cwOutputStream;
		
		private Thread cwThread;
		private CWThread cwThreadInstance;
		
		// Initialize the socket thread (set sockets)
		public SocketThread(ServerSocket listener, String remoteHost, int remotePort)
		{
			// Initialize variables
			this.listenerSocket = listener;
			this.remoteHost = remoteHost;
			this.remotePort = remotePort;
			this.isRunning = true;
		}

		@SuppressWarnings("deprecation")
		public void run()
		{
			while (this.isRunning)
			{
				try
				{
					// Get client logpath
					String clientLogPath = EntryPoint.configuration.getProperty("log_binary_client");
					String timestampLog = EntryPoint.configuration.getProperty("log_timestamps");
					String unknownLogFile = EntryPoint.configuration.getProperty("log_unknown");
					FileOutputStream logOut = null;
					try
					{
						logOut = new FileOutputStream(clientLogPath, true);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					
					// Accept socket
					this.remoteSocket = this.listenerSocket.accept();

					// Get Streams
					this.remoteInputStream = this.remoteSocket.getInputStream();
					this.remoteOutputStream = this.remoteSocket.getOutputStream();
					
					// Connect Cube World socket
					this.connectCW();
					// this.remoteSocket.setSoTimeout(2000);
					
					boolean running = true;
					
					// Loop until socket disconnected
					while (this.remoteSocket.isConnected() && running)
					{
						// 16K Buffer
						byte[] buffer = new byte[16384];
						
						try
						{
							int packetLength = this.remoteInputStream.read(buffer);
							
							// Read data if there is any data
							byte[] data;
							if (packetLength > 0)
							{
								data = new byte[packetLength];
								
								for(int i = 0; i < packetLength; i++)
								{
									data[i] = buffer[i];
								}
							}
							else
							{
								continue;
							}
							
							// Interpret data
							this.gotData(data, packetLength, logOut, timestampLog, unknownLogFile, clientLogPath);
						}
						catch (SocketException e)
						{
							running = false;
						}
					}
					
					// Stop/Close all resources
					this.cwThread.stop();
					this.cwSocket.close();
					this.remoteSocket.close();
					this.remoteInputStream.close();
					this.remoteOutputStream.close();
					this.cwInputStream.close();
					this.cwOutputStream.close();
					
					System.out.println("Connection dropped!");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * Connects the cubeworlds socket to the cube worlds server and starts its thread
		 */
		@SuppressWarnings("deprecation")
		private void connectCW()
		{
			try
			{
				// Load Cube Worlds Socket, thread and streams
				this.cwSocket = new Socket(this.remoteHost, this.remotePort);
				this.cwInputStream = this.cwSocket.getInputStream();
				this.cwOutputStream = this.cwSocket.getOutputStream();
				this.cwThreadInstance = new CWThread(this.remoteSocket, this.cwSocket, this.cwInputStream, this.cwOutputStream);
				
				// Stop thread if it is still running!
				if (this.cwThread != null)
				{
					this.cwThread.stop();
				}
				
				this.cwThread = new Thread(this.cwThreadInstance);
				this.cwThread.start();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		/**
		 * Stop proxy
		 */
		@SuppressWarnings("deprecation")
		public void stop()
		{
			try
			{
				this.isRunning = false;
				this.cwThread.stop();
				this.cwSocket.close();
				this.remoteSocket.close();
			}
			catch (Exception e)
			{
				
			}
		}
		
		/**
		 * Interprets data, loads packet classes or fires hooks.
		 * It also does logging and forwarding data.
		 */
		private void gotData(byte[] data, int packetLength, FileOutputStream logOut, String timestampLog, String unknownLogFile, String clientLogPath) throws Exception
		{
			// Get packet id (int32)
			int packetId = ByteUtils.readInt32(data, 0);
			
			// Identify packet
			APacket packet = PacketIdentifier.identifyClientPacket(packetId);
			packet.loadPacket(data);

			// If packet is unknown check for logfile
			if ( ! unknownLogFile.equals("") && packet.getPacketId() < 0)
			{
				// Open uknown logfile
				BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(unknownLogFile, true));
				bufferWriter.append("Server -> Client [" + new java.util.Date().toLocaleString() + "] [" + (System.currentTimeMillis()  % 1000) + "]\r\n" + packet);
				bufferWriter.flush();
				bufferWriter.close();
			}
			
			// Hooks
			AHook hook = HookIdentifier.identifyClientHook(packetId);
			if (hook != null)
			{
				hook.hook(data, this.remoteSocket.getOutputStream(), this.cwSocket.getOutputStream());
			}
			
			// Output
			System.out.print("Client -> Server [" + new java.util.Date().toLocaleString() + "] [" + (System.currentTimeMillis()  % 1000) + "]\r\n" + packet);
			
			// Should i log it binary?
			if ( ! clientLogPath.equals("") && logOut != null)
			{
				// Open logfile
				long beforeSize = new File(clientLogPath).length();
				logOut.write(data);
				logOut.flush();
				long afterSize = new File(clientLogPath).length();
				
				// Open timestamp logfile
				BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(timestampLog, true));
				bufferWriter.append("Client -> Server [" + new Date().toLocaleString() + "] [" + (System.currentTimeMillis() % 1000) + "] 0x" + Long.toHexString(beforeSize).toUpperCase() +
						" - 0x" + Long.toHexString(afterSize).toUpperCase() + "\r\n");
				bufferWriter.flush();
				bufferWriter.close();
			}
			
			// Send to server
			this.cwOutputStream.write(data);
		}
	}
	
	
	/**
	 * Initialize the socket proxy
	 * @param remoteHost
	 * @param remotePort
	 * @param localPort
	 * @throws IOException
	 */
	public SocketProxy(String remoteHost, int remotePort, int localPort) throws IOException
	{
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.localPort = localPort;
	}
	
	/**
	 * Starts the listeining socket and its thread
	 */
	public void startSocket()
	{
		try
		{
			this.clientThreadInstance = new SocketThread(new ServerSocket(this.localPort), this.remoteHost, this.remotePort);
			this.clientThread = new Thread(this.clientThreadInstance);
			this.clientThread.start();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Stops the threads
	 */
	@SuppressWarnings("deprecation")
	public void stop()
	{
		this.clientThreadInstance.stop();
		this.clientThread.stop();
	}
}
