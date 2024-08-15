package com.example._v1.mcq.game.respository;

import com.example._v1.mcq.game.entity.Mcq;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface McqRepo extends MongoRepository<Mcq, String> {
  List<Mcq> findByGameId(String gameId);
}
