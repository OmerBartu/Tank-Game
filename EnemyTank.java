import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Random;

/**
 * Represents an enemy tank in the game.
 * Handles initialization, sprite setup, direction configuration,
 * and initial positioning of the tank.
 */
public class EnemyTank {

    // Possible movement directions for the tank
    private Direction[] directions;

    // Tank position and movement speed
    private double x, y, speed = 2.0;

    // Visual representation of the tank
    private ImageView sprite;

    // Current movement direction of the tank
    private Direction tankDirection;

    // Rectangle used for collision detection
    private Rectangle hitBox;

    // Reference to the game world (used for map, bullet creation, etc.)
    private GameWorld gameWorld;

    // Used for sprite animation
    private int animationFrameCounter = 0;

    // Frame when the last shot was fired
    private int lastShot = 0;

    // Randomizer for movement and shooting
    private Random moveRandomizer;

    // List of tank images for animation
    private final ArrayList<Image> sprites = new ArrayList<>();

    // The root node to which the sprite is added
    private final Group root;

    /**
     * Constructs a new EnemyTank and initializes its position,
     * direction options, sprite images, and adds it to the scene.
     *
     * @param root The root group of the JavaFX scene graph
     * @param gameWorld Reference to the main game world
     */
    public EnemyTank(Group root, GameWorld gameWorld) {
        this.root = root;
        this.gameWorld = gameWorld;

        // Load default tank sprite
        this.sprite = new ImageView(new Image("file:assets/whiteTank1.png"));

        // Initialize the random number generator
        this.moveRandomizer = new Random();

        // Set a random starting position within bounds
        this.x = 30 + 1000 * moveRandomizer.nextDouble();
        this.y = 20 + 180 * moveRandomizer.nextDouble();

        // Set initial direction
        this.tankDirection = Direction.RIGHT;

        // Initialize collision hitbox
        this.hitBox = new Rectangle(x, y, 32, 32);

        // Define movement directions
        this.directions = new Direction[4];
        directions[0] = Direction.LEFT;
        directions[1] = Direction.RIGHT;
        directions[2] = Direction.UP;
        directions[3] = Direction.DOWN;

        // Load sprite images for animation
        sprites.add(new Image("file:assets/whiteTank1.png"));
        sprites.add(new Image("file:assets/whiteTank2.png"));

        // Set initial sprite position
        sprite.setX(x);
        sprite.setY(y);
        hitBox.setX(x);
        hitBox.setY(y);

        // Add the tank's sprite to the JavaFX scene
        root.getChildren().add(this.getSprite());
    }

    public Node getSprite() {
        return sprite;
    }

    /**
     * Updates the enemy tank's state every frame.
     * Randomly changes direction every second (every 60 frames),
     * moves the tank in the current direction, and randomly fires bullets
     * if at least 60 frames have passed since the last shot.
     */
    public void update() {

        // Change direction randomly every 60 frames (approximately once per second)
        if (gameWorld.frame % 60 == 0) {
            tankDirection = directions[moveRandomizer.nextInt(4)];
        }

        // Move in the current direction
        movement(tankDirection);

        // Fire a bullet randomly if at least 60 frames have passed since last shot
        if ((gameWorld.frame - lastShot) >= 60 && moveRandomizer.nextInt() % 30 == 0) {
            lastShot = gameWorld.frame;
            gameWorld.getBulletManager().createNewEnemyBullet(tankDirection, x+11, y+11, root);
        }
    }

    /**
     * Moves the enemy tank in the specified direction.
     * Handles sprite orientation, updates the tank's position,
     * and performs wall collision detection using the game map.
     *
     * @param direction The direction in which the tank should move.
     */
    private void movement(Direction direction) {
        // Change animation frame based on movement
        sprite.setImage(sprites.get((animationFrameCounter / 20) % 2));

        GameMap map = gameWorld.getGameMap();
        int tankColumn = (int) x;
        int tankRow = (int)y;
        int tankRow1, tankRow2, tankRow3;
        int tankColumn1, tankColumn2, tankColumn3;

        switch (direction) {
            case RIGHT:
                this.tankDirection = Direction.RIGHT;
                sprite.setRotate(0);
                x+=speed;

                // Check right edge for wall collisions
                tankColumn = (int) (x+32) / 16 ;
                tankRow1 = (int) (y+32) / 16;
                tankRow2 = (int) (y) / 16;
                tankRow3 = (int) (y+16) / 16;

                if (map.isWall(tankRow1, tankColumn) ||
                        map.isWall(tankRow2, tankColumn) ||
                        map.isWall(tankRow3, tankColumn)) {
                    x -= speed; // Undo movement on collision
                }
                break;
            case DOWN:
                this.tankDirection = Direction.DOWN;
                sprite.setRotate(90);
                y+=speed;

                // Check bottom edge for wall collisions
                tankColumn1 = (int) x / 16;
                tankColumn2 = (int) (x + 16) / 16;
                tankColumn3 = (int) (x + 32) / 16;
                tankRow = (int) (y + 32) / 16;

                if (map.isWall(tankRow, tankColumn1) ||
                        map.isWall(tankRow, tankColumn2) ||
                        map.isWall(tankRow, tankColumn3)) {
                    y -= speed;
                }
                break;
            case LEFT:
                this.tankDirection = Direction.LEFT;
                sprite.setRotate(180);
                x-=speed;

                // Check left edge for wall collisions
                tankColumn = (int) x / 16;
                tankRow1 = (int) (y + 32) / 16;
                tankRow2 = (int) y / 16;
                tankRow3 = (int) (y + 16) / 16;

                if (map.isWall(tankRow1, tankColumn) ||
                        map.isWall(tankRow2, tankColumn) ||
                        map.isWall(tankRow3, tankColumn)) {
                    x += speed;
                }
                break;
            case UP:
                this.tankDirection = Direction.UP;
                sprite.setRotate(270);
                y-=speed;

                // Check top edge for wall collisions
                tankColumn1 = (int) x / 16;
                tankColumn2 = (int) (x + 16) / 16;
                tankColumn3 = (int) (x + 32) / 16;
                tankRow = (int) (y - 1) / 16;

                if (map.isWall(tankRow, tankColumn1) ||
                        map.isWall(tankRow, tankColumn2) ||
                        map.isWall(tankRow, tankColumn3)) {
                    y += speed;
                }
                break;
        }

        // Update sprite and hitbox positions
        sprite.setX(x);
        sprite.setY(y);
        hitBox.setX(x);
        hitBox.setY(y);
    }
    public Rectangle getHitBox() {
        return hitBox;
    }

    public void setCoordinates (double x, double y) {
        sprite.setX(x);
        sprite.setY(y);
        hitBox.setX(x);
        hitBox.setY(y);
        this.x = x;
        this.y = y;
    }

    public void remove() {
        root.getChildren().remove(this.sprite);
    }
}
