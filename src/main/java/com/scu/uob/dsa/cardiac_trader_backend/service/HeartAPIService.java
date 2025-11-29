package com.scu.uob.dsa.cardiac_trader_backend.service;

import com.scu.uob.dsa.cardiac_trader_backend.exception.ExternalAPIException;

/**
 * Service interface for Heart Game API integration
 */
public interface HeartAPIService {
    /**
     * Fetch a heart puzzle from the external API
     * @return HeartPuzzle containing image URL and correct answer
     * @throws ExternalAPIException if API call fails
     */
    HeartPuzzle fetchPuzzle() throws ExternalAPIException;

    /**
     * Inner class representing a heart puzzle
     */
    class HeartPuzzle {
        private String imageUrl;
        private Integer correctAnswer;
        private String puzzleId;

        public HeartPuzzle(String imageUrl, Integer correctAnswer, String puzzleId) {
            this.imageUrl = imageUrl;
            this.correctAnswer = correctAnswer;
            this.puzzleId = puzzleId;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public Integer getCorrectAnswer() {
            return correctAnswer;
        }

        public String getPuzzleId() {
            return puzzleId;
        }
    }
}

