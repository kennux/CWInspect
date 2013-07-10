package net.kennux.cwinspect.packets;

public interface IHook
{
	// Hook will get called at which packet?
	public int getPacketId();
	
	// Return null for send nothing
	public byte[] hook(byte[] data);
}
