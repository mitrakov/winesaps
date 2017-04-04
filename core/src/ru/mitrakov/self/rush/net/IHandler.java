package ru.mitrakov.self.rush.net;

public interface IHandler {
    void onReceived(int[] data);
    void onChanged(boolean connected);
}
