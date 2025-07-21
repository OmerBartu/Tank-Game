import javafx.scene.Group;

import java.util.ArrayList;
import java.util.Random;

/**
 * Main game controller class that manages the core components of the game,
 * including player tank, enemy tanks, bullets, and the game map.
 */
public class GameWorld {
    private PlayerTank playerTank;
    private BulletManager bulletManager;
    private GameMap gameMap;
    private ArrayList<EnemyTank> enemyTanks = new ArrayList<>();
    public int frame = 0;
    private int score = 0;

    private Group root;
    private InputHandler handler;

    /**
     * Initializes all major game components and entities.
     *
     * @param root    the JavaFX root node for rendering game objects
     * @param handler the input handler for player controls
     */
    public GameWorld(Group root, InputHandler handler) {
        bulletManager = new BulletManager(this);
        gameMap = new GameMap(root, this);
        playerTank = new PlayerTank(handler, root, this, 3);

        this.root = root;
        this.handler = handler;

    }

    /**
     * Updates the game state on each frame.
     * Spawns new enemy tanks randomly, updates all enemy tanks,
     * the player tank, and all bullets if the player is still alive.
     */
    public void update() {
        Random randomizer = new Random();

        // Only update the game if the player is alive
        if (playerTank.getLife() > 0) {
            // Randomly spawn a new enemy tank every ~240 frames
            if (randomizer.nextInt() % 240 == 0) {
                enemyTanks.add(new EnemyTank(root, this));
            }

            // Update each enemy tank
            for (EnemyTank enemyTank : enemyTanks) {
                enemyTank.update();
            }

            // Update player tank
            playerTank.update();

            // Update all bullets (player and enemy)
            bulletManager.update();
        }
        frame++;
    }

    // Getter/setter methods
    public GameMap getGameMap() {
        return gameMap;
    }

    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    public PlayerTank getPlayerTank () {
        return playerTank;
    }

    public ArrayList<EnemyTank> getEnemyTanks () {
        return  enemyTanks;
    }

    public void setPlayerTank (PlayerTank tank) {
        this.playerTank = tank;
    }

    public BulletManager getBulletManager () {
        return bulletManager;
    }

    public void increaseScore () {
        score += 100;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}