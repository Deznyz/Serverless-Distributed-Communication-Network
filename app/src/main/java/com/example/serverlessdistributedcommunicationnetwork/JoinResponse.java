package com.example.serverlessdistributedcommunicationnetwork;
import java.io.Serializable;

// JoinResponse class for sending join responses
public class JoinResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private ChordNode successor;

    public JoinResponse(ChordNode successor) {
        this.successor = successor;
    }

    public ChordNode getSuccessor() {
        return successor;
    }
}