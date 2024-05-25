package com.example.serverlessdistributedcommunicationnetwork;

import java.io.Serializable;

public class JoinResponse implements Serializable {
    private ChordNode successor;
    private ChordNode predecessor;

    public JoinResponse(ChordNode successor, ChordNode predecessor) {
        this.successor = successor;
        this.predecessor = predecessor;
    }

    public ChordNode getSuccessor() {
        return successor;
    }

    public ChordNode getPredecessor() {
        return predecessor;
    }
}
