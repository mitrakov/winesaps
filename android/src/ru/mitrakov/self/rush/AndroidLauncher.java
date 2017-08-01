package ru.mitrakov.self.rush;

import android.os.*;
import android.view.View;
import android.graphics.Rect;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.*;

import ru.mitrakov.self.rush.stat.Stat;

public class AndroidLauncher extends AndroidApplication {
    private /*final*/ PsObject obj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Winesaps game = new Winesaps(obj = new AndroidPsObject(this));
        // final Stat game = new Stat(obj = new AndroidPsObject(this));

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useCompass = false;
        config.useAccelerometer = false;
        config.useGyroscope = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) { // addOnLayoutChangeListener requires Level API 11
            getWindow().getDecorView().getRootView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bot, int a, int b, int c, int d) {
                    final Rect rect = new Rect();
                    v.getWindowVisibleDisplayFrame(rect);
                    Gdx.app.postRunnable(new Runnable() { // @mitrakov: it is necessary to avoid OutOfSync exceptions!
                        @Override
                        public void run() {
                            game.setRatio(1f * rect.width() / rect.height());
                        }
                    });
                }
            });
        }

        initialize(game, config);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (obj != null)
            obj.setActive(false);
        Gdx.graphics.requestRendering(); // BUG in LibGDX! (http://badlogicgames.com/forum/viewtopic.php?f=11&t=17257)
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (obj != null)
            obj.setActive(true);
    }
}
