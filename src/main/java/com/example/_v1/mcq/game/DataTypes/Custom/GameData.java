package com.example._v1.mcq.game.DataTypes.Custom;

import com.example._v1.mcq.game.entity.Game;
import com.example._v1.mcq.game.entity.Mcq;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameData {
    private Game game;
    private List<Mcq> questions;
}