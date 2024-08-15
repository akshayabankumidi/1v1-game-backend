package com.example._v1.mcq.game.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnswerRequest {
    private String gameId;
    private String questionId;
    private String playerId;
    private String answer;

}