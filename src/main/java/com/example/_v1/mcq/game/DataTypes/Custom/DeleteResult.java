package com.example._v1.mcq.game.DataTypes.Custom;

import java.util.List;

public class DeleteResult {
    public final List<String> deletedIds;
    public final List<String> notFoundIds;

    public DeleteResult(List<String> deletedIds, List<String> notFoundIds) {
        this.deletedIds = deletedIds;
        this.notFoundIds = notFoundIds;
    }
}