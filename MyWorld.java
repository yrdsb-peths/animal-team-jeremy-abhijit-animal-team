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
    private static final int RESET_DELAY_FRAMES = 180;
    private static final int MIN_SPAWN_DISTANCE = 220;

    private final Random random = new Random();
    private final List<PlayerBase> players = new ArrayList<>();

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
        buildWalls();
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
