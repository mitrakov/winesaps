package ru.mitrakov.self.rush;

import java.util.Arrays;
import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.net.Network;
import static ru.mitrakov.self.rush.model.Model.*;

/**
 * Created by mitrakov on 23.02.2017
 */

class Parser implements Network.IHandler {



    private final Model model;

    public Parser(Model model) {
        assert model != null;
        this.model = model;
    }

    @Override
    public void handle(int[] data) {
        // @mitrakov: on Android copyOfRange requires minSdkVersion=9
        System.out.println(Arrays.toString(data));
        if (data.length > 0) {
            int code = data[0];
            switch (code) {
                case FULL_STATE:
                    fullState(Arrays.copyOfRange(data, 1, data.length));
                    break;
                case STATE_CHANGED:
                    stateChanged(Arrays.copyOfRange(data, 1, data.length));
                    break;
                default:
            }
        } else throw new IllegalArgumentException("Empty data");
    }

    private void fullState(int state[]) {
        int n = Field.HEIGHT * Field.WIDTH;
        if (state.length >= n) {
            int field[] = Arrays.copyOfRange(state, 0, n);
            int tail[] = Arrays.copyOfRange(state, n, state.length);

            model.setNewField(field);
            if (tail.length % 3 == 0) {
                for (int i = 0; i < tail.length; i += 3) {
                    int number = tail[i];
                    int id = tail[i + 1];
                    int xy = tail[i + 2];
                    model.appendObject(number, id, xy);
                }
            } else throw new IllegalArgumentException("Incorrect tail data format");
        } else throw new IllegalArgumentException("Incorrect field size");
    }

    private void stateChanged(int pairs[]) {
        if (pairs.length % 2 == 0) {
            for (int i = 0; i < pairs.length; i += 2) {
                int number = pairs[i];
                int xy = pairs[i + 1];
                model.setXy(number, xy);
            }
        } else throw new IllegalArgumentException("Incorrect state changed format");
    }
}
