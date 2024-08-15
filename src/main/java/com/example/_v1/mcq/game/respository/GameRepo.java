package com.example._v1.mcq.game.respository;

import com.example._v1.mcq.game.entity.Game;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GameRepo extends MongoRepository<Game, String> {
}
