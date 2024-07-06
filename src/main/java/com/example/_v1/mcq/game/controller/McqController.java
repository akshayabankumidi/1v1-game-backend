package com.example._v1.mcq.game.controller;

import com.example._v1.mcq.game.attributes.Mcq;
import com.example._v1.mcq.game.respository.McqRepo;
import com.example._v1.mcq.game.utils.McqUpdateUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/mcq")
public class McqController {
    private final McqRepo mcqRepo;
    private final ObjectMapper objectMapper;
    @PostMapping("/add")
    public ResponseEntity<?> addMcq(@RequestBody Mcq mcq) {
        try {
            log.info("Received MCQ to add: {}", mcq);
            Mcq savedMcq = mcqRepo.save(mcq);
            log.info("Added MCQ: {}", savedMcq);
            return ResponseEntity.ok(savedMcq);
        } catch (Exception e) {
            log.error("Error adding MCQ: ", e);
            return ResponseEntity.badRequest().body("Error adding MCQ: " + e.getMessage());
        }
    }

    @PutMapping("/modify/{id}")
    public ResponseEntity<?> modifyMcq(@PathVariable String id, @RequestBody Mcq mcq) {
        try {
            log.info("Attempting to modify MCQ with id: {}", id);
            return mcqRepo.findById(id)
                    .map(existingMcq -> {
                        McqUpdateUtil.updateNonNullFields(mcq, existingMcq);
                        Mcq updatedMcq = mcqRepo.save(existingMcq);
                        log.info("Modified MCQ: {}", updatedMcq);
                        return ResponseEntity.ok(updatedMcq);
                    })
                    .orElseGet(() -> {
                        log.warn("MCQ with id {} not found for modification", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("Error modifying MCQ: ", e);
            return ResponseEntity.badRequest().body("Error modifying MCQ: " + e.getMessage());
        }
    }
//    @DeleteMapping("/delete")
//    public ResponseEntity<?> deleteMcqs(@RequestBody List<String> ids) {
//        try {
//
//            log.info("Received request to delete MCQs with ids: {}", ids);
//            List<String> deletedIds = new ArrayList<>();
//            List<String> notFoundIds = new ArrayList<>();
//
//            for (String id : ids) {
//                if (mcqRepo.existsById(id)) {
//                    mcqRepo.deleteById(id);
//                    deletedIds.add(id);
//                } else {
//                    notFoundIds.add(id);
//                }
//            }
//
//            StringBuilder responseMessage = new StringBuilder("Operation completed. ");
//            if (!deletedIds.isEmpty()) {
//                responseMessage.append("Deleted MCQs with ids: ").append(deletedIds).append(". ");
//            }
//            if (!notFoundIds.isEmpty()) {
//                responseMessage.append("MCQs not found with ids: ").append(notFoundIds).append(".");
//            }
//
//            log.info(responseMessage.toString());
//            return ResponseEntity.ok(responseMessage.toString());
//        } catch (Exception e) {
//            log.error("Error deleting MCQs: ", e);
//            return ResponseEntity.badRequest().body("Error deleting MCQs: " + e.getMessage());
//        }
//    }

    @DeleteMapping("/delete")
public ResponseEntity<String> deleteMcqs(@RequestBody DeleteRequest request) {
    List<String> ids = request.getIds();
    log.info("Received delete request for ids: {}", ids);
    mcqRepo.deleteAllById(ids);
   return ResponseEntity.ok("Deleted " + ids.size() + " items");
}

}
class DeleteRequest {

    private List<String> ids;

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
}