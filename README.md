CWInspect
=========

CWInspect is a Cube World Man-in-the-Middle server implementation written in java for logging and analyzing the cube worlds protocl.

Currently Done
=========

- Man-in-the-middle server
- Packet logging
- Analyze packets based off classes in the project (Look at net.kennux.cwinspect.packets.server.SeedPacket)
- Use hooks which will get activated when the packet id of a received packet matches a hook packet id

Planned
=========

- Implement more packets
- Clean up the code
- Add helper classes for ex. zlib usage

Properties
=========

- remote_host = your remote cube world host
- remote_port = your remote cube world port
- local_port = the local port where the server should start listening (12345)
- log_to_file = Log console output to file ("" for log to console)
- log_data = 1 for log data in console, 0 for not log data


You can use this code to do whatever you want.