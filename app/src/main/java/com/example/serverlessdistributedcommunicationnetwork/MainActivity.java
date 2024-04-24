package com.example.serverlessdistributedcommunicationnetwork;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    // Define constants for the number of peers and maximum identifier
    private static final int N_PEERS = 10;
    private static final int MAX_IDENTIFIER = 1000;

    // Array to hold peer objects
    private Peer[] peers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the array of peers
        peers = new Peer[N_PEERS];

        // Create and initialize each peer with a random identifier
        for (int i = 0; i < N_PEERS; i++) {
            int randomId = (int) (Math.random() * MAX_IDENTIFIER);
            peers[i] = new Peer(randomId);
        }

        // Example: Joining a peer to the network
        Peer peerToJoin = peers[0]; // Assuming the first peer is joining
        peerToJoin.join(null); // For simplicity, passing null as existing peer
    }

    // Other methods for handling user interactions, message exchange, etc.
}