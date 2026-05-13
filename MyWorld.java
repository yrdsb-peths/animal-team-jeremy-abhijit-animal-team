import greenfoot.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyWorld extends World
{
    private static final int WORLD_WIDTH = 1000;
    private static final int WORLD_HEIGHT = 700;
    private static final int ROUND_LENGTH_MS = 5000;
    private static final int RESET_DELAY_FRAMES = 180;
    private static final int MIN_SPAWN_DISTANCE = 220;

    private final Random random = new Random();
    private final List<PlayerBase> players = new ArrayList<>();

    private TimerDisplay timerDisplay;
    private EndMessage endMessage;
    private Player1 player1;
    private long roundStartMs;
    private boolean roundEnding;
    private int resetCountdown;
    
    private int p1Score = 0;
    private int p2Score = 0;
    
    private int bombCarrier = 1;
    public int getBombCarrier()
    {
        return bombCarrier;
    }
    public void setBombCarrier(int playerNum)
    {
        bombCarrier = playerNum;
    }
    public MyWorld()
    {
        super(WORLD_WIDTH, WORLD_HEIGHT, 1);
        setPaintOrder(EndMessage.class, TimerDisplay.class, NameTag.class, Explosion.class, PlayerBase.class, Wall.class);
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

        PlayerBase explodedPlayer = getBomber();
        showEndMessage(getWinnerText(explodedPlayer), getEndDetail(explodedPlayer));
        if (explodedPlayer != null && explodedPlayer.getWorld() != null)
        {
            int x = explodedPlayer.getX();
            int y = explodedPlayer.getY();
            explodedPlayer.removeNameTag();
            removeObject(explodedPlayer);
            addObject(new Explosion(), x, y);
        }
    }

    private void resetRound()
    {
        removeObjects(getObjects(PlayerBase.class));
        removeObjects(getObjects(NameTag.class));
        removeObjects(getObjects(Explosion.class));
        removeObjects(getObjects(EndMessage.class));
        endMessage = null;
        player1 = null;
        players.clear();
        setupRound();
    }

    private void spawnPlayers()
    {
        players.clear();

        player1 = new Player1();
        addPlayerWithSpawn(player1, null);

        Player2 player2 = new Player2();
        addPlayerWithSpawn(player2, player1);
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
        setBombCarrier(bomber instanceof Player1 ? 1 : 2);
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

    private String getWinnerText(PlayerBase explodedPlayer)
    {
        if (explodedPlayer instanceof Player1)
        {
            return "Player 2 Wins!";
        }
        if (explodedPlayer instanceof Player2)
        {
            return "Player 1 Wins!";
        }
        return "Time's Up!";
    }

    private String getEndDetail(PlayerBase explodedPlayer)
    {
        if (explodedPlayer instanceof Player1)
        {
            return "Player 1 blew up when the 5-second timer ended.";
        }
        if (explodedPlayer instanceof Player2)
        {
            return "Player 2 blew up when the 5-second timer ended.";
        }
        return "The player holding the bomb loses when time expires.";
    }

    private void showEndMessage(String title, String detail)
    {
        if (endMessage != null)
        {
            removeObject(endMessage);
        }
        endMessage = new EndMessage(title, detail, WORLD_WIDTH, WORLD_HEIGHT);
        addObject(endMessage, WORLD_WIDTH / 2, WORLD_HEIGHT / 2);
    }

    private void buildWalls()
    {
        int thickness = 28;
        int borderGap = 160;

        int topBottomSegment = (WORLD_WIDTH - borderGap) / 2;
        addObject(new Wall(topBottomSegment, thickness), topBottomSegment / 2, thickness / 2);
        addObject(new Wall(topBottomSegment, thickness), WORLD_WIDTH - topBottomSegment / 2, thickness / 2);
        addObject(new Wall(topBottomSegment, thickness), topBottomSegment / 2, WORLD_HEIGHT - thickness / 2);
        addObject(new Wall(topBottomSegment, thickness), WORLD_WIDTH - topBottomSegment / 2, WORLD_HEIGHT - thickness / 2);

        int sideSegment = (WORLD_HEIGHT - borderGap) / 2;
        addObject(new Wall(thickness, sideSegment), thickness / 2, sideSegment / 2);
        addObject(new Wall(thickness, sideSegment), thickness / 2, WORLD_HEIGHT - sideSegment / 2);
        addObject(new Wall(thickness, sideSegment), WORLD_WIDTH - thickness / 2, sideSegment / 2);
        addObject(new Wall(thickness, sideSegment), WORLD_WIDTH - thickness / 2, WORLD_HEIGHT - sideSegment / 2);

        int lLen = 130;
        int lOffset = 140;

        addObject(new Wall(lLen, thickness), lOffset + lLen / 2, lOffset);
        addObject(new Wall(thickness, lLen), lOffset, lOffset + lLen / 2);

        addObject(new Wall(lLen, thickness), WORLD_WIDTH - lOffset - lLen / 2, lOffset);
        addObject(new Wall(thickness, lLen), WORLD_WIDTH - lOffset, lOffset + lLen / 2);

        addObject(new Wall(lLen, thickness), lOffset + lLen / 2, WORLD_HEIGHT - lOffset);
        addObject(new Wall(thickness, lLen), lOffset, WORLD_HEIGHT - lOffset - lLen / 2);

        addObject(new Wall(lLen, thickness), WORLD_WIDTH - lOffset - lLen / 2, WORLD_HEIGHT - lOffset);
        addObject(new Wall(thickness, lLen), WORLD_WIDTH - lOffset, WORLD_HEIGHT - lOffset - lLen / 2);
    }
    
    public int getPlayer1Score()
    {
        return p1Score;
    }
    public int getPlayer2Score()
    {
        return p2Score;
    }
}
