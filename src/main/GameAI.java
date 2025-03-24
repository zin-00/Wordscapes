package main;
import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;
import javax.swing.*;
import java.util.List;

public class GameAI {
    private final Map<String, Integer> hintIndexes;    // Stores the index of the next letter to give as a hint for each word
    private Set<String> validWords;    // Set of valid words that the player must guess
    private int hintsUsed;    // Counter for the number of hints used
    private int currentLevel;    // Current difficulty level of the game (1, 2, or 3)
    private int HINT_TIMER_SECONDS = 60; // Constant for the hint timer duration in seconds
    private boolean hintUsedThisRound = false;    // Flag to track if a hint was used during the current round
    private Map<Integer, WordBox> wordBoxesByLength; // Map to store WordBoxes by length
    private Game game; // Reference to the GameUIV2 instance

    // Constructor to initialize the game AI
    public GameAI() {
        this.hintIndexes = new HashMap<>();
        this.hintsUsed = 0;
        resetHintsCounter();
        this.currentLevel = 1; // Start with level 1 difficulty
    }

    // Reset the hint counter
    public void resetHintsCounter() {
        hintsUsed = 0;
    }

    // Initialize the level with a set of valid words and the selected difficulty level
    public void initializeLevel(Set<String> validWords, int level, Map<Integer, WordBox> wordBoxesByLength) {
        this.validWords = validWords;
        this.hintIndexes.clear(); // Reset the hint index
        this.hintsUsed = 0; // Reset hint usage
        this.currentLevel = level; // Set the difficulty level
        this.wordBoxesByLength = wordBoxesByLength; // Assign the reference to the map
    }

    // Getter for the number of hints used
    public int getHintsUsed() {
        return hintsUsed;
    }

    // Set if a hint was used in the current round
    public void setHintUsedThisRound(boolean used) {
        this.hintUsedThisRound = used;
    }

    // Check if a hint was used in the current round
    public boolean isHintUsedThisRound() {
        return hintUsedThisRound;
    }

    // Set the reference to the GameUI instance
    public void setGame(Game game) {
        this.game = game;
    }

    // Provide the hint and reveal the next letter of a word
    public void provideHint() {
        // Find an unsolved WordBox with an empty word
        WordBox unsolvedWordBox = wordBoxesByLength.values().stream()
            .filter(box -> !box.isFilled()) // Look for boxes that are not filled yet
            .findFirst()
            .orElse(null);

        // If there are no unsolved wordboxes, return
        if (unsolvedWordBox == null) {
            return; // All wordboxes are solved
        }

        // Get the valid words of the same length as the unsolved wordbox
        List<String> wordsOfSameLength = validWords.stream()
            .filter(word -> word.length() == unsolvedWordBox.getWordLength())
            .collect(Collectors.toList());

        // Pick a word from the list of words with the same length as the unsolved box
        String word = wordsOfSameLength.isEmpty() ? "" : wordsOfSameLength.get(0);

        // If no word is found, return
        if (word.isEmpty()) {
            return; // No valid words left
        }

        // Get the next hint for the word
        HintResult hintResult = getNextHint(word);

        if (hintResult != null) {
            // Only reveal the letter if it's not already filled
            if (!unsolvedWordBox.isLetterRevealed(hintResult.getPosition())) {
                unsolvedWordBox.revealLetter(hintResult.getPosition(), hintResult.getLetter());

                // Mark the word as solved if all letters have been revealed
                if (unsolvedWordBox.isFilled()) {
                    wordSolved(word);  // Mark the word as solved
                }

                // Update hint-related tracking
                hintsUsed++;
                game.attempts = hintsUsed;
                game.updateAttemptsDisplay();

                // Subtract time for using a hint
                game.timeRemaining -= 10;
                game.updateTimeDisplay();
            }
        }
    }

    // Get the next hint for the player (returns the next letter in the word)
    public HintResult getNextHint(String word) {
        hintIndexes.putIfAbsent(word, 0); // Initialize the index for the word if it doesn't exist
        int currentIndex = hintIndexes.get(word);
        if (currentIndex < word.length()) {
            // Return the next letter and its position
            hintIndexes.put(word, currentIndex + 1); // Update the hint index for the word
            return new HintResult(word, word.charAt(currentIndex), currentIndex); // Return the hint
        }
        return null;
    }

    // Calculate the score for a word based on its length, hints used, and other factors
    public int calculateScore(String word, boolean usedHint) {
        int basePoints = word.length() * 10; // Basic points based on word length
        if (hintsUsed == 0) {
            basePoints *= 2; // Double points if no hints used
        }
        if (word.length() > 4) {
            basePoints += (word.length() - 4) * 5; // Extra points for words longer than 4 letters
        }
        return basePoints;
    }
    

    // Generate a message based on the player's performance
    public String getPerformanceMessage(int timeRemaining, boolean usedHint) {
        if (hintsUsed == 0 && timeRemaining > HINT_TIMER_SECONDS / 2) {
            return "Excellent! Perfect solve with no hints!";
        } else if (hintsUsed == 0) {
            return "Great job! Solved without hints!";
        } else if (timeRemaining > 0) {
            return "Good work! Keep practicing to solve without hints!";
        } else {
            return "Keep trying! You're making progress!";
        }
    }

    // Remove the word from valid words and increase the difficulty if no hint was used
    public void wordSolved(String word) {
        validWords.remove(word); // Remove the solved word
        if (validWords.isEmpty()) {
            return; // If no valid words are left, end the game
        }

        // After solving a word, select another word of the same length
        List<String> sameLengthWords = validWords.stream()
            .filter(w -> w.length() == word.length()) // Filter words by length
            .collect(Collectors.toList());

        if (!sameLengthWords.isEmpty()) {
            String newWord = sameLengthWords.get(0); // Pick the first word of the same length
            // Reveal the first letter of the new word
            HintResult newHintResult = new HintResult(newWord, newWord.charAt(0), 0);
            WordBox wordBox = wordBoxesByLength.get(newWord.length());
            if (wordBox != null && !wordBox.isFilled()) {
                wordBox.revealLetter(0, newHintResult.getLetter());
                hintIndexes.put(newWord, 1); // Start the hint for the new word
            }
        }
    }

    // Helper class to represent a hint (word and index)
    private static class HintAction {
        String word;
        int index;
        HintAction(String word, int index) {
            this.word = word;
            this.index = index;
        }
    }

    // Result of the hint, including the word, letter, and position of the hint
    public class HintResult {
        private final String word;
        private final char letter;
        private final int position;

        public HintResult(String word, char letter, int position) {
            this.word = word;
            this.letter = letter;
            this.position = position;
        }

        public String getWord() {
            return word;
        }

        public char getLetter() {
            return letter;
        }

        public int getPosition() {
            return position;
        }
    }
}
