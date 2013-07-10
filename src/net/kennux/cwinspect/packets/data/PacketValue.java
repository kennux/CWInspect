package net.kennux.cwinspect.packets.data;

public class PacketValue
{
	private Object value;
	
	public PacketValue(Object value)
	{
		this.value = value;
	}
	
	public String getValueTypeName()
	{
		return this.value.getClass().getName();
	}
	
	public Object getValue()
	{
		return this.value;
	}
}
