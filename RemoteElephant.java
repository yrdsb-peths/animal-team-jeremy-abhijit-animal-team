import greenfoot.*;

public class RemoteElephant extends Actor {
    public RemoteElephant() {
        setImage(buildImage(new Color(40, 140, 220)));
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
