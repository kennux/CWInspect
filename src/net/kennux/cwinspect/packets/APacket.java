package net.kennux.cwinspect.packets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import net.kennux.cwinspect.main.EntryPoint;

public abstract class APacket
{
	// Output
	protected HashMap<String,Object> values;
	
	// Packet data
	protected byte[] packetData;

	public abstract int getPacketId();
	public abstract byte[] buildPacket();
	
	// Load packet into local variable
	public void loadPacket(byte[] data)
	{
		this.packetData = data;
	}
	
	public APacket()
	{
		this.values = new HashMap<String,Object>();
	}
	
	public void setValue(String key, Object value)
	{
		this.values.put(key, value);
	}
	
	/**
	 * Overload toString method for simple output packet information
	 */
	public String toString()
	{
		String returnString = "";
		if (EntryPoint.configuration.getProperty("log_data").equals("1"))
		{
			returnString = returnString + byteToHexDump(this.packetData, 8);
		}
		
		if (this.getPacketId() < 0)
		{
			// Unidentified
			return returnString + "This packet could not get identified (ID=" + (int) this.packetData[0] + ")!\r\n";
		}
		
		// Packet identified
		returnString = returnString + "Packet Name: " + this.getClass().getSimpleName() + "\r\n";
		
		Iterator<Entry<String,Object>> iterator = this.values.entrySet().iterator();
		
		// Iterate through all values
		while (iterator.hasNext())
		{
			// Output
			Entry<String,Object> currentEntry = iterator.next();
			
			returnString = returnString + "Valuename: " + currentEntry.getKey() + "\tValue: " + currentEntry.getValue().getClass().getSimpleName() + " | " +
					currentEntry.getValue() + "\r\n";
		}
		
		return returnString;
	}
	
	/**
	 * Helper function for outputing packet information
	 * @param data
	 * @param columns
	 * @return
	 */
	private static String byteToHexDump(byte[] data, int columns)
	{
		String hexDump = "";
		String currentLine = "";
		
		// 
		int i = 0;
		byte[] currentBytes = new byte[columns];
		
		for(byte currentByte : data)
		{
			if ((i % columns == 0 && i != 0) || i == data.length-1)
			{
				// Substring 1 for removing first " "
				try
				{
					currentLine = byteToHex(currentBytes) + "\t" + new String(currentBytes, "UTF-8");
					currentBytes = new byte[columns];
				}
				catch (Exception e)
				{
					hexDump = currentLine + "\r\n";
				}
				
				hexDump = hexDump + currentLine + "\r\n";
			}
			
			currentBytes[i % columns] = currentByte;
			i++;
		}
		
		return hexDump;
	}
	
	/**
	 * Converts byte array to hex string
	 * ex. 0F 8B D4 AB
	 * @param bytes
	 * @return
	 */
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
