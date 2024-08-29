package com.example._v1.mcq.game.controller;

import com.example._v1.mcq.game.DTO.AnswerRequest;
import com.example._v1.mcq.game.DTO.DeleteRequest;
import com.example._v1.mcq.game.DTO.GameRequest;
import com.example._v1.mcq.game.DataTypes.Custom.DeleteResult;
import com.example._v1.mcq.game.DataTypes.Custom.GameData;
import com.example._v1.mcq.game.DataTypes.Custom.GameMessage;
import com.example._v1.mcq.game.DataTypes.Custom.ParticipantInfo;
import com.example._v1.mcq.game.DataTypes.Enums.Status;
import com.example._v1.mcq.game.entity.Game;
import com.example._v1.mcq.game.entity.Mcq;
import com.example._v1.mcq.game.services.GameService;
import com.example._v1.mcq.game.services.McqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.security.Principal;

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
            log.info("inside getAllGamesPL2");
            return ResponseEntity.ok(gameService.getAllGamesParticpntLessThan2());
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

@MessageMapping("/game/join")
public void joinGame(GameRequest gameRequest, SimpMessageHeaderAccessor headerAccessor) {
    Principal principal = headerAccessor.getUser();
    if (principal == null) {
        log.error("Principal is null for game join request");
        return;
    }
    String username = principal.getName();
    log.info("Join game request received for game: {}, player: {}", gameRequest.getGameId(), username);

    Game game = gameService.getGame(gameRequest.getGameId());
    if (game == null) {
        log.error("Game not found: {}", gameRequest.getGameId());
        sendErrorToUser(username, "Game not found");
        return;
    }

    if (game.getParticipants() == null) {
        game.setParticipants(new ArrayList<>());
    }
    ParticipantInfo participantInfo = new ParticipantInfo(username,0,0);
    if (!game.getParticipants().contains(participantInfo)) {
        game.getParticipants().add(participantInfo);
    }
    game = gameService.updateGame(game);

    log.info("Updated game after join: {}", game);

    if (game.getParticipants().size() == 2) {
        game.setStatus(Status.InProgress);
        game = gameService.updateGame(game);

        List<Mcq> mcqs = mcqService.getMcqsByGameId(game.getId());
        Mcq firstQuestion = mcqs.get(0);
        GameData gameData = new GameData(game, mcqs);
        GameMessage gameStartedMessage = new GameMessage("GAME_STARTED", gameData);

        sendMessageToAllParticipants(game, gameStartedMessage);

        GameMessage questionMessage = new GameMessage("QUESTION", firstQuestion);
        sendMessageToAllParticipants(game, questionMessage);
    } else {
        List<Mcq> mcqs = mcqService.getMcqsByGameId(game.getId());
        GameData gameData = new GameData(game, mcqs);
        GameMessage gameWaitingMessage = new GameMessage("WAITING_FOR_PLAYERS", gameData);
        sendMessageToUser(username, gameWaitingMessage);
    }
}

    private void sendMessageToAllParticipants(Game game, GameMessage message) {
        for (ParticipantInfo participantInfo : game.getParticipants()) {
            sendMessageToUser(participantInfo.getParticipantId(), message);
            log.info("Sent message to all participants: {}", participantInfo.getParticipantId());
        }
    }

    private void sendMessageToUser(String username, GameMessage message) {
        try {
            messagingTemplate.convertAndSendToUser(username, "/queue/game", message);
            log.info("Sent message to user: {}, message type: {}", username, message.getType());
        } catch (Exception e) {
            log.error("Error sending message to user: {}", username, e);
        }
    }

    private void sendErrorToUser(String username, String errorMessage) {
        sendMessageToUser(username, new GameMessage("ERROR", errorMessage));
    }

    @MessageMapping("/game/answer")
    @SendToUser("/queue/game")
    public GameMessage submitAnswer(AnswerRequest answerRequest, SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            log.error("Principal is null for game join request");
             return null;
        }
        String username = principal.getName();
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

        return getNextGameState(updatedGame,username);
    }

    private GameMessage getNextGameState(Game game, String username) {

            Mcq nextQuestion = gameService.getNextQuestion(game.getId(),username);
            List<Mcq> mcqs = mcqService.getMcqsByGameId(game.getId());
            if (nextQuestion != null) {
                log.info("Next question for game {}: {}", game.getId(), nextQuestion.getId());
                GameMessage questionMessage = new GameMessage("QUESTION", nextQuestion);
                return questionMessage;
            } else {
                log.info("No more questions, game over: {}", game.getId());
                gameService.updateGame(game);
                List<ParticipantInfo> participants = game.getParticipants();
                ParticipantInfo p1 = participants.get(0);
                ParticipantInfo p2 = participants.get(1);
                GameMessage gameOverMessage = null;
                if(p1.getCurrentQuestionIndex() == mcqs.size() &&  p2.getCurrentQuestionIndex() == mcqs.size()) {
                    gameOverMessage = new GameMessage("GAME_OVER", game);
                    sendMessageToAllParticipants(game, gameOverMessage);

                } else {
                    gameOverMessage = new GameMessage("Opponent is Still Playing", game);
                }

                return gameOverMessage;
            }
        }
}