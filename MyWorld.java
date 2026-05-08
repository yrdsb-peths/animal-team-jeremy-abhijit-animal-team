import greenfoot.*;

public class MyWorld extends World {
    private static final int WORLD_WIDTH = 600;
    private static final int WORLD_HEIGHT = 400;

    private Elephant localElephant;
    private RemoteElephant remoteElephant;
    private Apple apple;
    private GameClient client;
    private boolean awaitingAppleUpdate;

    public MyWorld() {
        super(WORLD_WIDTH, WORLD_HEIGHT, 1);
        prepare();

        client = new GameClient();
        client.connectAsync();
        showText(client.getStatusLine(), getWidth() / 2, 16);
    }

    public void act() {
        if (client == null) {
            return;
        }

        GameClient.Position remotePos = client.consumeRemotePosition();
        if (remotePos != null) {
            remoteElephant.setLocation(remotePos.x, remotePos.y);
        }

        GameClient.Position applePos = client.consumeApplePosition();
        if (applePos != null) {
            apple.setLocation(applePos.x, applePos.y);
            awaitingAppleUpdate = false;
        }

        showText(client.getStatusLine(), getWidth() / 2, 16);
    }

    public void sendLocalPosition(int x, int y) {
        if (client != null) {
            client.sendPlayerMove(x, y);
        }
    }

    public void handleAppleCaught() {
        if (awaitingAppleUpdate) {
            return;
        }

        if (client != null && client.isConnected()) {
            awaitingAppleUpdate = true;
            client.sendAppleCaught();
        } else {
            moveAppleRandom();
        }
    }

    private void moveAppleRandom() {
        apple.setLocation(
                Greenfoot.getRandomNumber(WORLD_WIDTH - 40) + 20,
                Greenfoot.getRandomNumber(WORLD_HEIGHT - 40) + 20
        );
    }

    private void prepare() {
        localElephant = new Elephant();
        remoteElephant = new RemoteElephant();
        apple = new Apple();

        addObject(localElephant, 120, 220);
        addObject(remoteElephant, 480, 220);
        addObject(
                apple,
                Greenfoot.getRandomNumber(WORLD_WIDTH - 40) + 20,
                Greenfoot.getRandomNumber(WORLD_HEIGHT - 40) + 20
        );
    }
}
