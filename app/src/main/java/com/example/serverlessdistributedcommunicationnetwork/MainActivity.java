package com.example.serverlessdistributedcommunicationnetwork;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    private ChordNode node;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize CHORD network
        node = new ChordNode(0); // Assuming the first node has ID 0
        new JoinNetworkTask().execute();
    }

    private class JoinNetworkTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Perform CHORD network joining operation
                node.join(null); // Joining the network
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
                System.out.println("Success");
                Toast.makeText(MainActivity.this, "Network joined successfully!", Toast.LENGTH_SHORT).show();

            } else {
                // Failed to join the network
                // Display error message to the user
                System.out.println("Failure");
                Toast.makeText(MainActivity.this, "Failed to join the network. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
