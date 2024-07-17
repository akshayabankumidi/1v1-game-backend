package com.example._v1.mcq.game.controller;

import com.example._v1.mcq.game.entity.Mcq;

import com.example._v1.mcq.game.DTO.McqDeleteRequest;
import com.example._v1.mcq.game.services.McqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/mcq")
public class McqController {
    private final McqService mcqService;

    @PostMapping("/add")
    public ResponseEntity<?> addMcq(@RequestBody Mcq mcq) {
        try {
            Mcq savedMcq = mcqService.addMcq(mcq);
            return ResponseEntity.ok(savedMcq);
        } catch (Exception e) {
            log.error("Error adding MCQ: ", e);
            return ResponseEntity.badRequest().body("Error adding MCQ: " + e.getMessage());
        }
    }

    @PatchMapping("/modify/{id}")
    public ResponseEntity<?> modifyMcq(@PathVariable String id, @RequestBody Mcq mcq) {
        try {
            return mcqService.modifyMcq(id, mcq)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error modifying MCQ: ", e);
            return ResponseEntity.badRequest().body("Error modifying MCQ: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteMcqs(@RequestBody McqDeleteRequest deleteRequest) {
        try {
            McqService.DeleteResult result = mcqService.deleteMcqs(deleteRequest.getIds());
            StringBuilder responseMessage = new StringBuilder("Operation completed. ");
            if (!result.deletedIds.isEmpty()) {
                responseMessage.append("Deleted MCQs with ids: ").append(result.deletedIds).append(". ");
            }
            if (!result.notFoundIds.isEmpty()) {
                responseMessage.append("MCQs not found with ids: ").append(result.notFoundIds).append(".");
            }
            return ResponseEntity.ok(responseMessage.toString());
        } catch (Exception e) {
            log.error("Error deleting MCQs: ", e);
            return ResponseEntity.badRequest().body("Error deleting MCQs: " + e.getMessage());
        }
    }
}

