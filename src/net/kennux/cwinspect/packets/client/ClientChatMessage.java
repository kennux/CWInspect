package net.kennux.cwinspect.packets.client;

import net.kennux.cwinspect.packets.APacket;
import net.kennux.cwinspect.packets.helpers.ByteUtils;

public class ClientChatMessage extends APacket
{

	public int getPacketId()
	{
		return 10;
	}
	
	public int loadPacket(byte[] data)
	{
		super.loadPacket(data);
		
		// Text size
		int len = ByteUtils.readInt32(data, 4);
		
		// Read text ( * 2 because of UTF-16)
		String text = ByteUtils.readString(data, 8, len*2);
		
		this.values.put("ChatMessage", text);
		
		return 8 + (len*2);
	}
	
	public byte[] buildPacket()
	{
		return null;
	}

}
