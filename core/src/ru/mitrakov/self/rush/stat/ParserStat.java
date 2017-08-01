package ru.mitrakov.self.rush.stat;

import ru.mitrakov.self.rush.net.IHandler;
import ru.mitrakov.self.rush.GcResistantIntArray;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.utils.SimpleLogger.log;

/**
 * Created by mitrakov on 23.02.2017
 */
class ParserStat implements IHandler {
    private final IIntArray accessorial = new GcResistantIntArray(256);
    private /*final*/ ScreenStat screen;

    @Override
    public synchronized void onReceived(IIntArray data) {
        screen.onReceived();
        while (data.length() > 2) {
            int len = data.get(0) * 256 + data.get(1);
            processMsg(accessorial.copyFrom(data.remove(0, 2), len));
            data.remove(0, len);
        }
    }

    @Override
    public void onChanged(boolean connected) {
        screen.setConnected(connected);
    }

    void setScreen(ScreenStat screen) {
        this.screen = screen;
    }

    private void processMsg(IIntArray data) {
        log("Precessing:", data);
        if (data.length() > 1) {
            int code = data.get(0);
            int error = data.get(1);
            if (code == 0xF0) {
                if (error == 0) {
                    for (int i = 2; i + 2 < data.length(); i += 3) {
                        int category = data.get(i);
                        int value = data.get(i + 1) * 256 + data.get(i + 2);
                        screen.setValue(category, value);
                    }
                } else throw new RuntimeException("Statistics error: " + error);
            }
        } else throw new IllegalArgumentException("Data too short");
    }
}
