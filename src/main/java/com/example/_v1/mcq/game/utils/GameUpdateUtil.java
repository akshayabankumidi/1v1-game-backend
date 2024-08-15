package com.example._v1.mcq.game.utils;

import com.example._v1.mcq.game.entity.Game;

public class GameUpdateUtil {
    public static void updateNonNullFields(Game source, Game target){
       if(source.getStatus()!= null){
           target.setStatus(source.getStatus());
       }
       if(source.getParticipants()!= null){
           target.setParticipants(source.getParticipants());
       }
//       if(source.getMcqIDs()!= null){
//           target.setMcqIDs(source.getMcqIDs());
//       }
//       if(source.getTotalQuestions()!= 0){
//           target.setTotalQuestions(source.getTotalQuestions());
//       }
    }
}
