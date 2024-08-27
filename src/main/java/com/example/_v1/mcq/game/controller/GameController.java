package com.example._v1.mcq.game.controller;

import com.example._v1.mcq.game.DTO.AnswerRequest;
import com.example._v1.mcq.game.DTO.DeleteRequest;
import com.example._v1.mcq.game.DTO.GameRequest;
import com.example._v1.mcq.game.DataTypes.Custom.DeleteResult;
import com.example._v1.mcq.game.DataTypes.Custom.GameData;
import com.example._v1.mcq.game.DataTypes.Custom.GameMessage;
import com.example._v1.mcq.game.DataTypes.Enums.Status;
import com.example._v1.mcq.game.entity.Game;
import com.example._v1.mcq.game.entity.Mcq;
import com.example._v1.mcq.game.services.GameService;
import com.example._v1.mcq.game.services.McqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;
    private final McqService mcqService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/add")
    public ResponseEntity<?> addGame(@RequestBody Game game) {
        try {
            Game savedGame = gameService.addGame(game);
            return ResponseEntity.ok(savedGame);
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/modify/{id}")
    public ResponseEntity<?> modifyGame(@PathVariable String id, @RequestBody Game game) {
        try {
            return gameService.modifyGame(id, game)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteGame(@RequestBody DeleteRequest deleteRequest) {
        try {
            DeleteResult result = gameService.deleteGames(deleteRequest.getIds());
            StringBuilder responseMessage = new StringBuilder("Operation completed");
            if(!result.deletedIds.isEmpty()) {
                responseMessage.append(" deleted ids: ").append(result.deletedIds).append(".");
            }
            if(!result.notFoundIds.isEmpty()) {
                responseMessage.append(" not found ids: ").append(result.notFoundIds).append(".");
            }
            return ResponseEntity.ok(responseMessage.toString());
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getAllGamesPL2")
    public ResponseEntity<?> getAllGamesPL2() {
        try {
            return ResponseEntity.ok(gameService.getAllGamesParticpntLessThan2());
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @MessageMapping("/game/join")
    @SendToUser("/queue/game")
    public GameMessage joinGame(GameRequest gameRequest) {
        log.info("Join game request received for game: {}, player: {}", gameRequest.getGameId(), gameRequest.getPlayerId());
        Game game = gameService.getGame(gameRequest.getGameId());
        if (game == null) {
            log.error("Game not found: {}", gameRequest.getGameId());
            return new GameMessage("ERROR", "Game not found");
        }

        if (game.getParticipants() == null) {
            game.setParticipants(new ArrayList<>());
        }
        if (!game.getParticipants().contains(gameRequest.getPlayerId())) {
            game.getParticipants().add(gameRequest.getPlayerId());
        }
        game = gameService.updateGame(game);

        log.info("Updated game after join: {}", game);

        if (game.getParticipants().size() == 2) {
            game.setStatus(Status.InProgress);
            game = gameService.updateGame(game);

            List<Mcq> mcqs = mcqService.getMcqsByGameId(game.getId());
            Mcq firstQuestion = mcqs.get(0);
            log.info(firstQuestion.toString());
            GameData gameData = new GameData(game, mcqs);
            GameMessage gameStartedMessage = new GameMessage("GAME_STARTED", gameData);

            game.getParticipants().forEach(playerId -> {
                messagingTemplate.convertAndSendToUser(playerId, "/queue/game", gameStartedMessage);
                log.info("Sent GAME_STARTED message to player: {}", playerId);
            });

            GameMessage questionMessage = new GameMessage("QUESTION", firstQuestion);
            game.getParticipants().forEach(playerId -> {
                messagingTemplate.convertAndSendToUser(playerId, "/queue/game", questionMessage);
                log.info("Sent QUESTION message to player: {}", playerId);
            });

            return gameStartedMessage;
        } else {
            List<Mcq> mcqs = mcqService.getMcqsByGameId(game.getId());
            GameData gameData = new GameData(game, mcqs);
            return new GameMessage("WAITING_FOR_PLAYERS", gameData);
        }
    }

    @MessageMapping("/game/answer")
    @SendToUser("/queue/game")
    public GameMessage submitAnswer(AnswerRequest answerRequest) {
        log.info("Answer received: {}", answerRequest);
        Game updatedGame = gameService.processAnswer(
                answerRequest.getGameId(),
                answerRequest.getPlayerId(),
                answerRequest.getQuestionId(),
                answerRequest.getAnswer()
        );

        if (updatedGame == null) {
            log.error("Game not found for answer: {}", answerRequest.getGameId());
            return new GameMessage("ERROR", "Game not found");
        }

        return getNextGameState(updatedGame);
    }

    @MessageMapping("/game/nextQuestion")
    @SendToUser("/queue/game")
    public GameMessage nextQuestion(GameRequest gameRequest) {
        log.info("Next question request received for game: {}", gameRequest.getGameId());
        Game game = gameService.getGame(gameRequest.getGameId());

        if (game == null) {
            log.error("Game not found: {}", gameRequest.getGameId());
            return new GameMessage("ERROR", "Game not found");
        }

        return getNextGameState(game);
    }

    private GameMessage getNextGameState(Game game) {
        if (game.getStatus() == Status.Completed) {
            log.info("Game over: {}", game.getId());
            GameMessage gameOverMessage = new GameMessage("GAME_OVER", game);
            game.getParticipants().forEach(playerId -> {
                messagingTemplate.convertAndSendToUser(playerId, "/queue/game", gameOverMessage);
                log.info("Sent GAME_OVER message to player: {}", playerId);
            });
            return gameOverMessage;
        } else {
            Mcq nextQuestion = gameService.getNextQuestion(game.getId());
            if (nextQuestion != null) {
                log.info("Next question for game {}: {}", game.getId(), nextQuestion.getId());
                GameMessage questionMessage = new GameMessage("QUESTION", nextQuestion);
                game.getParticipants().forEach(playerId -> {
                    messagingTemplate.convertAndSendToUser(playerId, "/queue/game", questionMessage);
                    log.info("Sent QUESTION message to player: {}", playerId);
                });
                return questionMessage;
            } else {
                log.info("No more questions, game over: {}", game.getId());
                game.setStatus(Status.Completed);
                gameService.updateGame(game);
                GameMessage gameOverMessage = new GameMessage("GAME_OVER", game);
                game.getParticipants().forEach(playerId -> {
                    messagingTemplate.convertAndSendToUser(playerId, "/queue/game", gameOverMessage);
                    log.info("Sent GAME_OVER message to player: {}", playerId);
                });
                return gameOverMessage;
            }
        }
    }
}