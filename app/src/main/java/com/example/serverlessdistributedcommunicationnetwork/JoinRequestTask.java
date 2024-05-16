package com.example.serverlessdistributedcommunicationnetwork;

import android.os.AsyncTask;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class JoinRequestTask extends AsyncTask<String, Void, JoinResponse> {
    private ChordNode node;
    private JoinResponseListener joinResponseListener;

    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public JoinRequestTask(ChordNode node, JoinResponseListener listener) {
        this.node = node;
        this.joinResponseListener = listener;
    }

    @Override
    protected JoinResponse doInBackground(String... params) {
        String ipAddress = params[0];
        int port = Integer.parseInt(params[1]);
        JoinRequest joinRequest = new JoinRequest(node.getNodeId());

        try {
            // Establish the TCP connection
            socket = new Socket(ipAddress, port);

            // Get output stream to send data
            outputStream = new ObjectOutputStream(socket.getOutputStream());

            // Get input stream to receive data
            inputStream = new ObjectInputStream(socket.getInputStream());

            // Send join request
            outputStream.writeObject(joinRequest);
            outputStream.flush();

            // Read response
            Object response = inputStream.readObject();

            // Check if correct formatting
            if (response instanceof JoinResponse) {
                return (JoinResponse) response;
            } else {
                // Not correct formatting
                System.err.println("Error: Invalid response received");
                return null;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    //close the connection after handling the task
    protected void closeConnection(){

        try {
            if (outputStream != null) outputStream.close();
            if (inputStream != null) inputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onPostExecute(JoinResponse joinResponse) {
        // Handle the response in MainActivity
        if (joinResponseListener != null) {
            joinResponseListener.onJoinResponseReceived(joinResponse);
        } else {
            System.out.println("nodeJoinListener is null");
        }


    }
}
