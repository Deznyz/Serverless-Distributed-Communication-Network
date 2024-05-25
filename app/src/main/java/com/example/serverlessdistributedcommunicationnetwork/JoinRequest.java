package com.example.serverlessdistributedcommunicationnetwork;

import java.io.Serializable;

public class JoinRequest implements Serializable {
    private int nodeId;

    public JoinRequest(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeId() {
        return nodeId;
    }
}
