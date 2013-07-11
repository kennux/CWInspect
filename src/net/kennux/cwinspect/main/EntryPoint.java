package net.kennux.cwinspect.main;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Properties;

import net.kennux.cwinspect.networking.SocketProxy;
import net.kennux.cwinspect.packets.helpers.ByteUtils;

public class EntryPoint {

	public static Properties configuration;
	private static SocketProxy socketProxy;
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.println("Starting CWInspect...");
		System.out.println("Loading Config...");
		
		// Load Config
		configuration = new Properties();
		try
		{
			configuration.load(new FileInputStream("cwinspect.properties"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}

		System.out.println("Config loaded!");
		System.out.println("Starting Server!");
		
		try
		{
			socketProxy = new SocketProxy(configuration.getProperty("remote_host"), Integer.parseInt(configuration.getProperty("remote_port")), Integer.parseInt(configuration.getProperty("local_port")));
			socketProxy.startSocket();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Failed to start server!");
			return;
		}
		System.out.println("Server running!");
		
		
		// Change direction of system.out if user wants to
		if ( ! configuration.getProperty("log_to_file").isEmpty())
		{
			System.out.println("Starting logging to: " + configuration.getProperty("log_to_file"));
			try
			{
				System.setOut(new PrintStream(new FileOutputStream(configuration.getProperty("log_to_file"), true)));
			}
			catch (Exception e)
			{
				
			}
		}
	}

	private static String byteToHex(byte[] bytes)
	{
		StringBuilder sb = new StringBuilder();
	    for (byte b : bytes)
	    {
	        sb.append(String.format("%02X ", b));
	    }
	    
	    return sb.toString();
	}

}
