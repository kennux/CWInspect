package net.kennux.cwinspect.packets.helpers;

import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ZLib
{
	public static byte[] decompress(byte[] data, int buffer)
	{
		try
		{
			Inflater decompressor = new Inflater();
			byte[] decompressedBuffer = new byte[buffer];
			
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
	
	public static byte[] compress(byte[] data, int buffer)
	{
		try
		{
			Deflater compressor = new Deflater();
			byte[] compressedBuffer = new byte[buffer];
			
			compressor.setInput(data);
			int decompressedLength = compressor.deflate(compressedBuffer);
			
			byte[] compressed = new byte[decompressedLength];
			for(int i = 0; i < decompressedLength; i++)
			{
				compressed[i] = compressedBuffer[i];
			}
			
			return compressed;
		}
		catch (Exception e)
		{
			return null;
		}
	}

}
