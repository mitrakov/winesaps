package ru.mitrakov.self.rush.net;

public interface IHandler {
    void onConnected();
    void onReceived(int[] data);
    void onConnectionFailed();
}
