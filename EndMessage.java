import greenfoot.*;

public class EndMessage extends Actor
{
    public EndMessage(String title, String detail, int width, int height)
    {
        setImage(render(title, detail, width, height));
    }

    private GreenfootImage render(String titleText, String detailText, int width, int height)
    {
        GreenfootImage image = new GreenfootImage(width, height);
        image.setColor(new Color(0, 0, 0, 170));
        image.fillRect(0, 0, width, height);

        int boxWidth = Math.min(620, width - 120);
        int boxHeight = 210;
        int boxX = (width - boxWidth) / 2;
        int boxY = (height - boxHeight) / 2;

        image.setColor(new Color(25, 25, 25, 230));
        image.fillRect(boxX, boxY, boxWidth, boxHeight);
        image.setColor(new Color(255, 200, 80));
        image.drawRect(boxX, boxY, boxWidth - 1, boxHeight - 1);

        GreenfootImage title = new GreenfootImage(titleText, 48, Color.WHITE, new Color(0, 0, 0, 0));
        GreenfootImage subtitle = new GreenfootImage("Time is up", 24, new Color(220, 220, 220), new Color(0, 0, 0, 0));
        GreenfootImage detail = new GreenfootImage(detailText, 22, new Color(255, 230, 180), new Color(0, 0, 0, 0));

        image.drawImage(title, (width - title.getWidth()) / 2, boxY + 48);
        image.drawImage(subtitle, (width - subtitle.getWidth()) / 2, boxY + 110);
        image.drawImage(detail, (width - detail.getWidth()) / 2, boxY + 148);
        return image;
    }
}
