package ru.mitrakov.self.rush;

/**
 * Created by mitrakov on 01.03.2017
 */

@SuppressWarnings("WeakerAccess")
public class PsObject {

    public interface Listener {
        void onRatioChanged(float ratio);
    }

    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void raiseRatioChanged(float ratio) {
        if (listener != null)
            listener.onRatioChanged(ratio);
    }

    public void hide() {
    }

    public void activate() {
    }
}
