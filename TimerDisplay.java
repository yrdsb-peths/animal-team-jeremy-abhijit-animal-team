import greenfoot.*;

public class TimerDisplay extends Actor
{
    private static final int WIDTH = 120;
    private static final int HEIGHT = 36;
    private static final Font FONT = new Font("Arial", true, false, 24);

    public void setSecondsRemaining(int seconds)
    {
        if (seconds < 0)
        {
            seconds = 0;
        }

        int minutes = seconds / 60;
        int secs = seconds % 60;
        String text = String.format("%d:%02d", minutes, secs);
        setImage(render(text));
    }

    private GreenfootImage render(String text)
    {
        GreenfootImage image = new GreenfootImage(WIDTH, HEIGHT);
        image.setColor(new Color(0, 0, 0, 150));
        image.fillRect(0, 0, WIDTH, HEIGHT);
        image.setColor(Color.WHITE);
        image.setFont(FONT);
        image.drawString(text, 12, 26);
        return image;
    }
}
