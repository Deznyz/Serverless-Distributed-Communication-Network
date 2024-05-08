package com.example.serverlessdistributedcommunicationnetwork;

import java.io.Serializable;

//---USED FOR THE TCPJOIN METHOD NOT RELEVANT FOR MEETING 10/5---
//---IN DEVELOPMENT---
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