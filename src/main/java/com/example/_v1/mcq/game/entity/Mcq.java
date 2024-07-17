package com.example._v1.mcq.game.entity;
import com.example._v1.mcq.game.DataTypes.Custom.Options;
import com.example._v1.mcq.game.DataTypes.Enums.Difficulty;
import com.example._v1.mcq.game.DataTypes.Enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection ="Mcqs")
public class Mcq {
    @Id
    private String id;
    @NonNull
    private String question;
    @NonNull
    private List<String> listOfOptions;
    @NonNull
    private List<Options> correctOptions;

    private Difficulty difficulty;

    private Visibility visibility = Visibility.Private;

    private String topic = "Miscellaneous";

}
