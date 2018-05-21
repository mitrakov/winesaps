package ru.mitrakov.self.rush.stat;

import java.util.Locale;
import ru.mitrakov.self.rush.net.IHandler;
import ru.mitrakov.self.rush.GcResistantIntArray;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.utils.SimpleLogger.log;

/**
 * StatParser is used to parse statistic messages.
 * <br>Class is intended to have a single instance
 * @author Mitrakov
 * @see Stat
 */
class ParserStat implements IHandler {
    /** Helper array to avoid "new" operations and decrease GC pressure */
    private final IIntArray array = new GcResistantIntArray(256);
    /** Statistics Screen */
    private /*final*/ ScreenStat screen;

    @Override
    public synchronized void onReceived(IIntArray data) {
        while (data.length() > 2) {
            int len = data.get(0) * 256 + data.get(1);
            processMsg(array.copyFrom(data.remove(0, 2), len));
            data.remove(0, len);
        }
    }

    @Override
    public void onChanged(boolean connected) {
        screen.setConnected(connected);
    }

    /**
     * Sets current app screen
     * @param screen screen
     */
    void setScreen(ScreenStat screen) {
        this.screen = screen;
    }

    /**
     * Parses a single message (note that the incoming byte array may consist of several single messages)
     * @param data single message byte array
     */
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
            } else if (code == 0xF1) {
                String str = data.remove(0, 2).toUTF8();
                String msg = String.format(Locale.getDefault(), "Server response: code=%d; data=%s", error, str);
                screen.showMessage(msg);
            }
        } else throw new IllegalArgumentException("Data too short");
    }
}
