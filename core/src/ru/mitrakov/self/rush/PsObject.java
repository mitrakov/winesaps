package ru.mitrakov.self.rush;

/**
 * Created by mitrakov on 01.03.2017
 */

class PsObject {

    interface Listener {
        void onRatioChanged(float ratio);
    }

    private Listener listener;

    void setListener(Listener listener) {
        this.listener = listener;
    }

    void raiseRatioChanged(float ratio) {
        if (listener != null) {
            listener.onRatioChanged(ratio);
        }
    }
}
