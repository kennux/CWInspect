package net.kennux.cwinspect.hooks.client;

import java.io.OutputStream;

import net.kennux.cwinspect.hooks.AHook;

public class HandshakeHook extends AHook
{

	public int getPacketId()
	{
		return 17;
	}

	public void hook(byte[] data, OutputStream clientStream, OutputStream serverStream)
	{
		// Send server full message
		byte[] bytes = new byte[4];
		bytes[0] = 18;
		bytes[1] = 0;
		bytes[2] = 0;
		bytes[3] = 0;
		
		/* This hook was only written for testings!
		try
		{
			clientStream.write(bytes);
		}
		catch (Exception e)
		{
			
		}*/
		
		// A hook HAS TO forward the data by itself
		try
		{
			serverStream.write(data);
		}
		catch (Exception e)
		{
			
		}
	}

}
