package net.kennux.cwinspect.packets;

public class UnknownPacket extends APacket
{
	public int getPacketId()
	{
		return -1;
	}
	
	public int loadPacket(byte[] data)
	{
		super.loadPacket(data);
		// Fixed size!
		return -1;
	}
	
	public byte[] buildPacket()
	{
		return null;
	}
}
