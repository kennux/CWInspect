package net.kennux.cwinspect.packets.helpers;

import java.io.UnsupportedEncodingException;

public class ByteUtils
{
	/**
	 * Reads 32 bit integer at the given offset
	 * @param data
	 * @param offset
	 * @return
	 */
	public static int readInt32(byte[] data, int offset)
	{
		return   (int) (data[offset] |
	            (data[offset+1]) << 8 |
	            (data[offset+2]) << 16 |
	            (data[offset+3]) << 24);
	}
	
	/**
	 * Reads a 64 bit long at the given offset
	 * @param data
	 * @param offset
	 * @return
	 */
	public static long readLong64(byte[] data, int offset)
	{
		return   (long) (data[offset] |
	            (data[offset+1]) << 8 |
	            (data[offset+2]) << 16 |
	            (data[offset+3]) << 24 |
	            (data[offset+4]) << 32 |
	            (data[offset+5]) << 40 |
	            (data[offset+6]) << 48 |
	            (data[offset+7]) << 56);
	}

	/**
	 * Reads string with from the given range.
	 * Will return null in case of any error
	 * @param data
	 * @param offset
	 * @param len
	 * @param charset
	 * @return
	 */
	public static String readString(byte[] data, int offset, int len, String charset)
	{
		try
		{
			return new String(getBytesRange(data, offset, len), charset);
		}
		catch (UnsupportedEncodingException e)
		{
			return null;
		}
	}
	
	/**
	 * Reads string with the given range with charset UTF-16LE
	 * Will return null in case of any error
	 * @param data
	 * @param offset
	 * @param len
	 * @param charset
	 * @return
	 */
	public static String readString(byte[] data, int offset, int len)
	{
		return readString(data, offset, len, "UTF-16LE");
	}
	
	/**
	 * Returns bytes from the given string with the given charset
	 * Will return null in case of any error
	 * @param data
	 * @param offset
	 * @param len
	 * @param charset
	 * @return
	 */
	public static byte[] getBytesFromString(String str, String charset)
	{
		try
		{
			return str.getBytes(charset);
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	/**
	 * Returns bytes from the given string with charset UTF-16LE
	 * You have to reverse this bytes if you dont use LE charset
	 * Will return null in case of any error
	 * @param data
	 * @param offset
	 * @param len
	 * @param charset
	 * @return
	 */
	public static byte[] getBytesFromString(String str)
	{
		try
		{
			return str.getBytes("UTF-16LE");
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	/**
	 * Convers int32 to byte array (Little endian)
	 * @param num
	 * @return
	 */
	public static byte[] int32ToBytes(int num)
	{
		byte[] returnBytes = new byte[4];
		returnBytes[0] = (byte)(num & 0xFF);
		returnBytes[1] = (byte)((num >> 8) & 0xFF);
		returnBytes[2] = (byte)((num >> 16) & 0xFF);
		returnBytes[3] = (byte)((num >> 24) & 0xFF);
		
		return returnBytes;
	}
	
	/**
	 * Convers long64 to byte array (Little endian)
	 * @param num
	 * @return
	 */
	public static byte[] long64ToBytes(long num)
	{
		byte[] returnBytes = new byte[8];
		returnBytes[0] = (byte)(num & 0xFF);
		returnBytes[1] = (byte)((num >> 8) & 0xFF);
		returnBytes[2] = (byte)((num >> 16) & 0xFF);
		returnBytes[3] = (byte)((num >> 24) & 0xFF);
		returnBytes[4] = (byte)((num >> 32) & 0xFF);
		returnBytes[5] = (byte)((num >> 40) & 0xFF);
		returnBytes[6] = (byte)((num >> 48) & 0xFF);
		returnBytes[7] = (byte)((num >> 56) & 0xFF);
		
		return returnBytes;
	}
	
	
	public static byte[] readCompressed(byte[] data, int offset, int length)
	{
		return ZLib.decompress(getBytesRange(data, offset, length), length * 2);
	}
	
	/**
	 * Gets all bytes from data in the given range
	 * @param data
	 * @param offset
	 * @param length
	 * @return
	 */
	public static byte[] getBytesRange(byte[] data, int offset, int length)
	{
		byte[] rangeData = new byte[length];
		
		int j = 0;
		for (int i = offset; i < (offset + length); i++)
		{
			rangeData[j] = data[i];
			j++;
		}
		
		return rangeData;
	}
	
	/**
	 * Helper for little endian -> big endian / big endian -> little endian
	 * @param data
	 * @return
	 */
	public static byte[] reverseBytes(byte[] data, int steps)
	{
		// Variable for reversed big endian data
		byte[] bigEndian = new byte [data.length];
		
		// Reverse counter
		int j = bigEndian.length;
		
		// Reverse data
		for (int i = 0; i < bigEndian.length; i = i + steps)
		{
			for (int k = 0; k < steps; k++)
			{
				bigEndian[j-k] = data[i+k];
			}
			j--;
		}
		
		return bigEndian;
	}
	
	/**
	 * Concatenates 2 byte arrays
	 * @param data1
	 * @param data2
	 * @return
	 */
	public static byte[] concat(byte[] data1, byte[] data2)
	{
		byte[] newData = new byte[data1.length + data2.length];
		System.arraycopy(data1, 0, newData, 0, data1.length);
		System.arraycopy(data2, 0, newData, data1.length, data2.length);
		
		return newData;
	}
}
