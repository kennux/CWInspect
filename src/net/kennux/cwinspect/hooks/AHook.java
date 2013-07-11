package net.kennux.cwinspect.hooks;

import java.io.OutputStream;

public abstract class AHook
{
	/**
	 *  Hook will get called at which packet?
	 * @return
	 */
	public abstract int getPacketId();
	
	/**
	 *  Return null for send nothing
	 * @param data
	 * @return
	 */
	public abstract void hook(byte[] data, OutputStream clientStream, OutputStream serverStream);
	
	/**
	 * Used for outputting the information about the hook
	 */
	public String toString()
	{
		return "None";
	}
}
