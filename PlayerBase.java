import greenfoot.*;
import java.util.List;

public class PlayerBase extends Actor
{
    private static final int BASE_SPEED = 6;
    private static final double BOMBER_SPEED = 6.5;
    private static final int BLINK_TICK = 6;
    private static final int TRANSFER_COOLDOWN_FRAMES = 60;
    private static final int UNSTUCK_RADIUS = 8;
    private static final int STUCK_FRAMES = 4;
    private static final int COLLISION_RADIUS = 18;

    private final boolean controlled;
    private final String displayName;
    private final String upKey;
    private final String downKey;
    private final String leftKey;
    private final String rightKey;

    private NameTag nameTag;
    private boolean bomber;
    private int blinkCounter;
    private int transferCooldown;
    private int stuckFrames;
    private int lastX;
    private int lastY;

    private static final int SPRITE_HEIGHT = 64;
    private GreenfootImage cursedFrame1;
    private GreenfootImage cursedFrame2;
    private GreenfootImage survivorFrame;

    public PlayerBase(String displayName, boolean controlled, String upKey, String downKey, String leftKey, String rightKey)
    {
        this.displayName = displayName;
        this.controlled = controlled;
        this.upKey = upKey;
        this.downKey = downKey;
        this.leftKey = leftKey;
        this.rightKey = rightKey;

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

        if (lastX == 0 && lastY == 0)
        {
            lastX = getX();
            lastY = getY();
        }

        MyWorld world = (MyWorld)getWorld();
        if (world.isRoundEnding())
        {
            updateNameTag();
            return;
        }

        if (controlled)
        {
            boolean moved = handleMovement();
            handleStuck(moved);
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
        return collidesWithWallAt(getX(), getY());
    }

    public boolean overlaps(PlayerBase other)
    {
        return intersects(other);
    }

    private boolean handleMovement()
    {
        double speed = bomber ? BOMBER_SPEED : BASE_SPEED;
        double dx = 0;
        double dy = 0;
        boolean inputActive = false;

        if (Greenfoot.isKeyDown(upKey))
        {
            dy -= 1;
            inputActive = true;
        }
        if (Greenfoot.isKeyDown(downKey))
        {
            dy += 1;
            inputActive = true;
        }
        if (Greenfoot.isKeyDown(leftKey))
        {
            dx -= 1;
            inputActive = true;
        }
        if (Greenfoot.isKeyDown(rightKey))
        {
            dx += 1;
            inputActive = true;
        }

        if (!inputActive)
        {
            return false;
        }

        if (dx != 0 && dy != 0)
        {
            double diag = speed / Math.sqrt(2);
            dx *= diag;
            dy *= diag;
        }
        else
        {
            dx *= speed;
            dy *= speed;
        }

        setRotationFromVector((int)Math.signum(dx), (int)Math.signum(dy));

        int stepX = (int)Math.round(dx);
        int stepY = (int)Math.round(dy);

        moveAxis(stepX, 0);
        moveAxis(0, stepY);

        boolean moved = getX() != lastX || getY() != lastY;
        lastX = getX();
        lastY = getY();
        return moved;
    }

    private void setRotationFromVector(int dx, int dy)
    {
        if (dx == 0 && dy < 0)
        {
            setRotation(0);
        }
        else if (dx > 0 && dy < 0)
        {
            setRotation(45);
        }
        else if (dx > 0 && dy == 0)
        {
            setRotation(90);
        }
        else if (dx > 0 && dy > 0)
        {
            setRotation(135);
        }
        else if (dx == 0 && dy > 0)
        {
            setRotation(180);
        }
        else if (dx < 0 && dy > 0)
        {
            setRotation(225);
        }
        else if (dx < 0 && dy == 0)
        {
            setRotation(270);
        }
        else if (dx < 0 && dy < 0)
        {
            setRotation(315);
        }
    }

    private void moveAxis(int dx, int dy)
    {
        if (dx == 0 && dy == 0)
        {
            return;
        }

        int steps = Math.abs(dx != 0 ? dx : dy);
        int stepX = Integer.signum(dx);
        int stepY = Integer.signum(dy);

        for (int i = 0; i < steps; i++)
        {
            int nextX = getX() + stepX;
            int nextY = getY() + stepY;
            if (collidesWithWallAt(nextX, nextY))
            {
                break;
            }
            setLocation(nextX, nextY);
        }
    }

    private void handleStuck(boolean moved)
    {
        if (collidesWithWallAt(getX(), getY()) && !moved)
        {
            stuckFrames++;
            if (stuckFrames >= STUCK_FRAMES)
            {
                resolveWallOverlap();
                stuckFrames = 0;
            }
            return;
        }

        stuckFrames = 0;
    }

    private void resolveWallOverlap()
    {
        if (!collidesWithWallAt(getX(), getY()))
        {
            return;
        }

        int startX = getX();
        int startY = getY();

        for (int d = 1; d <= UNSTUCK_RADIUS; d++)
        {
            int[] dirs = {0, -d, 0, d, -d, 0, d, 0, -d, -d, d, -d, -d, d, d, d};
            for (int i = 0; i < dirs.length; i += 2)
            {
                setLocation(startX + dirs[i], startY + dirs[i+1]);
                if (!collidesWithWallAt(getX(), getY()))
                {
                    return;
                }
            }
        }

        setLocation(startX, startY);
    }

    private boolean collidesWithWallAt(int x, int y)
    {
        if (getWorld() == null)
        {
            return false;
        }

        int r = COLLISION_RADIUS;
        int d = (int)Math.round(r * 0.7);
        int[][] offsets = new int[][] {
            {0, -r}, {0, r}, {-r, 0}, {r, 0},
            {-d, -d}, {d, -d}, {-d, d}, {d, d}
        };

        for (int[] offset : offsets)
        {
            if (!getWorld().getObjectsAt(x + offset[0], y + offset[1], Wall.class).isEmpty())
            {
                return true;
            }
        }

        return false;
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
            ((MyWorld)getWorld()).setBombCarrier(other instanceof Player1 ? 1 : 2);
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
