import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.Random;

public class Explosion extends Actor
{
    private static final int LIFE_MAX = 40;
    private static final Random RANDOM = new Random();

    private int life = LIFE_MAX;

    public void act()
    {
        if (life <= 0)
        {
            if (getWorld() != null)
            {
                getWorld().removeObject(this);
            }
            return;
        }

        double t = 1.0 - (double)life / LIFE_MAX;
        int size = 30 + (int)(t * 180);
        int alpha = Math.max(0, 220 - (int)(t * 200));
        GreenfootImage image = new GreenfootImage(size, size);

        image.setColor(new Color(255, 200, 80, alpha));
        image.fillOval(0, 0, size, size);

        image.setColor(new Color(255, 130, 30, Math.max(0, alpha - 40)));
        image.fillOval(size / 6, size / 6, size * 2 / 3, size * 2 / 3);

        image.setColor(new Color(255, 60, 0, Math.max(0, alpha - 80)));
        image.drawOval(2, 2, size - 4, size - 4);

        image.setColor(new Color(255, 140, 40, Math.max(0, alpha - 20)));
        for (int i = 0; i < 10; i++)
        {
            int sx = RANDOM.nextInt(size);
            int sy = RANDOM.nextInt(size);
            int r = 2 + RANDOM.nextInt(3);
            image.fillOval(sx, sy, r, r);
        }
        setImage(image);

        life--;
    }
}
