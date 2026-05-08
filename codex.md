# Codex

- KryoNet server binds TCP 54555 and UDP 54777; client discovers via UDP broadcast, then connects.
- Server assigns a player id from the connection and relays `PlayerMove` to other clients.
- Apple position is server-authoritative; clients send `AppleCaught`, server broadcasts `AppleState`.
- Client listener only caches updates; Greenfoot `act()` applies them to avoid thread conflicts.
- Local movement sends a position update on spawn and when the elephant moves.
- If no server is found, the client stays offline and the apple moves locally.
- Separate `Elephant` and `RemoteElephant` actors keep local input and remote sync clean.
