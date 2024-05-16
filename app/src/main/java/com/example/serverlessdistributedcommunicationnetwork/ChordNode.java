package com.example.serverlessdistributedcommunicationnetwork;

import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.util.*;

public class ChordNode implements Serializable {
    private static final int M = 24;
    //total space of node IDs - using M=24 makes id_space = 16777216
    //done this way for easy scalability
    private static final int ID_SPACE = (int) Math.pow(2, M);
    private static final int DISCOVERY_PORT = 5555;//port for (UDP broadcast discovery

    private int nodeId;
    private ChordNode successor;
    private ChordNode predecessor;
    private ChordNode[] fingerTable;//changed to array for now, for easier usability in early stages
    private Map<String, String> ipAddresses;



    //for updating the display with ip-addresses
    private List<NodeJoinListener> joinListeners;

    //node object
    public ChordNode(NodeJoinListener listener) {
        this.nodeId = generateNodeId();
        this.fingerTable = new ChordNode[M];
        this.successor = this;
        this.predecessor = null;
        startDiscoveryListener(listener);
        ipAddresses = new HashMap<>();
        joinListeners = new ArrayList<>();
    }

    //generates a random node id between 0 and ID_SPACE - 1
    private int generateNodeId() {
        Random random = new Random();
        return random.nextInt(ID_SPACE);
    }

    //discpvery broadcast listener
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

                    //triggers when another node uses the "join" function
                    if (message.equals("DISCOVER")) {
                        sendDiscoveryResponse(packet.getAddress(), packet.getPort());

                        //stores the ip-address (so far) in a key-value map
                        ipAddresses.put(packet.getAddress().getHostAddress(), "test");

                        //triggers event
                        listener.onNodeJoin();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    //sends discovery response, when recieving a discovery message from another node
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

    //initial discover message sent when joining the network
    public void join(NodeJoinListener listener) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress group = InetAddress.getByName("224.0.0.1");

            //this message triggers the broadcast listener of other nodes
            String message = "DISCOVER";
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, DISCOVERY_PORT);
            socket.send(packet);
            listener.onNodeJoin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //---HELPER FUNCTIONS IN CHORD---

    //finds the successor of a given nodeID
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
    //---END OF: HELPER FUNCTIONS IN CHORD---

    //---GETTERS AND SETTERS---
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
    //---END OF: GETTERS AND SETTERS---


    //---JOIN LISTENERS METHODS---
    public void addJoinListener(NodeJoinListener listener) {
        joinListeners.add(listener);
    }

    public void removeAllJoinListeners() {
        joinListeners.clear();
    }
    //---END OF: JOIN LISTENERS METHODS---

}

//message format send by the JoinRequestTask and recieved by the ChordNodeServer
class JoinRequest implements Serializable {
    private int nodeId;

    public JoinRequest(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeId() {
        return nodeId;
    }
}

//message format send by the ChordNodeServer and recieved by the JoinRequestTask
class JoinResponse implements Serializable {
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
