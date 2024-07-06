package com.example._v1.mcq.game.respository;

import com.example._v1.mcq.game.attributes.Mcq;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface McqRepo extends MongoRepository<Mcq, String> {
//    Optional<Mcq> findById(String Id){};
}
