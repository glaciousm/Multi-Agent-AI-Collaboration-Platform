package com.localcollab.platform.web.dto;

import java.util.UUID;

public class TaskLaneRequest {
    private String name;
    private UUID implementorId;

    public String getName() {
        return name;
    }

    public UUID getImplementorId() {
        return implementorId;
    }
}
