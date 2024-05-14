package com.example.serverlessdistributedcommunicationnetwork;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.*;

public class ChordNodeServer {
    private int port;
    private ChordNode node;

    public ChordNodeServer(int port, ChordNode node) {
        this.port = port;
        this.node = node;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                System.out.println("1.1");
                Socket clientSocket = serverSocket.accept();
                System.out.println("1.2");
                new Thread(new JoinHandler(clientSocket, node)).start();
                System.out.println("1.3");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    ChordNode predecessor = successor.getPredecessor();

                    JoinResponse joinResponse = new JoinResponse(successor, predecessor);
                    outputStream.writeObject(joinResponse);
                    outputStream.flush();
                } else if (request instanceof FindSuccessorRequest) {
                    FindSuccessorRequest fsRequest = (FindSuccessorRequest) request;
                    ChordNode successor = node.findSuccessor(fsRequest.getNodeId());
                    outputStream.writeObject(new FindSuccessorResponse(successor));
                    outputStream.flush();
                } else {
                    System.err.println("Error: Invalid request received");
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}

// Serializable classes for find successor request and response
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


