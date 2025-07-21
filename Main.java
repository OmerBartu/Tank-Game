import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Main extends Application{
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Entry point for the JavaFX application.
     * Initializes the game scene, UI layers, input handling, and starts the game loop.
     *
     * @param stage the primary stage for this application.
     * @throws Exception if any error occurs during initialization.
     */
    @Override
    public void start(Stage stage) throws Exception {

        // Create UI and game layers
        Pane uiLayer = new Pane();                  // Layer for UI elements like labels
        Group root = new Group();                   // Layer for game objects
        Group window = new Group(root, uiLayer);    // Combined root for the Scene

        // Create the main game scene
        Scene scene = new Scene(window, 1080, 720, Color.BLACK);


        // Set stage properties
        stage.setTitle("Tank Game");
        stage.setResizable(false);

        // Create "Pause" text
        Label pauseText = new Label("Game Paused");
        pauseText.setFont(Font.font("Monospaced", FontWeight.BOLD, 36));
        pauseText.setTextFill(Color.RED);
        pauseText.setLayoutX(480);
        pauseText.setLayoutY(300);

        // Create "Restart" text shown during pause
        Label restartText = new Label("Restart? (R)");
        restartText.setFont(Font.font("Monospaced", FontWeight.BOLD, 36));
        restartText.setTextFill(Color.WHITE);
        restartText.setLayoutX(480);
        restartText.setLayoutY(330);

        // Pause state flag
        boolean[] pause = {false};

        // Input handling
        InputHandler inputHandler = new InputHandler();
        scene.setOnKeyPressed(event -> inputHandler.add(event.getCode()));
        scene.setOnKeyReleased(event -> {
            inputHandler.remove(event.getCode());

            // Toggle pause with 'P' key
            if (event.getCode() == KeyCode.P) {
                if (pause[0]) {
                    uiLayer.getChildren().remove(pauseText);
                    uiLayer.getChildren().remove(restartText);
                } else {
                    uiLayer.getChildren().add(pauseText);
                    uiLayer.getChildren().add(restartText);
                }
                pause[0] = !pause[0];
            }
        });


        // Initialize the game world
        final GameWorld[] gameWorld = {new GameWorld(root, inputHandler)};

        // Score display
        Label scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 30));
        scoreLabel.setTextFill(Color.GRAY);
        scoreLabel.setLayoutX(15);
        scoreLabel.setLayoutY(10);
        uiLayer.getChildren().add(scoreLabel);

        // Life display
        Label lifeLabel = new Label("Life: 3");
        lifeLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 30));
        lifeLabel.setTextFill(Color.GREEN);
        lifeLabel.setLayoutX(15);
        lifeLabel.setLayoutY(45);
        uiLayer.getChildren().add(lifeLabel);

        // Center the game view on the player's tank
        double offsetX = 1080 / 2 - gameWorld[0].getPlayerTank().getX();
        double offsetY = 720 / 2 - gameWorld[0].getPlayerTank().getY();
        root.setTranslateX(offsetX);
        root.setTranslateY(offsetY);

        final Boolean[] gameOver = {false};

        // Main game loop using AnimationTimer
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!gameOver[0]) {
                    if (pause[0]) {
                        // Restart while paused
                        if (inputHandler.isPressed(KeyCode.R)) {
                            root.getChildren().clear();
                            uiLayer.getChildren().clear();
                            gameWorld[0] = new GameWorld(root, inputHandler);
                            gameOver[0] = false;


                            gameWorld[0].getBulletManager().clearBullet();
                            uiLayer.getChildren().addAll(scoreLabel, lifeLabel);
                            scoreLabel.setText("Score: 0");
                            lifeLabel.setText("Life: 3" );

                            pause[0] = false;
                        }
                        if (inputHandler.isPressed(KeyCode.ESCAPE)) {
                            Platform.exit();
                        }
                    } else {
                        // Update game logic
                        gameWorld[0].update();

                        // Update UI elements
                        scoreLabel.setText("Score: " + gameWorld[0].getScore());
                        lifeLabel.setText("Life: " + gameWorld[0].getPlayerTank().getLife());

                        // Re-center camera on player tank
                        double offsetX = 1080 / 2 - gameWorld[0].getPlayerTank().getX();
                        double offsetY = 720 / 2 - gameWorld[0].getPlayerTank().getY();
                        root.setTranslateX(offsetX);
                        root.setTranslateY(offsetY);

                        // Check for game over
                        if (gameWorld[0].getPlayerTank().getLife() <= 0) {
                            gameOver[0] = true;

                            root.getChildren().clear();

                            // Show "Game Over" message
                            Label gameOverLabel = new Label("GAME OVER\nPress R to Restart");
                            gameOverLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 48));
                            gameOverLabel.setTextFill(Color.RED);
                            gameOverLabel.setLayoutX(300);
                            gameOverLabel.setLayoutY(300);
                            uiLayer.getChildren().add(gameOverLabel);

                            // Show final score
                            Label score = new Label("Your Score is " + gameWorld[0].getScore());
                            score.setFont(Font.font("Monospaced", FontWeight.BOLD, 48));
                            score.setTextFill(Color.GREEN);
                            score.setLayoutX(300);
                            score.setLayoutY(100);
                            uiLayer.getChildren().add(score);
                        }
                    }

                } else {
                    // Restart after game over
                    if (inputHandler.isPressed(KeyCode.R)) {
                        root.getChildren().clear();
                        uiLayer.getChildren().clear();
                        gameWorld[0] = new GameWorld(root, inputHandler);
                        gameOver[0] = false;

                        gameWorld[0].getBulletManager().clearBullet();
                        uiLayer.getChildren().addAll(scoreLabel, lifeLabel);
                        scoreLabel.setText("Score: 0");
                        lifeLabel.setText("Life: 3" );
                    }
                }
            }
        };
        gameLoop.start();

        // Show the window
        stage.setScene(scene);
        stage.show();
    }
}