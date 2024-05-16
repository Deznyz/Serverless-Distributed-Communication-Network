package com.example.serverlessdistributedcommunicationnetwork;

//interface for used in the TCP connection for ui and node updates
public interface JoinResponseListener {
    void onJoinResponseReceived(JoinResponse response);
}
