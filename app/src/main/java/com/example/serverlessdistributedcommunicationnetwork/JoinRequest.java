package com.example.serverlessdistributedcommunicationnetwork;

import java.io.Serializable;

// JoinRequest class for sending join requests
public class JoinRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private int nodeId;

    public JoinRequest(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeId() {
        return nodeId;
    }
}