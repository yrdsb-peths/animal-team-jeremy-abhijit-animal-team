import greenfoot.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyWorld extends World
{
    private static final int WORLD_WIDTH = 1000;
    private static final int WORLD_HEIGHT = 700;
    private static final int ROUND_LENGTH_MS = 120000;
    private static final int MAX_ROUNDS = 5;
    private static final int SHRINK_INTERVAL_MS = 20000;
    private static final int SHRINK_STEP = 24;
    private static final int MAX_SHRINK_LEVEL = 5;
    private static final int RESET_DELAY_FRAMES = 180;
    private static final int MIN_SPAWN_DISTANCE = 220;

    private final Random random = new Random();
    private final List<PlayerBase> players = new ArrayList<>();
    private final List<Wall> walls = new ArrayList<>();

    private TimerDisplay timerDisplay;
    private Label player1ScoreLabel;
    private Label player2ScoreLabel;
    private Label roundLabel;
    private EndMessage endMessage;
    private Player1 player1;
    private long roundStartMs;
    private boolean roundEnding;
    private boolean matchOver;
    private int resetCountdown;
    private int roundsPlayed;
    private int currentShrinkLevel;
    
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
        setPaintOrder(EndMessage.class, Label.class, NameTag.class, Explosion.class, PlayerBase.class, Wall.class);
        setupRound();
    }

    public void act()
    {
        if (matchOver)
        {
            return;
        }

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
        if (player1ScoreLabel == null)
        {
            player1ScoreLabel = new Label("", 24);
            addObject(player1ScoreLabel, 120, 30);
        }
        if (player2ScoreLabel == null)
        {
            player2ScoreLabel = new Label("", 24);
            addObject(player2ScoreLabel, WORLD_WIDTH - 120, 30);
        }
        if (roundLabel == null)
        {
            roundLabel = new Label("", 20);
            addObject(roundLabel, WORLD_WIDTH / 2, 70);
        }

        applyShrinkLevel(0);
        spawnPlayers();
        pickBomber();
        roundStartMs = System.currentTimeMillis();
        roundEnding = false;
        resetCountdown = 0;
        updateScoreLabels();
        updateTimer();
    }

    private void updateTimer()
    {
        int elapsedMs = (int)(System.currentTimeMillis() - roundStartMs);
        updateMapShrink(elapsedMs);

        int remainingMs = ROUND_LENGTH_MS - elapsedMs;
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

    private void updateMapShrink(int elapsedMs)
    {
        int shrinkLevel = Math.min(MAX_SHRINK_LEVEL, Math.max(0, elapsedMs / SHRINK_INTERVAL_MS));
        if (shrinkLevel != currentShrinkLevel)
        {
            applyShrinkLevel(shrinkLevel);
        }
    }

    private void applyShrinkLevel(int shrinkLevel)
    {
        currentShrinkLevel = shrinkLevel;
        rebuildWalls();
        movePlayersOutOfWalls();
    }

    private void triggerExplosion()
    {
        roundEnding = true;
        PlayerBase explodedPlayer = getBomber();
        awardRoundWin(explodedPlayer);
        roundsPlayed++;
        updateScoreLabels();

        if (roundsPlayed >= MAX_ROUNDS)
        {
            matchOver = true;
            resetCountdown = 0;
            showEndMessage(getMatchWinnerText(), getMatchDetail());
        }
        else
        {
            resetCountdown = RESET_DELAY_FRAMES;
            showEndMessage(getWinnerText(explodedPlayer), getRoundDetail(explodedPlayer));
        }
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
        if (!matchOver)
        {
            setupRound();
        }
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

    private void awardRoundWin(PlayerBase explodedPlayer)
    {
        if (explodedPlayer instanceof Player1)
        {
            p2Score++;
        }
        else if (explodedPlayer instanceof Player2)
        {
            p1Score++;
        }
    }

    private String getRoundDetail(PlayerBase explodedPlayer)
    {
        if (explodedPlayer instanceof Player1)
        {
            return "Player 1 blew up when the 2-minute timer ended.";
        }
        if (explodedPlayer instanceof Player2)
        {
            return "Player 2 blew up when the 2-minute timer ended.";
        }
        return "The player holding the bomb loses when time expires.";
    }

    private String getMatchWinnerText()
    {
        if (p1Score > p2Score)
        {
            return "Player 1 Wins The Match!";
        }
        if (p2Score > p1Score)
        {
            return "Player 2 Wins The Match!";
        }
        return "Match Tied!";
    }

    private String getMatchDetail()
    {
        return "Final score after 5 games: " + p1Score + " - " + p2Score;
    }

    private void updateScoreLabels()
    {
        if (player1ScoreLabel != null)
        {
            player1ScoreLabel.setValue("P1: " + p1Score);
        }
        if (player2ScoreLabel != null)
        {
            player2ScoreLabel.setValue("P2: " + p2Score);
        }
        if (roundLabel != null)
        {
            int currentRound = Math.min(roundsPlayed + 1, MAX_ROUNDS);
            if (matchOver)
            {
                roundLabel.setValue("Match Complete");
            }
            else
            {
                roundLabel.setValue("Game " + currentRound + " / " + MAX_ROUNDS);
            }
        }
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

    private void rebuildWalls()
    {
        int thickness = 28;
        int borderGap = 160;
        int inset = currentShrinkLevel * SHRINK_STEP;

        removeObjects(walls);
        walls.clear();

        int topBottomSegment = (WORLD_WIDTH - (inset * 2) - borderGap) / 2;
        addWall(topBottomSegment, thickness, inset + topBottomSegment / 2, inset + thickness / 2);
        addWall(topBottomSegment, thickness, WORLD_WIDTH - inset - topBottomSegment / 2, inset + thickness / 2);
        addWall(topBottomSegment, thickness, inset + topBottomSegment / 2, WORLD_HEIGHT - inset - thickness / 2);
        addWall(topBottomSegment, thickness, WORLD_WIDTH - inset - topBottomSegment / 2, WORLD_HEIGHT - inset - thickness / 2);

        int sideSegment = (WORLD_HEIGHT - (inset * 2) - borderGap) / 2;
        addWall(thickness, sideSegment, inset + thickness / 2, inset + sideSegment / 2);
        addWall(thickness, sideSegment, inset + thickness / 2, WORLD_HEIGHT - inset - sideSegment / 2);
        addWall(thickness, sideSegment, WORLD_WIDTH - inset - thickness / 2, inset + sideSegment / 2);
        addWall(thickness, sideSegment, WORLD_WIDTH - inset - thickness / 2, WORLD_HEIGHT - inset - sideSegment / 2);

        int lLen = 130;
        int lOffset = 140;

        addWall(lLen, thickness, inset + lOffset + lLen / 2, inset + lOffset);
        addWall(thickness, lLen, inset + lOffset, inset + lOffset + lLen / 2);

        addWall(lLen, thickness, WORLD_WIDTH - inset - lOffset - lLen / 2, inset + lOffset);
        addWall(thickness, lLen, WORLD_WIDTH - inset - lOffset, inset + lOffset + lLen / 2);

        addWall(lLen, thickness, inset + lOffset + lLen / 2, WORLD_HEIGHT - inset - lOffset);
        addWall(thickness, lLen, inset + lOffset, WORLD_HEIGHT - inset - lOffset - lLen / 2);

        addWall(lLen, thickness, WORLD_WIDTH - inset - lOffset - lLen / 2, WORLD_HEIGHT - inset - lOffset);
        addWall(thickness, lLen, WORLD_WIDTH - inset - lOffset, WORLD_HEIGHT - inset - lOffset - lLen / 2);
    }

    private void addWall(int width, int height, int x, int y)
    {
        Wall wall = new Wall(width, height);
        walls.add(wall);
        addObject(wall, x, y);
    }

    private void movePlayersOutOfWalls()
    {
        for (PlayerBase player : getObjects(PlayerBase.class))
        {
            movePlayerTowardCenter(player);
        }
    }

    private void movePlayerTowardCenter(PlayerBase player)
    {
        if (player == null || player.getWorld() == null || !player.overlapsWall())
        {
            return;
        }

        int x = player.getX();
        int y = player.getY();
        int centerX = WORLD_WIDTH / 2;
        int centerY = WORLD_HEIGHT / 2;

        for (int i = 0; i < 240 && player.overlapsWall(); i++)
        {
            if (x < centerX)
            {
                x++;
            }
            else if (x > centerX)
            {
                x--;
            }

            if (y < centerY)
            {
                y++;
            }
            else if (y > centerY)
            {
                y--;
            }

            player.setLocation(x, y);
        }
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
