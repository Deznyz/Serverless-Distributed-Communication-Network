package com.example.serverlessdistributedcommunicationnetwork;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NodeJoinListener, JoinResponseListener {
    private ChordNode node;
    private TextView ipAddressesTextView;
    private EditText ipAddressEditText;
    private EditText portEditText;
    private Button joinButton;
    private ChordNodeServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create the node
        node = new ChordNode(MainActivity.this);

        //---TO BE REPLACED WITH MORE INTERACTABLE AND USERFRIENDLY UI---

        ipAddressesTextView = findViewById(R.id.ipAddressesTextView);

        //used for the ability to connect to another ndoe
        ipAddressEditText = findViewById(R.id.ipAddressEditText);
        portEditText = findViewById(R.id.portEditText);
        joinButton = findViewById(R.id.joinButton);


        joinButton.setOnClickListener(v -> {
            String ipAddress = ipAddressEditText.getText().toString();
            String port = portEditText.getText().toString();

            if (!ipAddress.isEmpty() && !port.isEmpty()) {
                new JoinRequestTask(node, MainActivity.this).execute(ipAddress, port);
            } else {
                Toast.makeText(MainActivity.this, "Please enter both IP address and port", Toast.LENGTH_SHORT).show();
            }
        });

        //---END OF: TO BE REPLACED WITH MORE INTERACTABLE AND USERFRIENDLY UI---



        //MainActivity as a listener for join attempts
        node.addJoinListener(this);

        System.out.println("1"); //for debugging

        //thread for tcp connection server - uses port 5000
        new Thread(() -> {
            server = new ChordNodeServer(5000, node);
            server.startServer();
        }).start();



        System.out.println("2");//for debuggging
        //initial join logic -- needs to be revisited at some point (but works)
        new JoinNetworkTask().execute();
    }

    private class JoinNetworkTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                //join the network passingg MainActivity as a listener
                node.join(MainActivity.this);

                //for UI feedback on the phone
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                //for UI feedback on the phone
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (success) {
                //for UI feedback on the phone: SUCCESS
                Toast.makeText(MainActivity.this, "Network joined successfully!", Toast.LENGTH_SHORT).show();
                System.out.println("Success");

            } else {
                //for UI feedback on the phone: FAILURE
                Toast.makeText(MainActivity.this, "Failed to join the network. Please try again.", Toast.LENGTH_SHORT).show();
                System.out.println("Failure");
            }
        }
    }

    //updates the phone UI with a current list of discovered IP-addresses
    private void updateIpAddressesDisplay(Map<String, String> ipAddresses) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<String, String> entry : ipAddresses.entrySet()) {
            stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        //update textview
        ipAddressesTextView.setText(stringBuilder.toString());
    }

    //ui thread - triggered when new ip-addresses are added to the key-value map
    @Override
    public void onNodeJoin() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(MainActivity.this, "Node joined the network!", Toast.LENGTH_SHORT).show();

                //get current map
                updateIpAddressesDisplay(node.getIpAddresses());
            }
        });
    }

    //ui response + updating successor and predecessor
    public void onJoinResponseReceived(JoinResponse response) {
        if (response != null) {
            System.out.println("Join successful: " + response);
            node.setSuccessor(response.getSuccessor());
            node.setPredecessor(response.getPredecessor());

            //ui response
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Joined the network successfully!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            //error message in case of error i connecting
            System.err.println("Failed to join the network");

            //ui response
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Failed to join the network", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
