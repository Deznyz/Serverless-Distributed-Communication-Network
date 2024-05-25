package com.example.serverlessdistributedcommunicationnetwork;

import java.io.Serializable;

class LeaveRequest implements Serializable {
    private int nodeId;

    public LeaveRequest(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeId() {
        return nodeId;
    }
}

class LeaveResponse implements Serializable {
    private boolean success;

    public LeaveResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
