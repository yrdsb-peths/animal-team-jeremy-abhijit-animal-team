import greenfoot.*;

public class NameTag extends Actor
{
    private static final int FONT_SIZE = 14;
    private static final int H_PADDING = 8;
    private static final int V_PADDING = 4;

    private final String text;

    public NameTag(String text)
    {
        this.text = text;
        updateImage();
    }

    private void updateImage()
    {
        GreenfootImage textImage = new GreenfootImage(text, FONT_SIZE, Color.WHITE, null);
        int width = textImage.getWidth() + H_PADDING * 2;
        int height = textImage.getHeight() + V_PADDING * 2;

        GreenfootImage image = new GreenfootImage(width, height);
        image.setColor(new Color(0, 0, 0, 140));
        image.fillRect(0, 0, width, height);
        image.drawImage(textImage, H_PADDING, V_PADDING);
        setImage(image);
    }
}
