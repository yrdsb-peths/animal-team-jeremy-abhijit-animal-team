import greenfoot.*;

public class Elephant extends Actor {
    private static final int SPEED = 3;

    private int lastX;
    private int lastY;
    private boolean sentSpawn;

    public Elephant() {
        setImage(buildImage(new Color(60, 190, 255)));
    }

    public void act() {
        int dx = 0;
        int dy = 0;

        if (Greenfoot.isKeyDown("left") || Greenfoot.isKeyDown("a")) {
            dx -= SPEED;
        }
        if (Greenfoot.isKeyDown("right") || Greenfoot.isKeyDown("d")) {
            dx += SPEED;
        }
        if (Greenfoot.isKeyDown("up") || Greenfoot.isKeyDown("w")) {
            dy -= SPEED;
        }
        if (Greenfoot.isKeyDown("down") || Greenfoot.isKeyDown("s")) {
            dy += SPEED;
        }

        if (dx != 0 || dy != 0) {
            int newX = clamp(getX() + dx, 15, getWorld().getWidth() - 15);
            int newY = clamp(getY() + dy, 15, getWorld().getHeight() - 15);
            setLocation(newX, newY);
        }

        if (getWorld() instanceof MyWorld) {
            MyWorld world = (MyWorld) getWorld();
            if (!sentSpawn) {
                world.sendLocalPosition(getX(), getY());
                lastX = getX();
                lastY = getY();
                sentSpawn = true;
            }
            if (getX() != lastX || getY() != lastY) {
                world.sendLocalPosition(getX(), getY());
                lastX = getX();
                lastY = getY();
            }
            if (isTouching(Apple.class)) {
                world.handleAppleCaught();
            }
        }
    }

    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
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
