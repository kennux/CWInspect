package net.kennux.cwinspect.packets.helpers;

import java.util.zip.Inflater;

public class ZLib
{
	private static byte[] decompress(byte[] data)
	{
		try
		{
			Inflater decompressor = new Inflater();
			byte[] decompressedBuffer = new byte[32768];
			
			decompressor.setInput(data);
			int decompressedLength = decompressor.inflate(decompressedBuffer);
			
			byte[] decompressed = new byte[decompressedLength];
			for(int i = 0; i < decompressedLength; i++)
			{
				decompressed[i] = decompressedBuffer[i];
			}
			
			return decompressed;
		}
		catch (Exception e)
		{
			return null;
		}
	}

}
