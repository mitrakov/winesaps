package ru.mitrakov.self.rush;

import android.os.*;
import android.view.View;
import android.graphics.Rect;
import android.content.Intent;
import android.app.PendingIntent;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.*;

public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useCompass = false;
        config.useAccelerometer = false;
        config.useGyroscope = false;

        final PsObject obj = new PsObject(new AndroidBillingProvider(this)) {
            @Override
            public void hide() {
                moveTaskToBack(true);
            }

            @Override
            public void activate() {
                Intent intent = new Intent(getContext(), AndroidLauncher.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                try {
                    PendingIntent.getActivity(getContext(), 0, intent, 0).send();
                } catch (PendingIntent.CanceledException ignored) {
                }
            }
        };

        if (Build.VERSION.SDK_INT >= 11) { // "addOnLayoutChangeListener" requires Level API 11
            getWindow().getDecorView().getRootView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bot, int a, int b, int c, int d) {
                    Rect rect = new Rect();
                    v.getWindowVisibleDisplayFrame(rect);
                    obj.raiseRatioChanged(1f * rect.width() / rect.height());
                }
            });
        }

        initialize(new Winesaps(obj), config);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Gdx.graphics.requestRendering(); // BUG in LibGDX! (http://badlogicgames.com/forum/viewtopic.php?f=11&t=17257)
    }
}
