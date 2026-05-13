import greenfoot.*;

public class TimerDisplay extends Label
{
    public TimerDisplay()
    {
        super("0:00", 24, Color.WHITE, new Color(0, 0, 0, 150));
    }

    public void setSecondsRemaining(int seconds)
    {
        if (seconds < 0)
        {
            seconds = 0;
        }

        int minutes = seconds / 60;
        int secs = seconds % 60;
        String text = String.format("%d:%02d", minutes, secs);
        setValue(text);
    }
}
