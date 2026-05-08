import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GameServer {
    private static final int WORLD_WIDTH = 600;
    private static final int WORLD_HEIGHT = 400;
    private static final int TCP_PORT = 54555;
    private static final int UDP_PORT = 54777;

    private final Server server;
    private final Random random = new Random();
    private final Map<Integer, Packets.PlayerMove> lastPositions = new HashMap<>();

    private int appleX;
    private int appleY;

    public GameServer() throws IOException {
        server = new Server();
        registerClasses(server.getKryo());

        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                Packets.PlayerAssign assign = new Packets.PlayerAssign();
                assign.id = connection.getID();
                connection.sendTCP(assign);

                connection.sendTCP(currentAppleState());

                for (Packets.PlayerMove move : lastPositions.values()) {
                    connection.sendTCP(move);
                }
            }

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof Packets.PlayerMove) {
                    Packets.PlayerMove move = (Packets.PlayerMove) object;
                    Packets.PlayerMove relay = new Packets.PlayerMove();
                    relay.id = connection.getID();
                    relay.x = move.x;
                    relay.y = move.y;
                    lastPositions.put(relay.id, relay);
                    server.sendToAllExceptUDP(connection.getID(), relay);
                } else if (object instanceof Packets.AppleCaught) {
                    moveApple();
                    server.sendToAllTCP(currentAppleState());
                }
            }

            @Override
            public void disconnected(Connection connection) {
                lastPositions.remove(connection.getID());
            }
        });

        moveApple();
        server.start();
        server.bind(TCP_PORT, UDP_PORT);
        System.out.println("GameServer running on TCP " + TCP_PORT + " / UDP " + UDP_PORT);
    }

    public static void main(String[] args) throws IOException {
        new GameServer();
    }

    private void moveApple() {
        appleX = random.nextInt(WORLD_WIDTH - 40) + 20;
        appleY = random.nextInt(WORLD_HEIGHT - 40) + 20;
    }

    private Packets.AppleState currentAppleState() {
        Packets.AppleState state = new Packets.AppleState();
        state.x = appleX;
        state.y = appleY;
        return state;
    }

    private void registerClasses(Kryo kryo) {
        kryo.register(Packets.PlayerAssign.class);
        kryo.register(Packets.PlayerMove.class);
        kryo.register(Packets.AppleState.class);
        kryo.register(Packets.AppleCaught.class);
    }
}
