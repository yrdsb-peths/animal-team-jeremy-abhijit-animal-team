import greenfoot.*;

public class Wall extends Actor
{
    public Wall(int width, int height)
    {
        GreenfootImage image = new GreenfootImage(width, height);
        image.setColor(new Color(190, 130, 70));
        image.fillRect(0, 0, width, height);
        setImage(image);
    }
}
