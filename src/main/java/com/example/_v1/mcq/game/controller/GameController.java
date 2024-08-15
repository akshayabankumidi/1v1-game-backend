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
        try{
            Game savedGame = gameService.addGame(game);
            return ResponseEntity.ok(savedGame);
        } catch(Exception e){
          return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PatchMapping("/modify/{id}")
    public ResponseEntity<?> modifyGame(@PathVariable String id,@RequestBody Game game) {
        try{
            return gameService.modifyGame(id,game)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteGame(@RequestBody DeleteRequest deleteRequest) {
        try{
            DeleteResult result = gameService.deleteGames(deleteRequest.getIds());
            StringBuilder responseMessage = new StringBuilder("Operation completed");
            if(!result.deletedIds.isEmpty()){
                responseMessage.append(" deleted ids: ").append(result.deletedIds).append(".");
            }
            if(!result.notFoundIds.isEmpty()){
                responseMessage.append(" not found ids: ").append(result.notFoundIds).append(".");
            }
            return ResponseEntity.ok(responseMessage.toString());
        } catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getAllGamesPL2") // participant len <2
    public ResponseEntity<?> getAllGamesPL2() {
        try{
            return ResponseEntity.ok(gameService.getAllGamesParticpntLessThan2());
        } catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @MessageMapping("/game/join")
    @SendToUser("/queue/game")
    public GameMessage joinGame(GameRequest gameRequest) {
        Game game = gameService.getGame(gameRequest.getGameId());
        if (game == null) {
            return new GameMessage("ERROR", "Game not found");
        }

        // Add the player to the game's participants
        if (game.getParticipants() == null) {
            game.setParticipants(new ArrayList<>());
        }
        if (!game.getParticipants().contains(gameRequest.getPlayerId())) {
            game.getParticipants().add(gameRequest.getPlayerId());
        }
        game = gameService.updateGame(game);

        GameMessage responseMessage;
          log.info(String.valueOf(game));
        if (game.getParticipants().size() == 2) {
            // Two players have joined, start the game
            game.setStatus(Status.InProgress);
            game = gameService.updateGame(game);

            List<Mcq> mcqs = mcqService.getMcqsByGameId(game.getId());
            Mcq firstQuestion = mcqs.get(0);  // Assuming the first question is the starting question

            GameData gameData = new GameData(game, mcqs);
            responseMessage = new GameMessage("GAME_STARTED", gameData);

            // Send the first question to both players
            GameMessage questionMessage = new GameMessage("QUESTION", firstQuestion);
            game.getParticipants().forEach(playerId ->
                    messagingTemplate.convertAndSendToUser(playerId, "/queue/game", questionMessage));
        } else {
            // Waiting for more players
            List<Mcq> mcqs = mcqService.getMcqsByGameId(game.getId());
            GameData gameData = new GameData(game, mcqs);
            responseMessage = new GameMessage("WAITING_FOR_PLAYERS", gameData);
        }

        return responseMessage;
    }

    @MessageMapping("/game/answer")
    @SendToUser("/queue/game")
    public GameMessage submitAnswer(AnswerRequest answerRequest) {
        Game updatedGame = gameService.processAnswer(
                answerRequest.getGameId(),
                answerRequest.getPlayerId(),
                answerRequest.getQuestionId(),
                answerRequest.getAnswer()
        );

        if (updatedGame == null) {
            return new GameMessage("ERROR", "Game not found");
        }

        return getNextGameState(updatedGame);
    }

    @MessageMapping("/game/nextQuestion")
    @SendToUser("/queue/game")
    public GameMessage nextQuestion(GameRequest gameRequest) {
        Game game = gameService.getGame(gameRequest.getGameId());

        if (game == null) {
            return new GameMessage("ERROR", "Game not found");
        }

        return getNextGameState(game);
    }

    private GameMessage getNextGameState(Game game) {
        if (game.getStatus() == Status.Completed) {
            return new GameMessage("GAME_OVER", game);
        } else {
            Mcq nextQuestion = gameService.getNextQuestion(game.getId());
            if (nextQuestion != null) {
                return new GameMessage("QUESTION", nextQuestion);
            } else {
                game.setStatus(Status.Completed);
                gameService.updateGame(game);
                return new GameMessage("GAME_OVER", game);
            }
        }
    }
}
