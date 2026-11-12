"""Minimal RCON client: rcon.py <port> <password> <command>..."""
import socket, struct, sys
def pkt(i, t, body): b = body.encode() + b"\0\0"; return struct.pack("<iii", len(b) + 8, i, t) + b
def recv(s):
    n = struct.unpack("<i", s.recv(4))[0]; d = b""
    while len(d) < n: d += s.recv(n - len(d))
    return struct.unpack("<ii", d[:8]), d[8:-2].decode(errors="replace")
port, pw, cmds = int(sys.argv[1]), sys.argv[2], sys.argv[3:]
s = socket.create_connection(("127.0.0.1", port), timeout=30)
s.sendall(pkt(1, 3, pw)); (rid, _), _ = recv(s)
if rid == -1: sys.exit("auth failed")
for i, c in enumerate(cmds, 2):
    s.sendall(pkt(i, 2, c)); _, out = recv(s); print(f"> {c}\n{out}")
