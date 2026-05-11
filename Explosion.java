import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public class Explosion extends Actor
{
    private int life = 30;

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

        int size = 20 + (30 - life) * 3;
        GreenfootImage image = new GreenfootImage(size, size);
        image.setColor(new Color(255, 180, 60, 200));
        image.fillOval(0, 0, size, size);
        image.setColor(new Color(255, 90, 0, 140));
        image.drawOval(2, 2, size - 4, size - 4);
        setImage(image);

        life--;
    }
}
