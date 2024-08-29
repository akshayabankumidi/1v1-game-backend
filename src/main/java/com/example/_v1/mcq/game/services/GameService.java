package com.example._v1.mcq.game.services;

import com.example._v1.mcq.game.DataTypes.Custom.Options;
import com.example._v1.mcq.game.DataTypes.Custom.ParticipantInfo;
import com.example._v1.mcq.game.DataTypes.Enums.Status;
import com.example._v1.mcq.game.entity.Game;
import com.example._v1.mcq.game.entity.Mcq;
import com.example._v1.mcq.game.respository.GameRepo;
import com.example._v1.mcq.game.respository.McqRepo;
import com.example._v1.mcq.game.utils.GameUpdateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.example._v1.mcq.game.DataTypes.Custom.DeleteResult;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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
        List<Game> allGames = gameRepo.findAll();
        log.info("allGames: "+ allGames);
        return allGames.stream()
                .filter(game -> game.getParticipants().size() < 2)
                .collect(Collectors.toList());
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

        // Find the participant
        Optional<ParticipantInfo> participantOpt = game.getParticipants().stream()
                .filter(p -> p.getParticipantId().equals(playerId))
                .findFirst();

        if (participantOpt.isPresent()) {
            ParticipantInfo participant = participantOpt.get();

            // Update player's score
            Mcq question = mcqRepo.findById(questionId).orElse(null);
            log.info("question: "+ question);
            if (question != null && isCorrectAnswer(answer,question.getCorrectOptions(),question.getListOfOptions())) {
                log.info("inside update game score");
                log.info("answer: " + answer);
                participant.setScores(participant.getScores() + 1);
            }

            // Move to next question
            participant.setCurrentQuestionIndex(participant.getCurrentQuestionIndex() + 1);

            // Check if game is over for this participant
            List<Mcq> mcqs = mcqRepo.findByGameId(gameId);
            if (participant.getCurrentQuestionIndex() >= mcqs.size()) {
                // This participant has finished
                // Check if all participants have finished
                boolean allFinished = game.getParticipants().stream()
                        .allMatch(p -> p.getCurrentQuestionIndex() >= mcqs.size());
                if (allFinished) {
                    game.setStatus(Status.Completed);
                    determineWinner(game);
                }
            }
        }

        return updateGame(game);
    }
    public Mcq getNextQuestion(String gameId,String participantId) {
        Game game = getGame(gameId);
        if (game == null) {
            return null;
        }

        Optional<ParticipantInfo> participantOpt = game.getParticipants().stream()
                .filter(p -> p.getParticipantId().equals(participantId))
                .findFirst();

        if (participantOpt.isPresent()) {
            ParticipantInfo participant = participantOpt.get();
            List<Mcq> mcqs = mcqRepo.findByGameId(gameId);
            if (participant.getCurrentQuestionIndex() < mcqs.size()) {
                return mcqs.get(participant.getCurrentQuestionIndex());
            }
        }
        return null;
    }
    private void determineWinner(Game game) {
        int maxScore = game.getParticipants().stream()
                .mapToInt(ParticipantInfo::getScores)
                .max()
                .orElse(0);

        List<String> winners = game.getParticipants().stream()
                .filter(p -> p.getScores() == maxScore)
                .map(ParticipantInfo::getParticipantId)
                .collect(Collectors.toList());
    }

    private boolean isCorrectAnswer(String answer,List<Options> correctOptions,List<String> allOptions ) {
        for (Options correctOption : correctOptions) {
            int ind = correctOption.getOption();
            if(answer.equals(allOptions.get(ind))) {
                    return true;
            }
        }
        return false;
    }

}
