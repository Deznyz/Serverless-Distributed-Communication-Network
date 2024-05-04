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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize CHORD network
        node = new ChordNode(MainActivity.this); // Assuming the first node has ID 0
        ipAddressesTextView = findViewById(R.id.ipAddressesTextView); // Initialize TextView
        node.addJoinListener(this); // Register MainActivity as a listener for join attempts
        new JoinNetworkTask().execute();
    }

    private class JoinNetworkTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Perform CHORD network joining operation
                node.join(MainActivity.this); // Joining the network
                return true; // Network joined successfully

            } catch (Exception e) {
                e.printStackTrace();
                return false; // Failed to join the network
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            // Update UI based on the success or failure of the network joining operation
            if (success) {
                // Network joined successfully
                // Update UI with success message
                Toast.makeText(MainActivity.this, "Network joined successfully!", Toast.LENGTH_SHORT).show();
                System.out.println("Success");

            } else {
                // Failed to join the network
                // Display error message to the user
                Toast.makeText(MainActivity.this, "Failed to join the network. Please try again.", Toast.LENGTH_SHORT).show();
                System.out.println("Failure");
            }
        }
    }

    private void updateIpAddressesDisplay(Map<String, String> ipAddresses) {
        // Prepare a StringBuilder to construct the text to be displayed
        StringBuilder stringBuilder = new StringBuilder();

        // Iterate over the IP addresses and append them to the StringBuilder
        for (Map.Entry<String, String> entry : ipAddresses.entrySet()) {
            stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        // Set the text of the TextView to the constructed string
        ipAddressesTextView.setText(stringBuilder.toString());
    }

    @Override
    public void onNodeJoin() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will run on the main/UI thread
                Toast.makeText(MainActivity.this, "Node joined the network!", Toast.LENGTH_SHORT).show();
                // Update IP addresses display
                updateIpAddressesDisplay(node.getIpAddresses());
            }
        });
    }

}
