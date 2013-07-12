package net.kennux.cwinspect.packets.server;

import net.kennux.cwinspect.packets.APacket;

// This is only an example!
public class SeedPacket extends APacket
{
	public int getPacketId()
	{
		return 15;
	}
	
	public int loadPacket(byte[] data)
	{
		super.loadPacket(data);
		byte value = 58;
		this.values.put("Unknown", value);
		
		return -1;
	}

	public byte[] buildPacket()
	{
		return null;
	}
}
