package com.example._v1.mcq.game.utils;

import com.example._v1.mcq.game.entity.Mcq;

public class McqUpdateUtil {
    public static void updateNonNullFields(Mcq source, Mcq target) {
        if (source.getQuestion() != null) {
            target.setQuestion(source.getQuestion());
        }
        if (source.getListOfOptions() != null) {
            target.setListOfOptions(source.getListOfOptions());
        }
        if (source.getCorrectOptions() != null) {
            target.setCorrectOptions(source.getCorrectOptions());
        }
        if (source.getDifficulty() != null) {
            target.setDifficulty(source.getDifficulty());
        }
        if (source.getVisibility() != null) {
            target.setVisibility(source.getVisibility());
        }
        if(source.getTopic()!= null){
            target.setTopic(source.getTopic());
        }

    }
}