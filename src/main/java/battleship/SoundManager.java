
package battleship;
import javazoom.jl.player.Player;
import java.io.InputStream;

public class SoundManager {

    private SoundManager() {
        // Utility class - no instances
    }

    public static void playHit() {
        play();
    }

    private static void play() {
        Thread soundThread = new Thread(() -> {
            try {
                InputStream sound = SoundManager.class
                        .getResourceAsStream("/sounds/" + "hit.mp3");

                if (sound == null) {
                    System.err.println("Sound file not found: " + "hit.mp3");
                    return;
                }

                Player player = new Player(sound);
                player.play();

            } catch (Exception e) {
                System.err.println("Error playing sound: " + "hit.mp3");
            }
        });

        soundThread.start();
    }
}

