package com.example._v1.mcq.game.services;

import com.example._v1.mcq.game.entity.Mcq;
import com.example._v1.mcq.game.respository.McqRepo;
import com.example._v1.mcq.game.utils.McqUpdateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class McqService {
    private final McqRepo mcqRepo;

    public Mcq addMcq(Mcq mcq) {
        log.info("Adding MCQ: {}", mcq);
        return mcqRepo.save(mcq);
    }

    public Optional<Mcq> modifyMcq(String id, Mcq mcq) {
        log.info("Modifying MCQ with id: {}", id);
        return mcqRepo.findById(id)
                .map(existingMcq -> {
                    McqUpdateUtil.updateNonNullFields(mcq, existingMcq);
                    return mcqRepo.save(existingMcq);
                });
    }

    public DeleteResult deleteMcqs(List<String> ids) {
        log.info("Deleting MCQs with ids: {}", ids);
        List<String> deletedIds = new ArrayList<>();
        List<String> notFoundIds = new ArrayList<>();

        for (String id : ids) {
            if (mcqRepo.existsById(id)) {
                mcqRepo.deleteById(id);
                deletedIds.add(id);
            } else {
                notFoundIds.add(id);
            }
        }

        return new DeleteResult(deletedIds, notFoundIds);
    }

    public static class DeleteResult {
        public final List<String> deletedIds;
        public final List<String> notFoundIds;

        public DeleteResult(List<String> deletedIds, List<String> notFoundIds) {
            this.deletedIds = deletedIds;
            this.notFoundIds = notFoundIds;
        }
    }
}