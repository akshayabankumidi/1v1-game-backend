package com.example._v1.mcq.game.respository;

import com.example._v1.mcq.game.entity.Mcq;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface McqRepo extends MongoRepository<Mcq, String> {
//    Optional<Mcq> findById(String Id){};
}
