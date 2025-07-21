import java.util.HashSet;
import javafx.scene.input.KeyCode;

/**
 * Handles keyboard input by tracking currently pressed keys using a HashSet.
 * Provides methods to add, remove, and check the status of keys.
 * Typically used for real-time input handling in games.
 */
public class InputHandler {
    private final HashSet<KeyCode> keysPressed = new HashSet<>();

    public void add(KeyCode key) {
        keysPressed.add(key);
    }

    public void remove(KeyCode key) {
        keysPressed.remove(key);
    }

    public boolean isPressed(KeyCode key) {
        return keysPressed.contains(key);
    }

}