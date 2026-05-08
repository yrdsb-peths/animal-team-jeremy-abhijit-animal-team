import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.net.InetAddress;

public class GameClient {
    private static final int TCP_PORT = 54555;
    private static final int UDP_PORT = 54777;
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int DISCOVERY_TIMEOUT_MS = 2000;

    private final Client client;
    private final Object lock = new Object();

    private volatile boolean started;
    private volatile boolean connected;
    private volatile int playerId = -1;
    private volatile String statusLine = "Starting client...";

    private boolean remotePending;
    private int remoteX;
    private int remoteY;
    private boolean applePending;
    private int appleX;
    private int appleY;

    public GameClient() {
        client = new Client();
        registerClasses(client.getKryo());

        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                connected = true;
                statusLine = "Connected. Waiting for ID...";
            }

            @Override
            public void disconnected(Connection connection) {
                connected = false;
                statusLine = "Disconnected. Restart scenario to retry.";
            }

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof Packets.PlayerAssign) {
                    Packets.PlayerAssign assign = (Packets.PlayerAssign) object;
                    playerId = assign.id;
                    statusLine = "Connected as player " + playerId;
                } else if (object instanceof Packets.PlayerMove) {
                    Packets.PlayerMove move = (Packets.PlayerMove) object;
                    if (move.id != playerId) {
                        setRemotePosition(move.x, move.y);
                    }
                } else if (object instanceof Packets.AppleState) {
                    Packets.AppleState appleState = (Packets.AppleState) object;
                    setApplePosition(appleState.x, appleState.y);
                }
            }
        });
    }

    public void connectAsync() {
        Thread thread = new Thread(() -> {
            try {
                if (!started) {
                    client.start();
                    started = true;
                }

                statusLine = "Searching for server...";
                InetAddress host = client.discoverHost(UDP_PORT, DISCOVERY_TIMEOUT_MS);
                if (host == null) {
                    host = InetAddress.getByName("127.0.0.1");
                }

                statusLine = "Connecting to " + host.getHostAddress() + "...";
                client.connect(CONNECT_TIMEOUT_MS, host, TCP_PORT, UDP_PORT);
            } catch (IOException ex) {
                statusLine = "Server not found. Start server and restart.";
            }
        }, "kryonet-connect");

        thread.setDaemon(true);
        thread.start();
    }

    public boolean isConnected() {
        return connected;
    }

    public String getStatusLine() {
        return statusLine;
    }

    public void sendPlayerMove(int x, int y) {
        if (!connected) {
            return;
        }
        Packets.PlayerMove move = new Packets.PlayerMove();
        move.id = playerId;
        move.x = x;
        move.y = y;
        client.sendUDP(move);
    }

    public void sendAppleCaught() {
        if (!connected) {
            return;
        }
        Packets.AppleCaught caught = new Packets.AppleCaught();
        caught.id = playerId;
        client.sendTCP(caught);
    }

    public Position consumeRemotePosition() {
        synchronized (lock) {
            if (!remotePending) {
                return null;
            }
            remotePending = false;
            return new Position(remoteX, remoteY);
        }
    }

    public Position consumeApplePosition() {
        synchronized (lock) {
            if (!applePending) {
                return null;
            }
            applePending = false;
            return new Position(appleX, appleY);
        }
    }

    private void setRemotePosition(int x, int y) {
        synchronized (lock) {
            remoteX = x;
            remoteY = y;
            remotePending = true;
        }
    }

    private void setApplePosition(int x, int y) {
        synchronized (lock) {
            appleX = x;
            appleY = y;
            applePending = true;
        }
    }

    private void registerClasses(Kryo kryo) {
        kryo.register(Packets.PlayerAssign.class);
        kryo.register(Packets.PlayerMove.class);
        kryo.register(Packets.AppleState.class);
        kryo.register(Packets.AppleCaught.class);
    }

    public static class Position {
        public final int x;
        public final int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
