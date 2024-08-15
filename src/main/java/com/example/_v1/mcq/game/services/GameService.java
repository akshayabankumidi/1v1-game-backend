package com.example._v1.mcq.game.services;

import com.example._v1.mcq.game.DataTypes.Custom.Options;
import com.example._v1.mcq.game.DataTypes.Enums.Status;
import com.example._v1.mcq.game.entity.Game;
import com.example._v1.mcq.game.entity.Mcq;
import com.example._v1.mcq.game.respository.GameRepo;
import com.example._v1.mcq.game.respository.McqRepo;
import com.example._v1.mcq.game.utils.GameUpdateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example._v1.mcq.game.DataTypes.Custom.DeleteResult;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepo gameRepo;
    private final McqRepo mcqRepo;
    public Game addGame(Game game) {
        return gameRepo.save(game);
    }

    public Optional<Game> modifyGame(String id, Game game) {
        return gameRepo.findById(id)
                .map(existingGame -> {
                    GameUpdateUtil.updateNonNullFields(game, existingGame);
                    return gameRepo.save(existingGame);
                });
    }

    public DeleteResult deleteGames(List<String> ids) {
        List<String> deletedIds = new ArrayList<>();
        List<String> notFoundIds = new ArrayList<>();

        for (String id : ids) {
            if (gameRepo.existsById(id)) {
                gameRepo.deleteById(id);
                deletedIds.add(id);
            } else {
                notFoundIds.add(id);
            }
        }
        return new DeleteResult(deletedIds, notFoundIds);
    }

    public List<Game> getAllGamesParticpntLessThan2() {
        List<Game> games = new ArrayList<>();
        List<Game> allGames = gameRepo.findAll();
        return allGames;
//        for (Game game : allGames) {
//            int size = 0;
//            if (!game.getParticipants().isEmpty()) {
//                size = game.getParticipants().size();
//            }
//
//            if (size < 2) {
//                games.add(game);
//            }
//        }
//        return games;
    }


    public Game getGame(String id) {
        return gameRepo.findById(id).orElse(null);
    }

    public Game updateGame(Game game) {
        return gameRepo.save(game);
    }

    public Game processAnswer(String gameId, String playerId, String questionId, String answer) {
        Game game = getGame(gameId);
        if (game == null) {
            return null;
        }

        // Update player's score
        Map<String, Integer> scores = game.getScores();
        if (scores == null) {
            scores = new HashMap<>();
            game.setScores(scores);
        }
        Mcq question = mcqRepo.findById(questionId).orElse(null);
        if (question != null && question.isCorrectAnswer(answer)) {
            scores.put(playerId, scores.getOrDefault(playerId, 0) + 1);
        }

        // Move to next question
        game.setCurrentQuestionIndex(game.getCurrentQuestionIndex() + 1);

        // Check if game is over
        List<Mcq> mcqs = mcqRepo.findByGameId(gameId);
        if (game.getCurrentQuestionIndex() >= mcqs.size()) {
            game.setStatus(Status.Completed);
            determineWinner(game);
        }

        return updateGame(game);
    }
    public Mcq getNextQuestion(String gameId) {
        Game game = getGame(gameId);
        if (game == null) {
            return null;
        }
        List<Mcq> mcqs = mcqRepo.findByGameId(gameId);
        if (game.getCurrentQuestionIndex() < mcqs.size()) {
            return mcqs.get(game.getCurrentQuestionIndex());
        }
        return null;
    }
    private void determineWinner(Game game) {
        Map<String, Integer> scores = game.getScores();
        if (scores == null || scores.isEmpty()) {
            return;
        }

        int maxScore = Collections.max(scores.values());
        List<String> winners = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue() == maxScore) {
                winners.add(entry.getKey());
            }
        }

        game.setWinners(winners);
    }


}
