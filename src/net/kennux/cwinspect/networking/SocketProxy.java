package net.kennux.cwinspect.networking;

import java.io.IOException;
import java.net.*;
import java.io.*;
import java.util.zip.*;

import net.kennux.cwinspect.packets.APacket;
import net.kennux.cwinspect.packets.IHook;

public class SocketProxy {
	
	// Networking (Sockets)
	private ServerSocket listenerSocket;
	
	// Thread for sockets
	private Thread clientThread, cwThread;
	
	// Information for sockets
	private String remoteHost;
	private int remotePort, localPort;
	
	private SocketThread clientThreadInstance;
	
	class CWThread implements Runnable
	{
		private Socket clientSocket, cwSocket;
		private InputStream inputStream;
		private OutputStream outputStream;
		
		public CWThread(Socket clientSocket, Socket cwSocket, InputStream inputStream, OutputStream outputStream)
		{
			this.inputStream = inputStream;
			this.outputStream = outputStream;
			this.clientSocket = clientSocket;
			this.cwSocket = cwSocket;
		}
		
		public void run()
		{
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
					
					// Identify packet
					APacket packet = PacketIdentifier.identifyServerPacket((int) data[0]);
					packet.loadPacket(data);
					
					// Hooks
					IHook hook = HookIdentifier.identifyServerHook((int) data[0]);
					if (hook != null)
					{
						this.outputStream.write(hook.hook(data));
					}
					
					// Output
					System.out.print("Server -> Client [" + new java.util.Date().toLocaleString() + "]\r\n" + packet);
					
					// Send to client
					this.clientSocket.getOutputStream().write(data);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
	}
	
	// Socket thread
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

		public void run()
		{
			while (this.isRunning)
			{
				try
				{
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
							
							// Identify packet
							APacket packet = PacketIdentifier.identifyClientPacket((int) data[0]);
							packet.loadPacket(data);

							// Hooks
							IHook hook = HookIdentifier.identifyClientHook((int) data[0]);
							if (hook != null)
							{
								this.remoteOutputStream.write(hook.hook(data));
							}
							
							// Output
							System.out.print("Client -> Server [" + new java.util.Date().toLocaleString() + "]\r\n" + packet);
							
							// Send to server
							this.cwOutputStream.write(data);
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
		
		private void connectCW()
		{
			try
			{
				// Load Cube Worlds Socket, thread and streams
				this.cwSocket = new Socket(this.remoteHost, this.remotePort);
				this.cwInputStream = this.cwSocket.getInputStream();
				this.cwOutputStream = this.cwSocket.getOutputStream();
				this.cwThreadInstance = new CWThread(this.remoteSocket, this.cwSocket, this.cwInputStream, this.cwOutputStream);
				this.cwThread = new Thread(this.cwThreadInstance);
				this.cwThread.start();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
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
	}
	
	
	public SocketProxy(String remoteHost, int remotePort, int localPort) throws IOException
	{
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.localPort = localPort;
	}
	
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
	
	public void stop()
	{
		this.clientThreadInstance.stop();
	}
}
