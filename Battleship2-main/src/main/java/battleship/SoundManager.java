
package battleship;
import javazoom.jl.player.Player;
import java.io.InputStream;

public class SoundManager {

    private SoundManager() {
        // Utility class - no instances
    }

    public static void playHit() {
        play("hit.mp3");
    }

    private static void play(String soundFile) {
        Thread soundThread = new Thread(() -> {
            try {
                InputStream sound = SoundManager.class
                        .getResourceAsStream("/sounds/" + soundFile);

                if (sound == null) {
                    System.err.println("Sound file not found: " + soundFile);
                    return;
                }

                Player player = new Player(sound);
                player.play();

            } catch (Exception e) {
                System.err.println("Error playing sound: " + soundFile);
            }
        });

        soundThread.start();
    }
}

