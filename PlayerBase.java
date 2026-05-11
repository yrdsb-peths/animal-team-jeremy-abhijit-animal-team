import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;

public class PlayerBase extends Actor
{
    private static final int BASE_SPEED = 2;
    private static final int BOMBER_SPEED = 3;
    private static final int BLINK_TICK = 10;
    private static final int TRANSFER_COOLDOWN_FRAMES = 60;

    private final boolean controlled;
    private final String displayName;

    private NameTag nameTag;
    private boolean bomber;
    private int blinkCounter;
    private int transferCooldown;

    private static final int SPRITE_HEIGHT = 72;
    private static final int ROTATION_OFFSET_DEGREES = 90;

    private GreenfootImage cursedFrame1;
    private GreenfootImage cursedFrame2;
    private GreenfootImage survivorFrame;

    public PlayerBase(String displayName, boolean controlled)
    {
        this.displayName = displayName;
        this.controlled = controlled;

        cursedFrame1 = loadScaled("Cursed-Frame-1.png");
        cursedFrame2 = loadScaled("Cursed-Frame-2.png");
        survivorFrame = loadScaled("Survivor-Frame-1.png");
        setImage(survivorFrame);
    }

    public void act()
    {
        if (getWorld() == null)
        {
            return;
        }

        if (nameTag == null)
        {
            attachNameTag();
        }

        MyWorld world = (MyWorld)getWorld();
        if (world.isRoundEnding())
        {
            updateNameTag();
            return;
        }

        if (controlled)
        {
            handleMovement();
            faceMouse();
        }

        handleBomberTransfer();
        updateBlink();
        updateNameTag();
    }

    public void setBomber(boolean isBomber)
    {
        bomber = isBomber;
        blinkCounter = 0;
        transferCooldown = 0;
        if (!bomber)
        {
            setImage(survivorFrame);
        }
    }

    public boolean isBomber()
    {
        return bomber;
    }

    public void attachNameTag()
    {
        if (getWorld() == null || nameTag != null)
        {
            return;
        }
        nameTag = new NameTag(displayName);
        getWorld().addObject(nameTag, getX(), getY() - tagOffset());
    }

    public void removeNameTag()
    {
        if (nameTag != null && nameTag.getWorld() != null)
        {
            nameTag.getWorld().removeObject(nameTag);
        }
        nameTag = null;
    }

    public boolean overlapsWall()
    {
        return isTouching(Wall.class);
    }

    public boolean overlaps(PlayerBase other)
    {
        return intersects(other);
    }

    private void handleMovement()
    {
        int speed = bomber ? BOMBER_SPEED : BASE_SPEED;
        int dx = 0;
        int dy = 0;

        if (Greenfoot.isKeyDown("w"))
        {
            dy -= speed;
        }
        if (Greenfoot.isKeyDown("s"))
        {
            dy += speed;
        }
        if (Greenfoot.isKeyDown("a"))
        {
            dx -= speed;
        }
        if (Greenfoot.isKeyDown("d"))
        {
            dx += speed;
        }

        moveAxis(dx, 0);
        moveAxis(0, dy);
    }

    private void moveAxis(int dx, int dy)
    {
        if (dx == 0 && dy == 0)
        {
            return;
        }

        int oldX = getX();
        int oldY = getY();
        setLocation(oldX + dx, oldY + dy);
        if (isTouching(Wall.class))
        {
            setLocation(oldX, oldY);
        }
    }

    private void faceMouse()
    {
        MouseInfo mouse = Greenfoot.getMouseInfo();
        if (mouse != null)
        {
            turnTowards(mouse.getX(), mouse.getY());
            setRotation(getRotation() + ROTATION_OFFSET_DEGREES);
        }
    }

    private void handleBomberTransfer()
    {
        if (!bomber)
        {
            if (transferCooldown > 0)
            {
                transferCooldown--;
            }
            return;
        }

        if (transferCooldown > 0)
        {
            transferCooldown--;
            return;
        }

        List<PlayerBase> touching = getIntersectingObjects(PlayerBase.class);
        for (PlayerBase other : touching)
        {
            if (other == this)
            {
                continue;
            }
            if (other.transferCooldown > 0)
            {
                continue;
            }
            setBomber(false);
            other.setBomber(true);
            transferCooldown = TRANSFER_COOLDOWN_FRAMES;
            other.transferCooldown = TRANSFER_COOLDOWN_FRAMES;
            break;
        }
    }

    private void updateBlink()
    {
        if (!bomber)
        {
            return;
        }

        blinkCounter++;
        if ((blinkCounter / BLINK_TICK) % 2 == 0)
        {
            setImage(cursedFrame1);
        }
        else
        {
            setImage(cursedFrame2);
        }
    }

    private void updateNameTag()
    {
        if (nameTag != null)
        {
            nameTag.setLocation(getX(), getY() - tagOffset());
        }
    }

    private int tagOffset()
    {
        return getImage().getHeight() / 2 + 12;
    }

    private GreenfootImage loadScaled(String filename)
    {
        GreenfootImage image = new GreenfootImage(filename);
        double scale = (double)SPRITE_HEIGHT / image.getHeight();
        int targetWidth = Math.max(1, (int)Math.round(image.getWidth() * scale));
        image.scale(targetWidth, SPRITE_HEIGHT);
        return image;
    }
}
