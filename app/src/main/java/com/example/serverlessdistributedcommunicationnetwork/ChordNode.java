// ChordNode.java
package com.example.serverlessdistributedcommunicationnetwork;

import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.util.*;

public class ChordNode implements Serializable {
    private static final int M = 24;
    private static final int ID_SPACE = (int) Math.pow(2, M);
    private static final int DISCOVERY_PORT = 5555;

    private int nodeId;
    private ChordNode successor;
    private ChordNode predecessor;
    private ChordNode[] fingerTable;
    private Map<String, String> ipAddresses;

    private List<NodeJoinListener> joinListeners;

    public ChordNode(NodeJoinListener listener) {
        this.nodeId = generateNodeId();
        this.fingerTable = new ChordNode[M];
        this.successor = this;
        this.predecessor = null;
        startDiscoveryListener(listener);
        ipAddresses = new HashMap<>();
        joinListeners = new ArrayList<>();
    }

    private int generateNodeId() {
        Random random = new Random();
        return random.nextInt(ID_SPACE);
    }

    private void startDiscoveryListener(NodeJoinListener listener) {
        new Thread(() -> {
            try (MulticastSocket socket = new MulticastSocket(DISCOVERY_PORT)) {
                InetAddress group = InetAddress.getByName("224.0.0.1");
                socket.joinGroup(group);

                while (true) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String message = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    if (message.equals("DISCOVER")) {
                        sendDiscoveryResponse(packet.getAddress(), packet.getPort());
                        ipAddresses.put(packet.getAddress().getHostAddress(), "test");
                        listener.onNodeJoin();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void sendDiscoveryResponse(InetAddress address, int port) {
        try (DatagramSocket socket = new DatagramSocket()) {
            String response = nodeId + "," + InetAddress.getLocalHost().getHostAddress();
            byte[] buffer = response.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void join(NodeJoinListener listener) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress group = InetAddress.getByName("224.0.0.1");
            String message = "DISCOVER";
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, DISCOVERY_PORT);
            socket.send(packet);
            listener.onNodeJoin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ChordNode findSuccessor(int id) {
        if (successor != null && isInInterval(id, nodeId, successor.nodeId)) {
            return successor;
        } else {
            ChordNode closestPrecedingNode = closestPrecedingFinger(id);
            if (closestPrecedingNode != null && closestPrecedingNode != this) {
                return closestPrecedingNode.findSuccessor(id);
            } else {
                return this;
            }
        }
    }

    private boolean isInInterval(int id, int start, int end) {
        if (start < end) {
            return id > start && id <= end;
        } else {
            return id > start || id <= end;
        }
    }

    private ChordNode closestPrecedingFinger(int id) {
        for (int i = M - 1; i >= 0; i--) {
            if (fingerTable[i] != null && isInInterval(fingerTable[i].nodeId, nodeId, id)) {
                return fingerTable[i];
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

    public ChordNode getSuccessor() {
        return successor;
    }

    public void setSuccessor(ChordNode successor) {
        this.successor = successor;
    }

    public int getNodeId() {
        return this.nodeId;
    }

    public Map<String, String> getIpAddresses() {
        return ipAddresses;
    }

    public void addJoinListener(NodeJoinListener listener) {
        joinListeners.add(listener);
    }

    public void removeJoinListener(NodeJoinListener listener) {
        joinListeners.remove(listener);
    }

    public void leave() {
        if (predecessor != null) {
            predecessor.setSuccessor(successor);
        }
        if (successor != null) {
            successor.setPredecessor(predecessor);
            // Transfer data to the successor
            // transferDataToSuccessor(); // Implement this if you have data to transfer
        }
        predecessor = null;
        successor = null;
    }
}
