package com.example._v1.mcq.game.DataTypes.Custom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ParticipantInfo {
    String participantId;
    private int currentQuestionIndex = 0;
    private int scores =0;
}
