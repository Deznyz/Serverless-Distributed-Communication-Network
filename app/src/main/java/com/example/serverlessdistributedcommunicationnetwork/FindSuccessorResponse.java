package com.example.serverlessdistributedcommunicationnetwork;

import java.io.Serializable;

public class FindSuccessorResponse implements Serializable {
    private ChordNode successor;

    public FindSuccessorResponse(ChordNode successor) {
        this.successor = successor;
    }

    public ChordNode getSuccessor() {
        return successor;
    }
}
