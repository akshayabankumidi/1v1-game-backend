package com.example._v1.mcq.game.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "GameProgress")
public class GameProgress {
    @Id
    private String id;
    private String gameId;
    private String participantId;
    private int score;
    private int incorrectAnswers;
    private int currentQuestionIndex;
    private List<String> answeredMcqIds; // Track answered MCQs
}