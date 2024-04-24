package com.example.serverlessdistributedcommunicationnetwork;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.webrtc.CandidatePairChangeEvent;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnection.Observer;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE = 1;

    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;
    private DataChannel dataChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request necessary permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, REQUEST_CODE);
        } else {
            initializePeerConnection();
        }
    }

    private void initializePeerConnection() {
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions());
        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory();

        List<PeerConnection.IceServer> iceServers = new LinkedList<>();
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());

        peerConnection = peerConnectionFactory.createPeerConnection(iceServers, new CustomPeerConnectionObserver("peerConnection") {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                // Send local ICE candidate to the remote peer
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                super.onDataChannel(dataChannel);
                MainActivity.this.dataChannel = dataChannel;
                dataChannel.registerObserver(new CustomDataChannelObserver("dataChannel"));
            }
        });

        // Create DataChannel
        DataChannel.Init init = new DataChannel.Init();
        init.ordered = true;
        dataChannel = peerConnection.createDataChannel("dataChannel", init);
        dataChannel.registerObserver(new CustomDataChannelObserver("dataChannel"));

        // Create Offer
        peerConnection.createOffer(new CustomSdpObserver("createOffer") {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                peerConnection.setLocalDescription(new CustomSdpObserver("setLocalDescription"), sessionDescription);
                // Send session description to the remote peer
            }
        }, new MediaConstraints());
    }

    // Custom implementation of PeerConnection.Observer
    // Custom implementation of PeerConnection.Observer
    // Custom implementation of PeerConnection.Observer
    // Custom implementation of PeerConnection.Observer
    private static class CustomPeerConnectionObserver implements PeerConnection.Observer {
        private final String TAG;

        CustomPeerConnectionObserver(String tag) {
            this.TAG = tag;
        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            Log.d(TAG, "onIceCandidate: " + iceCandidate);
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            Log.d(TAG, "onDataChannel: " + dataChannel.label());
        }

        // Implement the empty onAddStream method
        @Override
        public void onAddStream(MediaStream mediaStream) {
            // Deprecated method, no action needed
        }

        // Implement the empty onRemoveStream method
        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            // Deprecated method, no action needed
        }

        // Implement the empty onRenegotiationNeeded method
        @Override
        public void onRenegotiationNeeded() {
            // No action needed
        }

        // Implement the empty onAddTrack method
        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
            // No action needed
        }

        // Implement other overridden methods as needed
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {

        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

        }

        @Override
        public void onConnectionChange(PeerConnection.PeerConnectionState newState) {

        }

        @Override
        public void onSelectedCandidatePairChanged(CandidatePairChangeEvent event) {

        }
    }




    // Custom implementation of SdpObserver
    private static class CustomSdpObserver implements SdpObserver {
        private final String TAG;

        CustomSdpObserver(String tag) {
            this.TAG = tag;
        }

        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Log.d(TAG, "onCreateSuccess: " + sessionDescription);
        }

        // Implement other overridden methods as needed
        @Override
        public void onSetSuccess() {

        }

        @Override
        public void onCreateFailure(String s) {

        }

        @Override
        public void onSetFailure(String s) {

        }
    }

    // Custom implementation of DataChannel.Observer
    private static class CustomDataChannelObserver implements DataChannel.Observer {
        private final String TAG;

        CustomDataChannelObserver(String tag) {
            this.TAG = tag;
        }

        @Override
        public void onMessage(DataChannel.Buffer buffer) {
            Log.d(TAG, "onMessage: " + buffer.toString());
        }

        // Implement other overridden methods as needed
        @Override
        public void onBufferedAmountChange(long l) {

        }

        @Override
        public void onStateChange() {

        }
    }
}
