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
                    ChordNode predecessor = (successor != null) ? successor.getPredecessor() : null;
                    JoinResponse joinResponse = new JoinResponse(successor, predecessor);
                    outputStream.writeObject(joinResponse);
                    outputStream.flush();
                    System.out.println("Processed JoinRequest from node: " + joinRequest.getNodeId());
                } else if (request instanceof FindSuccessorRequest) {
                    FindSuccessorRequest fsRequest = (FindSuccessorRequest) request;
                    ChordNode successor = node.findSuccessor(fsRequest.getNodeId());
                    outputStream.writeObject(new FindSuccessorResponse(successor));
                    outputStream.flush();
                    System.out.println("Processed FindSuccessorRequest for node: " + fsRequest.getNodeId());
                } else if (request instanceof LeaveRequest) {
                    LeaveRequest leaveRequest = (LeaveRequest) request;
                    if (node.getNodeId() == leaveRequest.getNodeId()) {
                        node.leave();
                        outputStream.writeObject(new LeaveResponse(true));
                        outputStream.flush();
                        System.out.println("Processed LeaveRequest for node: " + leaveRequest.getNodeId());
                    } else {
                        outputStream.writeObject(new LeaveResponse(false));
                        outputStream.flush();
                        System.err.println("Error: LeaveRequest for non-existing node");
                    }
                } else {
                    System.err.println("Error: Invalid request received");
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
