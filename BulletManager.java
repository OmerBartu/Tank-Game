import javafx.animation.PauseTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Random;

/**
 * Represents a generic bullet in the game world.
 * Provides essential methods for updating and accessing bullet properties.
 * Implementing classes should define how bullets behave and interact.
 */
interface Bullet {
    public void update ();
    public double getX ();
    public double getY ();
    public Group getRoot ();
    Node getSprite();
    public Rectangle getHitBox();
}

/**
 * Manages all bullet objects in the game (player and enemy).
 */
public class BulletManager {
    // List holding all active bullets in the game
    private static final ArrayList<Bullet> allBullet = new ArrayList<>();

    private GameWorld gameWorld;
    private Group root;

    /**
     * Initializes the bullet manager with the current game world.
     */
    BulletManager (GameWorld gameWorld) {
        this.gameWorld = gameWorld;
    }

    /**
     * Creates and adds a new player bullet to the game.
     */
    public void createNewPlayerBullet (Direction direction, double x, double y, Group root) {
        allBullet.add(new PlayerBullet(direction, x, y, root));
    }

    /**
     * Creates and adds a new enemy bullet to the game.
     */
    public void createNewEnemyBullet (Direction direction, double x, double y, Group root) {
        allBullet.add(new EnemyBullet(direction, x, y, root));
    }

    /**
     * Creates a small explosion effect at the specified position.
     * Used for bullet-wall collisions.
     */
    public void createSmallExplosion (double x, double y, Group root) {
        ImageView smallExplosion = new ImageView(new Image("file:assets/smallExplosion.png"));
        smallExplosion.setX(x);
        smallExplosion.setY(y);
        this.root = root;
        root.getChildren().add(smallExplosion);

        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
        pause.setOnFinished(e -> root.getChildren().remove(smallExplosion));
        pause.play();
    }

    /**
     * Creates a large explosion effect at the specified position.
     * Used for bullet-tank collisions.
     */
    public void createExplosion (double x, double y, Group root) {
        ImageView explosion = new ImageView(new Image("file:assets/explosion.png"));
        explosion.setX(x-30);
        explosion.setY(y-30);
        root.getChildren().add(explosion);

        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
        pause.setOnFinished(e -> root.getChildren().remove(explosion));
        pause.play();
    }

    /**
     * Removes all bullets from the scene and clears the internal bullet list.
     * Typically called on game reset or restart.
     */
    public void clearBullet () {
        for (Bullet bullet : allBullet) {
            Group bulletRoot = bullet.getRoot();
            bulletRoot.getChildren().remove(bullet.getSprite());
        }
        allBullet.clear();
    }

    /**
     * Updates the state of all bullets in the game.
     * - Moves bullets forward in their direction.
     * - Detects and handles collisions with walls, the player tank, and enemy tanks.
     * - Removes bullets that have collided.
     * - Creates explosions upon collision for visual feedback.
     */
    public void update() {
        // List to store bullets that need to be removed after update
        ArrayList<Bullet> bulletsToDelete = new ArrayList<>();
        // List to store enemy tanks that need to be removed after update
        ArrayList<EnemyTank> tanksToRemove = new ArrayList<>();

        for (Bullet bullet : allBullet) {
            // Move the bullet based on its direction and speed
            bullet.update();

            // Convert bullet's position to map grid coordinates
            int bulletColumn = (int) bullet.getX() / 16 ;
            int bulletRow = (int) bullet.getY() / 16;

            // Check if bullet hits a wall tile
            if (gameWorld.getGameMap().isWall(bulletRow, bulletColumn)) {
                // Remove bullet sprite from the scene
                Group bulletRoot = bullet.getRoot();
                bulletRoot.getChildren().remove(bullet.getSprite());

                // Mark bullet for deletion from the list
                bulletsToDelete.add(bullet);

                // Create a small explosion effect at the impact point
                createSmallExplosion(bullet.getX(), bullet.getY(), bullet.getRoot());
            }

            // Check for collision with the player tank (only for EnemyBullets)
            Shape playerIntersection = Shape.intersect(bullet.getHitBox(), gameWorld.getPlayerTank().getHitBox());
            if (playerIntersection.getBoundsInLocal().getWidth() != -1 && bullet instanceof EnemyBullet) {
                PlayerTank playerTank = gameWorld.getPlayerTank();

                // Reduce player's life
                playerTank.setLife(playerTank.getLife()-1);

                // Reset player tank to starting position
                playerTank.setCoordinates(540, 600);

                // Explosion effect for the hit
                createExplosion(bullet.getX(), bullet.getY(), bullet.getRoot());
            }

            // Check for collisions with each enemy tank (only for PlayerBullets)
            ArrayList<EnemyTank> enemyTanks = gameWorld.getEnemyTanks();
            for (EnemyTank tank : enemyTanks) {
                Shape enemyIntersection = Shape.intersect(bullet.getHitBox(), tank.getHitBox());
                if (enemyIntersection.getBoundsInLocal().getWidth() != -1 && bullet instanceof PlayerBullet) {
                    tanksToRemove.add(tank);
                    tank.remove();

                    // Explosion effect for the hit
                    createExplosion(bullet.getX(), bullet.getY(), bullet.getRoot());

                    // Increase player's score
                    gameWorld.setScore(gameWorld.getScore() + 100);
                }
            }
        }

        // Remove all enemy tanks that were marked for deletion
        for (EnemyTank tank : tanksToRemove) {
            gameWorld.getEnemyTanks().remove(tank);
        }
        // Remove all bullets that were marked for deletion
        for (Bullet bullet : bulletsToDelete) {
            allBullet.remove(bullet);
        }

        // Clear the deletion list just in case (not strictly necessary here)
        bulletsToDelete.clear();
        tanksToRemove.clear();

    }

    public static ArrayList<Bullet> getAllBullet () {
        return allBullet;
    }

    public static void setNewBullet (Bullet bullet) {
        allBullet.add(bullet);
    }
}

/**
 * Represents a bullet fired by the player.
 * Implements the Bullet interface to enable unified handling of all bullets.
 * Handles its own movement and rendering.
 */
class PlayerBullet implements Bullet{
    private Rectangle hitBox;
    private Direction direction;
    private double x, y, speed = 4.0;

    private ImageView sprite;

    private final Group root;

    /**
     * Constructs a new player bullet at the given coordinates, moving in the specified direction.
     * Adds the bullet's sprite to the game root and registers the bullet with the BulletManager.
     *
     * @param direction the direction in which the bullet will move
     * @param x the initial x-coordinate of the bullet
     * @param y the initial y-coordinate of the bullet
     * @param root the root group to which the bullet's sprite will be added
     */
    PlayerBullet (Direction direction, double x, double y, Group root) {
        this.root = root;
        this.hitBox = new Rectangle(13, 10);
        this.x = x;
        this.y = y;
        this.direction = direction;

        this.sprite = new ImageView(new Image("file:assets/bullet.png"));
        sprite.setX(x);
        sprite.setY(y);
        hitBox.setX(x);
        hitBox.setY(y);

        switch (direction) {
            case RIGHT:
                sprite.setRotate(0);
                break;
            case DOWN:
                sprite.setRotate(90);
                break;
            case LEFT:
                sprite.setRotate(180);
                break;
            case UP:
                sprite.setRotate(270);
        }

        root.getChildren().add(this.getSprite());

        BulletManager.setNewBullet(this);
    }

    @Override
    public Node getSprite() {
        return sprite;
    }

    @Override
    public double getX () {
        return x;
    }
    @Override
    public double getY () {
        return y;
    }

    /**
     * Updates the bullet's position based on its direction.
     * Moves the sprite and hitbox accordingly.
     */
    @Override
    public void update() {

        switch (this.direction) {
            case UP:
                y -= speed;
                break;
            case DOWN:
                y += speed;
                break;
            case LEFT:
                x -= speed;
                break;
            case RIGHT:
                x += speed;
        }

        sprite.setX(x);
        sprite.setY(y);
        hitBox.setX(x);
        hitBox.setY(y);

    }

    public Group getRoot() {
        return root;
    }

    public Rectangle getHitBox() {
        return hitBox;
    }

    public void setHitBox(Rectangle hitBox) {
        this.hitBox = hitBox;
    }
}

/**
 * Represents a bullet fired by an enemy tank.
 * Implements the Bullet interface for consistent behavior with other bullet types.
 * Controls its own movement and rendering.
 */
class EnemyBullet implements Bullet{
    private Rectangle hitBox;
    private Direction direction;
    private double x, y, speed = 4.0;

    private ImageView sprite;

    private final Group root;

    /**
     * Constructs a new enemy bullet at the given coordinates, moving in the specified direction.
     * Adds the bullet sprite to the root group and registers it with the BulletManager.
     *
     * @param direction the direction the bullet will travel
     * @param x the initial x-coordinate of the bullet
     * @param y the initial y-coordinate of the bullet
     * @param root the root node where the bullet sprite will be added
     */
    EnemyBullet (Direction direction, double x, double y, Group root) {
        this.root = root;
        this.hitBox = new Rectangle(13, 10);
        this.x = x;
        this.y = y;
        this.direction = direction;

        this.sprite = new ImageView(new Image("file:assets/bullet.png"));
        sprite.setX(x);
        sprite.setY(y);

        switch (direction) {
            case RIGHT:
                sprite.setRotate(0);
                break;
            case DOWN:
                sprite.setRotate(90);
                break;
            case LEFT:
                sprite.setRotate(180);
                break;
            case UP:
                sprite.setRotate(270);
        }

        root.getChildren().add(this.getSprite());

        BulletManager.setNewBullet(this);
    }

    /**
     * Updates the bullet's position based on its direction.
     * Moves the sprite and the hitbox accordingly.
     */
    @Override
    public void update() {

        switch (this.direction) {
            case UP:
                y -= speed;
                break;
            case DOWN:
                y += speed;
                break;
            case LEFT:
                x -= speed;
                break;
            case RIGHT:
                x += speed;
        }

        sprite.setX(x);
        sprite.setY(y);
        hitBox.setX(x);
        hitBox.setY(y);
    }

    @Override
    public double getX () {
        return x;
    }
    @Override
    public double getY () {
        return y;
    }
    @Override
    public Group getRoot() {
        return root;
    }

    @Override
    public Node getSprite() {
        return sprite;
    }

    public Rectangle getHitBox() {
        return hitBox;
    }

    public void setHitBox(Rectangle hitBox) {
        this.hitBox = hitBox;
    }
}
