import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public class CursedOverlay extends Actor
{
    public CursedOverlay(int width, int height)
    {
        GreenfootImage image = new GreenfootImage(width, height);
        image.setColor(new Color(200, 0, 0, 80));
        image.fillRect(0, 0, width, height);
        setImage(image);
    }
}
