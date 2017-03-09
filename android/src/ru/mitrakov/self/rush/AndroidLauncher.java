package ru.mitrakov.self.rush;

import android.os.Bundle;
import android.view.View;
import android.graphics.Rect;

import com.badlogic.gdx.backends.android.*;

public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useCompass = false;
        config.useAccelerometer = false;
        config.useGyroscope = false;

        final PsObject obj = new PsObject();
        // @mitrakov: "addOnLayoutChangeListener" requires Level API 11
        getWindow().getDecorView().getRootView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() { //no NULL
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int a, int b, int c, int d) {
                Rect rect = new Rect();
                v.getWindowVisibleDisplayFrame(rect);
                System.out.println("New Size: " + rect.width() + "; " + rect.height());
                obj.raiseRatioChanged(1f * rect.width() / rect.height());
            }
        });

        initialize(new RushClient(obj), config);
    }
}
