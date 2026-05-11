import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Write a description of class MyWorld here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class MyWorld extends World
{
    private static final int WORLD_WIDTH = 1000;
    private static final int WORLD_HEIGHT = 700;
    private static final int ROUND_LENGTH_MS = 120000;
    private static final int RESET_DELAY_FRAMES = 60;
    private static final int MIN_SPAWN_DISTANCE = 220;

    private final Random random = new Random();
    private final List<PlayerBase> players = new ArrayList<>();

    private TimerDisplay timerDisplay;
    private long roundStartMs;
    private boolean roundEnding;
    private int resetCountdown;

    /**
     * Constructor for objects of class MyWorld.
     * 
     */
    public MyWorld()
    {    
        // Create a new world with 600x400 cells with a cell size of 1x1 pixels.
        super(WORLD_WIDTH, WORLD_HEIGHT, 1);
        setPaintOrder(TimerDisplay.class, NameTag.class, Explosion.class, PlayerBase.class, Wall.class);
        buildWalls();
        setupRound();
    }

    public void act()
    {
        updateTimer();
        if (roundEnding)
        {
            resetCountdown--;
            if (resetCountdown <= 0)
            {
                resetRound();
            }
        }
    }

    public boolean isRoundEnding()
    {
        return roundEnding;
    }

    private void setupRound()
    {
        if (timerDisplay == null)
        {
            timerDisplay = new TimerDisplay();
            addObject(timerDisplay, WORLD_WIDTH / 2, 30);
        }

        spawnPlayers();
        pickBomber();
        roundStartMs = System.currentTimeMillis();
        roundEnding = false;
        resetCountdown = 0;
        updateTimer();
    }

    private void updateTimer()
    {
        int remainingMs = (int)(ROUND_LENGTH_MS - (System.currentTimeMillis() - roundStartMs));
        int remainingSeconds = remainingMs / 1000;
        if (remainingSeconds <= 0 && !roundEnding)
        {
            triggerExplosion();
        }
        if (timerDisplay != null)
        {
            timerDisplay.setSecondsRemaining(Math.max(0, remainingSeconds));
        }
    }

    private void triggerExplosion()
    {
        roundEnding = true;
        resetCountdown = RESET_DELAY_FRAMES;

        PlayerBase bomber = getBomber();
        if (bomber != null && bomber.getWorld() != null)
        {
            int x = bomber.getX();
            int y = bomber.getY();
            bomber.removeNameTag();
            removeObject(bomber);
            addObject(new Explosion(), x, y);
        }
    }

    private void resetRound()
    {
        removeObjects(getObjects(PlayerBase.class));
        removeObjects(getObjects(NameTag.class));
        removeObjects(getObjects(Explosion.class));
        players.clear();
        setupRound();
    }

    private void spawnPlayers()
    {
        players.clear();

        Player1 player1 = new Player1();
        addPlayerWithSpawn(player1, null);

        Player2 player2 = new Player2();
        addPlayerWithSpawn(player2, player1);

        Player3 player3 = new Player3();
        addPlayerWithSpawn(player3, player1);

        Player4 player4 = new Player4();
        addPlayerWithSpawn(player4, player1);
    }

    private void addPlayerWithSpawn(PlayerBase player, PlayerBase avoidPlayer)
    {
        int attempts = 200;
        while (attempts-- > 0)
        {
            int x = randomRange(40, WORLD_WIDTH - 40);
            int y = randomRange(40, WORLD_HEIGHT - 40);
            addObject(player, x, y);

            if (isValidSpawn(player, avoidPlayer))
            {
                players.add(player);
                player.attachNameTag();
                return;
            }

            removeObject(player);
        }

        addObject(player, WORLD_WIDTH / 2, WORLD_HEIGHT / 2);
        players.add(player);
        player.attachNameTag();
    }

    private boolean isValidSpawn(PlayerBase player, PlayerBase avoidPlayer)
    {
        if (player.overlapsWall())
        {
            return false;
        }

        for (PlayerBase other : players)
        {
            if (other == player)
            {
                continue;
            }
            if (player.overlaps(other))
            {
                return false;
            }
            if (distance(player, other) < MIN_SPAWN_DISTANCE)
            {
                return false;
            }
        }

        if (avoidPlayer != null && distance(player, avoidPlayer) < MIN_SPAWN_DISTANCE)
        {
            return false;
        }

        return true;
    }

    private int randomRange(int min, int max)
    {
        return min + random.nextInt(Math.max(1, max - min + 1));
    }

    private double distance(Actor a, Actor b)
    {
        int dx = a.getX() - b.getX();
        int dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void pickBomber()
    {
        if (players.isEmpty())
        {
            return;
        }

        PlayerBase bomber = players.get(random.nextInt(players.size()));
        for (PlayerBase player : players)
        {
            player.setBomber(player == bomber);
        }
    }

    private PlayerBase getBomber()
    {
        for (PlayerBase player : players)
        {
            if (player.isBomber())
            {
                return player;
            }
        }
        return null;
    }

    private void buildWalls()
    {
        int thickness = 28;
        int border = 26;

        addObject(new Wall(WORLD_WIDTH, thickness), WORLD_WIDTH / 2, thickness / 2);
        addObject(new Wall(WORLD_WIDTH, thickness), WORLD_WIDTH / 2, WORLD_HEIGHT - thickness / 2);
        addObject(new Wall(thickness, WORLD_HEIGHT), thickness / 2, WORLD_HEIGHT / 2);
        addObject(new Wall(thickness, WORLD_HEIGHT), WORLD_WIDTH - thickness / 2, WORLD_HEIGHT / 2);

        int lLen = 220;
        int lOffset = thickness / 2 + border;

        addObject(new Wall(lLen, thickness), lOffset + lLen / 2, lOffset);
        addObject(new Wall(thickness, lLen), lOffset, lOffset + lLen / 2);

        addObject(new Wall(lLen, thickness), WORLD_WIDTH - lOffset - lLen / 2, lOffset);
        addObject(new Wall(thickness, lLen), WORLD_WIDTH - lOffset, lOffset + lLen / 2);

        addObject(new Wall(lLen, thickness), lOffset + lLen / 2, WORLD_HEIGHT - lOffset);
        addObject(new Wall(thickness, lLen), lOffset, WORLD_HEIGHT - lOffset - lLen / 2);

        addObject(new Wall(lLen, thickness), WORLD_WIDTH - lOffset - lLen / 2, WORLD_HEIGHT - lOffset);
        addObject(new Wall(thickness, lLen), WORLD_WIDTH - lOffset, WORLD_HEIGHT - lOffset - lLen / 2);

    }
}
