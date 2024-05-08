package com.example.serverlessdistributedcommunicationnetwork;
import java.io.Serializable;

//---USED FOR THE TCPJOIN METHOD NOT RELEVANT FOR MEETING 10/5---
//---IN DEVELOPMENT---
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