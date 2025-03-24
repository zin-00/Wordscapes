package main;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundManager {
    private Clip backgroundMusic;
    private Clip correctSound;
    private Clip incorrectSound;
    private Clip neutralSound;
    private Clip gameBackgroundMusic;

    public SoundManager() {
        loadSounds();
    }

    private void loadSounds() {
        try {
            // Attempt to load sounds, but don't throw exceptions that might prevent menu from showing
            backgroundMusic = loadClip("res/sounds/game-music-loopv1.wav");
            gameBackgroundMusic = loadClip("res/sounds/school-glock.wav");
            correctSound = loadClip("res/sounds/correct.wav");
            incorrectSound = loadClip("res/sounds/incorrect.wav");
            neutralSound = loadClip("res/sounds/sound1.wav");
        } catch (Exception e) {
            System.out.println("Could not load all sounds: " + e.getMessage());
        }
    }

    private Clip loadClip(String filePath) {
        try {
            File soundFile = new File(filePath);
            if (!soundFile.exists()) {
                System.out.println("Sound file not found: " + filePath);
                return null;
            }
            
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            return clip;
        } catch (Exception e) {
            System.out.println("Error loading sound from " + filePath + ": " + e.getMessage());
            return null;
        }
    }

    public void playBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }
    
    public void playGameBackgroundMusic() {
        if (gameBackgroundMusic != null) {
            gameBackgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }
    private void playSound(Clip clip) {
        if (clip != null) {
            clip.setFramePosition(0);
            clip.start();
        }
    }
}