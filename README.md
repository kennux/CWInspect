CWInspect
=========

CWInspect is a Cube World Man-in-the-Middle server implementation written in java for logging and analyzing the cube worlds protocl.

Currently Done
=========

- Man-in-the-middle server
- Packet logging
- Hooks for manipulating data or send other data
- Analyze packets based off classes in the project (Look at net.kennux.cwinspect.packets.server.SeedPacket)
- Use hooks which will get activated when the packet id of a received packet matches a hook packet id
- Helper classes for reading / sending data (ByteUtils)

Hooks
=========

Hooks have to be put into
- net.kennux.cwinspect.hooks.server -> Hooks that will get activated on packet from server to client with the specified packet id
- net.kennux.cwinspect.hooks.client -> Hooks that will get activated on packet from client to server with the specified packet id

Hooks have to forward data by itself. Example: 
```java
	public void hook(byte[] data, OutputStream clientStream, OutputStream serverStream)
	{
		// A hook HAVE TO forward the data by itself
		try
		{
			serverStream.write(data);
		}
		catch (Exception e)
		{
			
		}
	}
```

Packets
=========

Packets have to be put into
- net.kennux.cwinspect.packets.server -> Packets from server
- net.kennux.cwinspect.packets.client -> Packets from client

Packets have to implement the following Methods:
- getPacketId() -> Return integer of the packet id
- loadPacket(byte[] data) -> Loads packet data and adds it with value names into the HashMap this.values, return -1 for packet length unknown or the length of the packet
- buildPacket() -> Return byte array of the built packet (When using implementation for sending packets, otherwise return null)


Logs
=========

CWInspect can log packets binary and as hexdump.
They will get logged as hexdump to console / logfile if you put an 1 into log_data

Binary logs do need the timestamp logfile, because the different packets in the logfiles will get marked there.

Example: "Server -> Client [11.07.2013 22:04:35] [148] 0x9FFA - 0xA104".
The [148] are the current milliseconds

CWInspect can also only log unknown packets as hexdump if you put a 1 into log_unknown.

Properties
=========

- remote_host = your remote cube world host
- remote_port = your remote cube world port
- local_port = the local port where the server should start listening (12345)
- log_to_file = Log console output to file ("" for log to console)
- log_binary_server = Logfile for binary data from server -> client, "" for no log
- log_binary_client = Logfile for binary data from client -> server, "" for no log
- log_timestamps = Logfile for data timestamps, needed for binary log!
- log_unknown = Log unknown packets as Hex Dump
- log_data = 1 for log data in console, 0 for not log data
- log_binary = 1 for log binary data in console or logfile, 0 for only log hex (ex. 0F 14 3A)
- columns_hex = how much bytes in hexadecimal (ex. 0F 15) per line?

License
=========
Copyright (c) 2013 Kenneth Ellersdorfer
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.