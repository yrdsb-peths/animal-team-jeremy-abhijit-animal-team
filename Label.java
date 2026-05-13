import greenfoot.*;

public class Label extends Actor
{
    private static final int HORIZONTAL_PADDING = 12;
    private static final int VERTICAL_PADDING = 8;

    private final int fontSize;
    private final Color textColor;
    private final Color backgroundColor;
    private String value;

    public Label(String value, int fontSize)
    {
        this(value, fontSize, Color.WHITE, new Color(0, 0, 0, 150));
    }

    public Label(String value, int fontSize, Color textColor, Color backgroundColor)
    {
        this.fontSize = fontSize;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        setValue(value);
    }

    public void setValue(String value)
    {
        this.value = value;
        setImage(render());
    }

    public String getValue()
    {
        return value;
    }

    private GreenfootImage render()
    {
        GreenfootImage textImage = new GreenfootImage(value, fontSize, textColor, new Color(0, 0, 0, 0));
        int width = textImage.getWidth() + HORIZONTAL_PADDING * 2;
        int height = textImage.getHeight() + VERTICAL_PADDING * 2;

        GreenfootImage image = new GreenfootImage(width, height);
        image.setColor(backgroundColor);
        image.fillRect(0, 0, width, height);
        image.drawImage(textImage, HORIZONTAL_PADDING, VERTICAL_PADDING);
        return image;
    }
}
