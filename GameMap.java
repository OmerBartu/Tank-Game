import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Represents the tile-based game map including walls and empty spaces.
 * Handles generation and rendering of the map.
 */
public class GameMap {
    private final int TILE_SIZE = 16;
    private final int ROWS = 45;
    private final int COLS = 67;

    private final int[][] map;
    private final Group root;
    private final Image wallImage;
    private GameWorld gameWorld;


    /**
     * Initializes the game map with predefined borders and internal walls.
     */
    public GameMap(Group root, GameWorld gameWorld) {
        this.root = root;
        this.gameWorld = gameWorld;
        this.wallImage = new Image("file:assets/wall.png");
        this.map = new int[ROWS][COLS];
        generateBorders();
        render();
    }

    /**
     * Fills the map array with walls on the borders and two horizontal inner walls.
     */
    private void generateBorders() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (row == 0 || row == ROWS - 1 || col == 0 || col == COLS - 1) {
                    map[row][col] = 1; //wall
                } else {
                    map[row][col] = 0; //empty
                }
            }
        }

        // Two horizontal walls across the middle of the map
        for (int col = 10; col < 57; col++) {
            map[15][col] = 1;
            map[30][col] = 1;
        }
    }

    /**
     * Renders wall tiles based on the map array.
     */
    private void render() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (map[row][col] == 1) {
                    ImageView wallView = new ImageView(wallImage);
                    wallView.setFitWidth(TILE_SIZE);
                    wallView.setFitHeight(TILE_SIZE);
                    wallView.setX(col * TILE_SIZE);
                    wallView.setY(row * TILE_SIZE);
                    root.getChildren().add(wallView);
                }
            }
        }
    }

    public int[][] getMap() {
        return this.map;
    }

    public boolean isWall(int row, int col) {
        return this.map[row][col] == 1;
    }
}