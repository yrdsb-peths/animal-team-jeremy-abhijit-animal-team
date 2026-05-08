import greenfoot.*;

public class Apple extends Actor {
    public Apple() {
        setImage(buildImage());
    }

    private GreenfootImage buildImage() {
        GreenfootImage image = new GreenfootImage(16, 16);
        image.setColor(new Color(220, 40, 40));
        image.fillOval(1, 3, 12, 12);
        image.setColor(new Color(40, 160, 40));
        image.fillRect(9, 0, 2, 4);
        return image;
    }
}
