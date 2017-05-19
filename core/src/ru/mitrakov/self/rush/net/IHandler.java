package ru.mitrakov.self.rush.net;

import ru.mitrakov.self.rush.utils.collections.IIntArray;

public interface IHandler {
    void onReceived(IIntArray data);
    void onChanged(boolean connected);
}
