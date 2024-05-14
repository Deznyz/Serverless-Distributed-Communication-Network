package com.example.serverlessdistributedcommunicationnetwork;

import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.UUID;
import java.util.Random;
import java.net.*;
import java.util.*;



public class ChordNode implements Serializable{
    private static final int M = 24;
    //total space of node IDs - using M=24 makes id_space = 16777216
    //done this way for easy scalability
    private static final int ID_SPACE = (int) Math.pow(2, M);
    private static final int DISCOVERY_PORT = 5555; //port for (UDP broadcast discovery

    private int nodeId;
    private ChordNode successor;//as of right now not in functional use
    private ChordNode predecessor;//as of right now not in functional use
    private ChordNode[] fingerTable;//as of right now not in functional use
    private Map<String, String> ipAddresses;


    // For updating the display with ip-addresses
    private List<NodeJoinListener> joinListeners = new ArrayList<>();


    //node object
    public ChordNode(NodeJoinListener listener) {
        this.nodeId = generateNodeId();
        this.fingerTable = new ChordNode[M];
        startDiscoveryListener(listener);
        ipAddresses = new HashMap<>();
        joinListeners = new ArrayList<>();
    }

    //generates a random node id between 0 and ID_SPACE - 1
    private int generateNodeId() {
        Random random = new Random();
        return random.nextInt(ID_SPACE);
    }


    //new thread with a listener on 224.0.0.1
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

                        //gets both port and ip-address from the other node
                        System.out.println("Received discovery request from " + packet.getAddress() + ". From port " + packet.getPort());

                        //stores the ip-address (so far) in a key-value map
                        ipAddresses.put(packet.getAddress().getHostAddress(), "test");

                        //prints the map for debuggings sake
                        System.out.println(ipAddresses);

                        //triggers event
                        listener.onNodeJoin();

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    //sends a message to the other node, when a discoverymessage was recievced
    private void sendDiscoveryResponse(InetAddress address, int port) {
        try (DatagramSocket socket = new DatagramSocket()) {
            String response = nodeId + "," + InetAddress.getLocalHost().getHostAddress();
            byte[] buffer = response.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            socket.send(packet);
            //includes the ip-address of current node
            System.out.println("Sent discovery response to " + address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //UDP join method - sends a discovery message on the chosen ip address
    public void join(NodeJoinListener listener) {
        //broadcasts a discovery message
        try (DatagramSocket socket = new DatagramSocket()) {
            //224.0.0.1 reserved for all hosts on the network
            InetAddress group = InetAddress.getByName("224.0.0.1");
            String message = "DISCOVER";
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, DISCOVERY_PORT);
            socket.send(packet);



            //EVENT: notifies the listener about join for the UI update (as is right now)
            //NOTE TO SELF: might not need to be here after added to "startDiscoveryListener" - look into it
            listener.onNodeJoin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //---TCP CONNECTION IN DEVELOPMENT---
    //not relevant for the meeting 10/5
    public void tcpJoin(String existingNodeIP, int existingNodePort) {
        try (Socket socket = new Socket(existingNodeIP, existingNodePort);
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {

            // Send join request
            outputStream.writeObject(new JoinRequest(nodeId));
            outputStream.flush();

            // Receive join response
            Object response = inputStream.readObject();

            if (response instanceof JoinResponse) {
                JoinResponse joinResponse = (JoinResponse) response;
                this.successor = joinResponse.getSuccessor();
                this.predecessor = joinResponse.getPredecessor();

                // Update references
                this.successor.setPredecessor(this);
                this.predecessor.setSuccessor(this);

                // Update finger table
                updateFingerTable();
            } else {
                System.err.println("Error: Invalid response received during join");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    //---END OF TCP CONNECTION IN DEVELOPMENT---

    //---HELPER FUNCTIONS: as of this moment these functions does not do anything iin the current implementation---
    private void updateFingerTable() {
        for (int i = 0; i < M; i++) {
            int start = (nodeId + (1 << i)) % ID_SPACE;
            fingerTable[i] = findSuccessor(start);
        }

    }

    private boolean isInInterval(int id, int start, int end) {
        if (start < end) {
            return id > start && id <= end;
        } else {
            return id > start || id <= end;
        }
    }

    ChordNode findSuccessor(int id) {
        if (isInInterval(id, nodeId, successor.nodeId)) {
            return successor;
        } else {
            ChordNode closestPrecedingNode = closestPrecedingFinger(id);
            // Placeholder for actual remote call to closestPrecedingNode.findSuccessor(id)
            return closestPrecedingNode.findSuccessor(id); // Replace with actual logic to contact other nodes
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

    //---END OF HELPERFUNCTIONS---

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

    public Map<String, String> getIpAddresses() {
        return ipAddresses;
    }

    //implementation of the
    public void addJoinListener(NodeJoinListener listener) {
        joinListeners.add(listener);
    }




    //--Leftover code for a precious attempt, left in as im unsure if needed later---
//    private void notifyNodeJoinListeners() {
//        for (NodeJoinListener listener : joinListeners) {
//            listener.onNodeJoin();
//        }
//    }
    //---END OF LEFTOVERCODE---




}



// Serializable classes for join request and response
class JoinRequest implements Serializable {
    private int nodeId;

    public JoinRequest(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeId() {
        return nodeId;
    }
}

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

