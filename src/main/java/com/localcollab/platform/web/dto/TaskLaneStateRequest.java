package com.localcollab.platform.web.dto;

import com.localcollab.platform.domain.TaskLaneState;
import jakarta.validation.constraints.NotNull;

public class TaskLaneStateRequest {
    @NotNull(message = "state is required")
    private TaskLaneState state;

    public TaskLaneState getState() {
        return state;
    }

    public void setState(TaskLaneState state) {
        this.state = state;
    }
}
