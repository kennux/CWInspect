package net.kennux.cwinspect.packets.server;

import net.kennux.cwinspect.packets.APacket;
import net.kennux.cwinspect.packets.data.PacketValue;

public class SeedPacket extends APacket
{
	public int getPacketId()
	{
		return 15;
	}
	
	public void loadPacket(byte[] data)
	{
		super.loadPacket(data);
		PacketValue value = new PacketValue(data[1]);
		this.values.put("Unknown", value);
	}
	
	public String toString()
	{
		String str = super.toString();
		return str;
	}

	public byte[] buildPacket()
	{
		return null;
	}
}
