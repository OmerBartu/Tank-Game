import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

/**
 * Represents the player's tank in the game.
 * Handles movement, shooting, animation, and collision detection.
 * The tank can move in four directions and shoot bullets based on user input.
 */
public class PlayerTank {
    // Position and movement speed
    private double x, y, speed = 2.0;

    // Handles keyboard input
    private InputHandler input;

    // Visual representation of the tank
    private ImageView sprite;

    // Current direction of the tank
    private Direction tankDirection;

    // Rectangle used for collision detection
    private Rectangle hitBox;

    // Reference to the main game world (for accessing map, bullets, etc.)
    private GameWorld gameWorld;

    // Number of lives the player has
    private int life;

    // Controls the tank's animation frame (switches between sprites)
    private int animationFrameCounter = 0;

    // Tracks when the last bullet was fired (used for cooldown)
    int lastShot = -300;

    // Stores tank sprite images (for animation)
    private final ArrayList<Image> sprites = new ArrayList<>();

    // Reference to the root node of the scene graph (JavaFX)
    private final Group root;

    /**
     * Constructor for PlayerTank.
     * Initializes position, input, graphics, and adds the sprite to the scene.
     */
    public PlayerTank(InputHandler input, Group root, GameWorld gameWorld, int life) {
       this.input = input;
       this.root = root;
       this.gameWorld = gameWorld;

        // Initialize the tank's sprite image
       this.sprite = new ImageView(new Image("file:assets/yellowTank1.png"));

        // Starting position of the tank (center-bottom of the screen)
       this.x = 540;
       this.y = 600;

        // Initial facing direction
       this.tankDirection = Direction.RIGHT;

        // Initialize hitbox at same location and size as the sprite
       this.hitBox = new Rectangle(x, y, 32, 32);

        // Set the number of lives
       this.life = life;

        // Load both animation frames
        sprites.add(new Image("file:assets/yellowTank1.png"));
        sprites.add(new Image("file:assets/yellowTank2.png"));

        // Add the sprite to the JavaFX scene graph
        root.getChildren().add(this.getSprite());
    }


    public Node getSprite() {
        return sprite;
    }

    public void setCoordinates (double x, double y) {
        sprite.setX(x);
        sprite.setY(y);
        hitBox.setX(x);
        hitBox.setY(y);
        this.x = x;
        this.y = y;
    }

    /**
     * Updates the player's tank each frame.
     * Handles input for movement and shooting.
     * Prevents movement through walls and updates sprite animation accordingly.
     */
    public void update() {
        boolean isMoving = false;
        animationFrameCounter++;

        GameMap map = gameWorld.getGameMap();

        // Handle UP movement
        if (input.isPressed(KeyCode.UP)) {

            y -= speed;                 // Move up
            movement(Direction.UP);     // Update direction and sprite
            isMoving = true;

            // Wall collision check (3 horizontal points along top edge)
            int tankColumn1 = (int) (x) / 16;
            int tankColumn2 = (int) (x + 16) / 16;
            int tankColumn3 = (int) (x + 32) / 16;
            int tankRow = (int) (y - 1) / 16;
            if (map.isWall(tankRow, tankColumn1) ||
                    map.isWall(tankRow, tankColumn2) ||
                    map.isWall(tankRow, tankColumn3)) {
                y += speed; // Revert movement if blocked
            }
        }// Handle DOWN movement
        else if (input.isPressed(KeyCode.DOWN)) {
            y += speed;
            movement(Direction.DOWN);
            isMoving = true;

            // Wall collision check (3 horizontal points along bottom edge)
            int tankColumn1 = (int) (x) / 16;
            int tankColumn2 = (int) (x + 16) / 16;
            int tankColumn3 = (int) (x + 32) / 16;
            int tankRow = (int) (y + 32) / 16;
            if (map.isWall(tankRow, tankColumn1) ||
                    map.isWall(tankRow, tankColumn2) ||
                    map.isWall(tankRow, tankColumn3)) {
                y -= speed;
            }
        }// Handle LEFT movement
        else if (input.isPressed(KeyCode.LEFT)) {
            x -= speed;
            movement(Direction.LEFT);
            isMoving = true;

            // Wall collision check (3 vertical points along left edge)
            int tankColumn = (int) (x) / 16;
            int tankRow1 = (int) (y + 32) / 16;
            int tankRow2 = (int) (y) / 16;
            int tankRow3 = (int) (y + 16) / 16;
            if (map.isWall(tankRow1, tankColumn) ||
                    map.isWall(tankRow2, tankColumn) ||
                    map.isWall(tankRow3, tankColumn)) {
                x += speed;
            }
        }// Handle RIGHT movement
        else if (input.isPressed(KeyCode.RIGHT)) {
            x += speed;
            movement(Direction.RIGHT);
            isMoving = true;

            // Wall collision check (3 vertical points along right edge)
            int tankColumn = (int) (x + 32) / 16;
            int tankRow1 = (int) (y + 32) / 16;
            int tankRow2 = (int) (y + 8) / 16;
            int tankRow3 = (int) (y + 23) / 16;
            if (map.isWall(tankRow1, tankColumn) ||
                    map.isWall(tankRow2, tankColumn) ||
                    map.isWall(tankRow3, tankColumn)) {
                x -= speed;
            }
        }
        // Reset animation frame when idle
        if (!isMoving) {
            animationFrameCounter = 0;
        }

        // Fire bullet if X key is pressed and enough time has passed
        if (input.isPressed(KeyCode.X) && (gameWorld.frame - lastShot) > 10) {
            lastShot = gameWorld.frame;
            gameWorld.getBulletManager().createNewPlayerBullet(tankDirection, x+11, y+11, root);
        }

        // Update hitbox and sprite position
        hitBox.setX(x);
        hitBox.setY(y);
        sprite.setX(x);
        sprite.setY(y);
    }

    /**
     * Updates the player's tank sprite based on movement direction.
     * Controls animation frame and sprite rotation to reflect current direction.
     *
     * @param direction The direction in which the tank is intended to move.
     */
    private void movement(Direction direction) {
        // Set the tank sprite for animation (alternates every 20 frames)
        sprite.setImage(sprites.get((animationFrameCounter / 20) % 2));

        // Adjust tank's direction and rotate the sprite accordingly
        switch (direction) {
            case RIGHT:
                this.tankDirection = Direction.RIGHT;
                sprite.setRotate(0);
                break;
            case DOWN:
                this.tankDirection = Direction.DOWN;
                sprite.setRotate(90);
                break;
            case LEFT:
                this.tankDirection = Direction.LEFT;
                sprite.setRotate(180);
                break;
            case UP:
                this.tankDirection = Direction.UP;
                sprite.setRotate(270);
        }
    }

    public Rectangle getHitBox() {
        return hitBox;
    }

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public int getX() {
        return (int) x;
    }
    public int getY() {
        return (int) y;
    }
}
