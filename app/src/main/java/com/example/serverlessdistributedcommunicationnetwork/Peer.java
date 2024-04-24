package com.example.serverlessdistributedcommunicationnetwork;

import java.util.Arrays;

public class Peer {
    private int id;
    private Peer successor;
    private Peer predecessor;
    private Peer[] fingerTable;

    private static final int FINGER_TABLE_SIZE = 8; // Size of the finger table
    private static final int MAX_IDENTIFIER = 1000; // Maximum value for peer identifier

    public Peer(int id) {
        this.id = id;
        fingerTable = new Peer[FINGER_TABLE_SIZE];
        // Initialize successor and predecessor to null
        successor = null;
        predecessor = null;
    }

    // Method to join the network
    public void join(Peer existingPeer) {
        if (existingPeer == null) {
            // First peer joining the network
            predecessor = null;
            successor = this;
            // Initialize finger table with self
            Arrays.fill(fingerTable, this);
        } else {
            predecessor = null;
            successor = existingPeer.findSuccessor(this.id);
            successor.updateFingerTable(this, 1);
            predecessor = successor.getPredecessor();
            successor.setPredecessor(this);
        }
    }

    // Method to leave the network
    public void leave() {
        if (predecessor != null) {
            successor.setPredecessor(predecessor);
        }
        if (successor != null) {
            predecessor.setSuccessor(successor);
        }
    }

    // Method to route a message to a destination peer
    public void routeMessage(int destinationId, String message) {
        Peer successor = findSuccessor(destinationId);
        successor.receiveMessage(message);
    }

    // Method to find the successor of a given identifier
    private Peer findSuccessor(int identifier) {
        // Implementation of CHORD's successor finding algorithm
        // This is a simplified version for demonstration purposes
        Peer successor = this;
        while (successor.successor.id != this.id && !(identifier > this.id && identifier <= successor.id)) {
            successor = successor.closestPrecedingFinger(identifier);
        }
        return successor;
    }

    // Method to find the closest preceding finger of a given identifier
    private Peer closestPrecedingFinger(int identifier) {
        for (int i = FINGER_TABLE_SIZE - 1; i >= 0; i--) {
            if (fingerTable[i] != null && fingerTable[i].id < this.id && fingerTable[i].id > identifier) {
                return fingerTable[i];
            }
        }
        return this;
    }

    // Method to update the finger table of the peer
    public void updateFingerTable(Peer peer, int index) {
        if (peer.id > this.id && peer.id <= fingerTable[index].id) {
            fingerTable[index] = peer;
            if (predecessor != null) {
                predecessor.updateFingerTable(peer, index);
            }
        }
    }

    // Method to receive a message
    public void receiveMessage(String message) {
        System.out.println("Received message: " + message);
        // Process the received message
    }

    // Getters and setters
    public Peer getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(Peer predecessor) {
        this.predecessor = predecessor;
    }

    public Peer getSuccessor() {
        return successor;
    }

    public void setSuccessor(Peer successor) {
        this.successor = successor;
    }
}
