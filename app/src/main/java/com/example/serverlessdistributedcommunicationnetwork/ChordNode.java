package com.example.serverlessdistributedcommunicationnetwork;

import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;
import java.util.Random;
import java.net.*;
import java.util.*;



public class ChordNode {
    private static final int M = 4; // Size of the identifier space (2^M)
    private static final int ID_SPACE = (int) Math.pow(2, M); // Total space of node IDs
    private static final int DISCOVERY_PORT = 5555; // Port for multicast discovery

    private int nodeId;
    private ChordNode successor;
    private ChordNode predecessor;
    private Map<Integer, ChordNode> fingerTable;
    private Map<String, String> ipAddresses = new HashMap<>();


    // For updating the display with ip-addresses
    private List<NodeJoinListener> joinListeners = new ArrayList<>();


    public ChordNode(NodeJoinListener listener) {
        this.nodeId = generateNodeId(); // Assign unique node ID
        this.fingerTable = new HashMap<>();
        initializeFingerTable();
        startDiscoveryListener(listener);
        ipAddresses = new HashMap<>();
        joinListeners = new ArrayList<>();
    }

    private int generateNodeId() {
        // Generate a random integer between 0 and ID_SPACE - 1 as the node ID
        Random random = new Random();
        return random.nextInt(ID_SPACE);
    }

    private void initializeFingerTable() {
        for (int i = 0; i < M; i++) {
            int fingerId = (nodeId + (1 << i)) % ID_SPACE;
            fingerTable.put(fingerId, null);
        }
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
                        // Respond to discovery request
                        sendDiscoveryResponse(packet.getAddress(), packet.getPort());
                        System.out.println("Received discovery request from " + packet.getAddress() + ". From port " + packet.getPort());

                        ipAddresses.put(packet.getAddress().getHostAddress(), "test");
                        System.out.println(ipAddresses);
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
            System.out.println("Sent discovery response to " + address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // UDP join method (not functionality) needs to be revised
    public void join(NodeJoinListener listener) {
        // Broadcast discovery message
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress group = InetAddress.getByName("224.0.0.1");
            String message = "DISCOVER";
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, DISCOVERY_PORT);
            socket.send(packet);



            // Notify listener about join attempt
            listener.onNodeJoin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TCP CONNECTION IN DEVELOPMENT

    public void tcpJoin(String existingNodeIP, int existingNodePort) {
        try (Socket socket = new Socket(existingNodeIP, existingNodePort);
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {

            // Send join request
            outputStream.writeObject(new JoinRequest(nodeId));

            // Receive response
            Object response = inputStream.readObject();

            if (response instanceof JoinResponse) {
                JoinResponse joinResponse = (JoinResponse) response;
                this.successor = joinResponse.getSuccessor();
                this.predecessor = this.successor.getPredecessor();
                this.successor.setPredecessor(this);
                updateFingerTable();
            } else {
                // Handle unexpected response
                System.err.println("Unexpected response received during join");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // END OF TCP CONNECTION IN DEVELOPMENT

    private void updateFingerTable() {
        // Implement logic to update the finger table based on the successor node
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

    public Map<String, String> getIpAddresses() {
        return ipAddresses;
    }

    public void addJoinListener(NodeJoinListener listener) {
        joinListeners.add(listener);
    }

    private void notifyNodeJoinListeners() {
        for (NodeJoinListener listener : joinListeners) {
            listener.onNodeJoin();
        }
    }

}

