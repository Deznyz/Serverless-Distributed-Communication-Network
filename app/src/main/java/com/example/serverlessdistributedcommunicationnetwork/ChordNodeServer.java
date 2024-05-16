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
        //new thread that keeps listening
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

                //stores the request
                Object request = inputStream.readObject();

                //makes sure its the right formatting
                if (request instanceof JoinRequest) {

                    //stores it as right formatting
                    JoinRequest joinRequest = (JoinRequest) request;

                    //gets the successor to the given nodeId
                    ChordNode successor = node.findSuccessor(joinRequest.getNodeId());
                    //removes the join listeners of the successor due to serialization problems with MainActivity
                    successor.removeAllJoinListeners();
                    //done in order to remove the join listeners of the successors' successor due to serialization problems with MainActivity
                    successor.setSuccessor(successor.findSuccessor(successor.getNodeId()));

                    ChordNode predecessor = (successor != null) ? successor.getPredecessor() : null;

                    //sends back the join respone
                    JoinResponse joinResponse = new JoinResponse(successor, predecessor);
                    outputStream.writeObject(joinResponse);
                    outputStream.flush();
                    System.out.println("Processed JoinRequest from node: " + joinRequest.getNodeId());


                    //below (FindSuccessorRequest) is not in use yet
                } else if (request instanceof FindSuccessorRequest) {
                    FindSuccessorRequest fsRequest = (FindSuccessorRequest) request;
                    ChordNode successor = node.findSuccessor(fsRequest.getNodeId());
                    outputStream.writeObject(new FindSuccessorResponse(successor));
                    outputStream.flush();
                    System.out.println("Processed FindSuccessorRequest for node: " + fsRequest.getNodeId());
                } else {
                    System.err.println("Error: Invalid request received");
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}


//rest of code is an idea for later (very simple implementation and not operable yet)
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
