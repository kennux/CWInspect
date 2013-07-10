package net.kennux.cwinspect.packets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import net.kennux.cwinspect.main.EntryPoint;
import net.kennux.cwinspect.packets.data.*;

public abstract class APacket
{
	// Output
	protected HashMap<String,PacketValue> values;
	
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
		this.values = new HashMap<String,PacketValue>();
	}
	
	public void setValue(String key, PacketValue value)
	{
		this.values.put(key, value);
	}
	
	// Overload toString method for simple output packet information
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
		
		Iterator<Entry<String,PacketValue>> iterator = this.values.entrySet().iterator();
		
		// Iterate through all values
		while (iterator.hasNext())
		{
			// Output
			Entry<String,PacketValue> currentEntry = iterator.next();
			
			returnString = returnString + "Valuename: " + currentEntry.getKey() + "\tValue: " + currentEntry.getValue().getValueTypeName() + " | " + currentEntry.getValue().getValue();
		}
		
		return returnString;
	}
	
	// Helper function for output
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
					// currentLine = currentLine.substring(1) + new String(currentBytes, "UTF-8");
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
