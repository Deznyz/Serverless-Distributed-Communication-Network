package com.example.serverlessdistributedcommunicationnetwork;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NodeJoinListener {
    private ChordNode node;
    private TextView ipAddressesTextView;
    private ChordNodeServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //---INITIALIZE CHORD NETWORK---
        //create the node
        node = new ChordNode(MainActivity.this);

        ipAddressesTextView = findViewById(R.id.ipAddressesTextView);


        //MainActivity as a listener for join attempts
        node.addJoinListener(this);

        System.out.println("1");
        new Thread(() -> {
            server = new ChordNodeServer(5000, node);
            server.startServer();
        }).start();
        System.out.println("2");
        //new thread with join logic
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

}
