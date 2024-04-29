package com.example.serverlessdistributedcommunicationnetwork;

import java.util.ArrayList;
import java.util.List;

public class ChordNode {
    private static final int M = 4; // Size of the identifier space (2^M)

    private int nodeId;
    private ChordNode successor;
    private ChordNode predecessor;
    private List<ChordNode> fingerTable;

    public ChordNode(int nodeId) {
        this.nodeId = nodeId;
        this.fingerTable = new ArrayList<>();
        initializeFingerTable();
    }

    private void initializeFingerTable() {
        for (int i = 0; i < M; i++) {
            fingerTable.add(null);
        }
    }

    public void join(ChordNode node) {
        if (node == null) {
            this.predecessor = null;
            this.successor = this;
            for (int i = 0; i < M; i++) {
                fingerTable.set(i, this);
            }
        } else {
            this.successor = node.findSuccessor(this.nodeId);
            this.predecessor = this.successor.getPredecessor();
            this.successor.setPredecessor(this);
            for (int i = 0; i < M; i++) {
                if (isInInterval((nodeId + (int) Math.pow(2, i)) % (int) Math.pow(2, M), this.nodeId, this.successor.nodeId, false)) {
                    fingerTable.set(i, this.successor);
                } else {
                    fingerTable.set(i, node.findSuccessor((nodeId + (int) Math.pow(2, i)) % (int) Math.pow(2, M)));
                }
            }
        }
    }

    private boolean isInInterval(int id, int start, int end, boolean inclusive) {
        if (inclusive) {
            return (start <= id && id <= end) || (start > end && (start <= id || id <= end));
        } else {
            return (start < id && id < end) || (start > end && (start < id || id < end));
        }
    }

    public ChordNode findSuccessor(int id) {
        if (id == nodeId) {
            return this;
        } else if (isInInterval(id, nodeId, successor.nodeId, true)) {
            return successor;
        } else {
            ChordNode node = closestPrecedingNode(id);
            if (node != null) {
                return node.findSuccessor(id);
            }
            return null;
        }
    }

    private ChordNode closestPrecedingNode(int id) {
        for (int i = M - 1; i >= 0; i--) {
            ChordNode finger = fingerTable.get(i);
            if (finger != null && isInInterval(finger.nodeId, nodeId, id, false)) {
                return finger;
            }
        }
        return this;
    }

    public ChordNode getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(ChordNode predecessor) {
        this.predecessor = predecessor;
    }
}
