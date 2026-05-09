import greenfoot.*;

public class RemoteElephant extends Actor {
    private static final float SMOOTHING = 0.25f;

    private int targetX;
    private int targetY;
    private boolean hasTarget;

    public RemoteElephant() {
        setImage(buildImage(new Color(40, 140, 220)));
    }

    public void act() {
        if (!hasTarget) {
            return;
        }

        int dx = targetX - getX();
        int dy = targetY - getY();

        int stepX = Math.round(dx * SMOOTHING);
        int stepY = Math.round(dy * SMOOTHING);

        if (stepX == 0 && dx != 0) {
            stepX = dx > 0 ? 1 : -1;
        }
        if (stepY == 0 && dy != 0) {
            stepY = dy > 0 ? 1 : -1;
        }

        setLocation(getX() + stepX, getY() + stepY);
    }

    public void setTargetPosition(int x, int y) {
        targetX = x;
        targetY = y;
        hasTarget = true;
    }

    private GreenfootImage buildImage(Color body) {
        GreenfootImage image = new GreenfootImage(50, 35);
        image.setColor(body);
        image.fillOval(2, 6, 36, 24);
        image.fillOval(24, 10, 12, 12);
        image.fillRect(30, 16, 14, 12);
        image.setColor(Color.WHITE);
        image.fillOval(8, 12, 8, 8);
        image.setColor(Color.BLACK);
        image.fillOval(11, 15, 3, 3);
        return image;
    }
}
