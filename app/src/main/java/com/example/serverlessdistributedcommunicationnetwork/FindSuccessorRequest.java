package com.example.serverlessdistributedcommunicationnetwork;

import java.io.Serializable;

public class FindSuccessorRequest implements Serializable {
    private int nodeId;

    public FindSuccessorRequest(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeId() {
        return nodeId;
    }
}
