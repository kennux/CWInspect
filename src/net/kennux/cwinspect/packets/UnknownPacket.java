package net.kennux.cwinspect.packets;

public class UnknownPacket extends APacket
{
	public int getPacketId()
	{
		return -1;
	}

	public byte[] buildPacket()
	{
		return null;
	}
}
