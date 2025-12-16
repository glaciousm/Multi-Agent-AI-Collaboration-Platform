package com.localcollab.platform.web.dto;

import com.localcollab.platform.domain.TaskLaneState;

public class TaskLaneStateRequest {
    private TaskLaneState state;

    public TaskLaneState getState() {
        return state;
    }

    public void setState(TaskLaneState state) {
        this.state = state;
    }
}
