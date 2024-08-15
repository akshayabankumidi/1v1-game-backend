package com.example._v1.mcq.game.DataTypes.Custom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameMessage {
    private String type;
    private Object payload;
}