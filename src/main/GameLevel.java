package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class GameLevel {
    private String letters;
    private Set<String> validWords;
    private int level = 1;
    private List<GameLevel> levels;

    public GameLevel(String letters, Set<String> validWords) {
        this.letters = letters;
        this.validWords = new HashSet<>(validWords);
    }

    public String getLetters() {
        return letters;
    }

    public Set<String> getValidWords() {
        return new HashSet<>(validWords);
    }

    public void initializeLevels() {
        levels = new ArrayList<>();
        // Level 1: Easy
        levels.add(new GameLevel("SUGBO",
            new HashSet<>(Arrays.asList("BUGO",
            		"SUGO",
            		"GUSO",
            		"BUSOG",
            		"UBOS",
            		"GUBO")))); // 6 possible words

        // Level 2: Semi-Easy
        levels.add(new GameLevel("KALBUO",
            new HashSet<>(Arrays.asList("BULAK",
            		"KALBU",
            		"KULBA",
            		"BUKAL",
            		"ABO",
            		"KALO",
            		"BOLA",
            		"KUBAL",
            		"BOLA",
            		"BULA")))); // 10 possible words
        // Level 3: Medium
        levels.add(new GameLevel("BALAYAN",
            new HashSet<>(Arrays.asList("LAYA",
            		"BALAY",
            		"LABAN",
            		"ALAY",
            		"BALA",
            		"BALAYAN",
            		"LABA")))); // 6 possible words

        // Level 4: Harder
        levels.add(new GameLevel("KUSOGAN",
            new HashSet<>(Arrays.asList("KUSOG",
            		"KUSOGA",
            		"USOG",
            		"SUKO",
            		"GUSO",
            		"KUSOGAN",
            		"KUGON",
            		"GAKOS",
            		"SUKA")))); // 9 possible words

        // Level 5: Hard
        levels.add(new GameLevel("KAMINGAW",
            new HashSet<>(Arrays.asList("MINGAW",
            		"KAMINGAW",
            		"AGAW",
            		"NAKAW",
            		"KAGAW",
            		"KAWANG",
            		"KAMI",
            		"MINAW")))); // 8 possible words

     // Level 6: (Very Difficult)
        levels.add(new GameLevel("PANAGHIUSA",
            new HashSet<>(Arrays.asList("PANAG",
            		"HIUSA",
            		"PAGHIUSA",
            		"HUSA",
            		"PANA",
            		"GIUSA",
            		"PANAGHIUSA",
            		"USA",
            		"GIUNSA",
            		"GAPAS",
            		"GISA",
            		"USAP",
            		"NIPA",
            		"GANA",
            		"GAHI",
            		"ANAG",
            		"SAPA",
            		"GAPAS",
            		"HANAP",
            		"PUSA",
            		"HAPI")))); // 21 possible words
     // Level 7:(Expert)
        levels.add(new GameLevel("KINATIBUKANO",
            new HashSet<>(Arrays.asList("TINIBUKAN",
            		"KINATIBUKAN",
            		"TIBUOK",
            		"TIBUKAN",
            		"KATIBUKAN",
            		"KATINA",
            		"BUKA", 
            		"BUKOT",
            		"KANA",
            		"KINI",
            		"TABI",
            		"ABO",
            		"TINA",
            		"ANOK",
            		"ANAK",
            		"TIBU"))));


    }

    public List<GameLevel> getLevels() {
        return levels;
    }

    // Check if the word entered by the player matches the word already filled in the boxes
    public boolean isSameAsFilledWord(String inputWord, String filledWord) {
        return inputWord.equalsIgnoreCase(filledWord);
    }

    // Method to compare user's input with valid words
    public boolean isValidWord(String inputWord) {
        return validWords.contains(inputWord.toUpperCase());
    }
}