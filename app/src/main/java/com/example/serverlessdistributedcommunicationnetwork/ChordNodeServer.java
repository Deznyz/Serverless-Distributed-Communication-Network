package com.example.serverlessdistributedcommunicationnetwork;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

public class ChordNodeServer {
    private int port;
    private ChordNode node;

    public ChordNodeServer(int port, ChordNode node) {
        this.port = port;
        this.node = node;
    }

    public void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(new JoinHandler(clientSocket, node)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static class JoinHandler implements Runnable {
        private Socket clientSocket;
        private ChordNode node;

        public JoinHandler(Socket clientSocket, ChordNode node) {
            this.clientSocket = clientSocket;
            this.node = node;
        }

        @Override
        public void run() {
            try (ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                 ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream())) {

                Object request = inputStream.readObject();

                if (request instanceof JoinRequest) {
                    JoinRequest joinRequest = (JoinRequest) request;
                    ChordNode successor = node.findSuccessor(joinRequest.getNodeId());
                    successor.removeAllJoinListeners();
                    successor.setSuccessor(successor.findSuccessor(successor.getNodeId()));
                    ChordNode predecessor = (successor != null) ? successor.getPredecessor() : null;
                    JoinResponse joinResponse = new JoinResponse(successor, predecessor);
                    outputStream.writeObject(joinResponse);
                    outputStream.flush();
                    System.out.println("Processed JoinRequest from node: " + joinRequest.getNodeId());
                } else {
                    System.err.println("Error: Invalid request received");
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}

class FindSuccessorRequest implements Serializable {
    private int nodeId;

    public FindSuccessorRequest(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeId() {
        return nodeId;
    }
}

class FindSuccessorResponse implements Serializable {
    private ChordNode successor;

    public FindSuccessorResponse(ChordNode successor) {
        this.successor = successor;
    }

    public ChordNode getSuccessor() {
        return successor;
    }
}
