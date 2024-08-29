package com.example._v1.mcq.game.entity;

import com.example._v1.mcq.game.DataTypes.Custom.ParticipantInfo;
import com.example._v1.mcq.game.DataTypes.Enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Game")
public class Game {
    @Id
    private String id;
    @NonNull
    private String creatorId;
    private Status status = Status.Waiting;
    private List<ParticipantInfo> participants = new ArrayList<>();
}