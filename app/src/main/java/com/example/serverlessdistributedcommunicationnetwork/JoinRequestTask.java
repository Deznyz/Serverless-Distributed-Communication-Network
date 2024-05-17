package com.example.serverlessdistributedcommunicationnetwork;

import android.os.AsyncTask;
import android.widget.Toast;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class JoinRequestTask extends AsyncTask<String, Void, JoinResponse> {
    private ChordNode node;
    private JoinResponseListener joinResponseListener;

    public JoinRequestTask(ChordNode node, JoinResponseListener listener) {
        this.node = node;
        this.joinResponseListener = listener;
    }

    @Override
    protected JoinResponse doInBackground(String... params) {
        String ipAddress = params[0];
        int port = Integer.parseInt(params[1]);
        JoinRequest joinRequest = new JoinRequest(node.getNodeId());

        try (Socket socket = new Socket(ipAddress, port);

             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {

            outputStream.writeObject(joinRequest);
            outputStream.flush();

            Object response = inputStream.readObject();

            if (response instanceof JoinResponse) {
                return (JoinResponse) response;
            } else {
                System.err.println("Error: Invalid response received");
                return null;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(JoinResponse joinResponse) {
        if (joinResponseListener != null) {
            joinResponseListener.onJoinResponseReceived(joinResponse);
        }else if (joinResponseListener == null){
            System.out.println("nodeJoinListener is null");
        }
    }
}
