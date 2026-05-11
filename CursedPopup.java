import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public class CursedPopup extends Actor
{
    private static final int LIFE_MAX = 50;
    private static final String TEXT = "CURSED!";

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
        int fontSize = 28 + (int)(t * 8);
        int alpha = Math.max(0, 255 - (int)(t * 200));

        GreenfootImage text = new GreenfootImage(TEXT, fontSize, new Color(255, 255, 255, alpha), null);
        GreenfootImage shadow = new GreenfootImage(TEXT, fontSize, new Color(0, 0, 0, alpha), null);

        int width = text.getWidth() + 20;
        int height = text.getHeight() + 12;
        GreenfootImage image = new GreenfootImage(width, height);
        image.setColor(new Color(0, 0, 0, Math.max(0, alpha - 80)));
        image.fillRect(0, 0, width, height);
        image.drawImage(shadow, 11, 7);
        image.drawImage(text, 10, 6);
        setImage(image);

        setLocation(getX(), getY() - 1);
        life--;
    }
}
