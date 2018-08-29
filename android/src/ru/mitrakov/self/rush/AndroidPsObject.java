package ru.mitrakov.self.rush;

import android.os.*;
import android.app.*;
import android.content.*;
import android.provider.Settings;

import java.util.Locale;

import com.badlogic.gdx.Gdx;

/**
 * Platform Specific Object for Android platform
 */
class AndroidPsObject extends PsObject {
    /** Android Activity */
    private final Activity activity;
    /** Thread for a {@link #handler} */
    private final HandlerThread thread;
    /** Timer implementation for Android (don't use TimerTask, see details <a href="https://stackoverflow.com/questions/20330355">here</a>)*/
    private final Handler handler;

    /** Active flag */
    private boolean active = true;

    /**
     * Creates a new instance of PsObject for Android platform
     * @param activity Android Activity
     */
    AndroidPsObject(Activity activity) {
        // super(new AmazonBillingProvider(activity));
        super(new GooglePlayBillingProvider(activity));

        assert activity != null;
        this.activity = activity;
        thread = new HandlerThread("psObject"); // https://stackoverflow.com/questions/18856376
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    @Override
    public String getPlatform() {
        // G = Google Play, W = Web
        return String.format(Locale.getDefault(), "W.%s.%d", Gdx.app.getType(), Gdx.app.getVersion());
    }

    @Override
    public void hide() {
        activity.moveTaskToBack(true);
    }

    @Override
    public void setActive(boolean value) {
        active = value;
        if (active) {
            NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null)
                manager.cancel(1); // cancel our notification with ID=1
        }
    }

    @Override
    public void pushNotification(String msg, boolean force) {
        if (!active || force) { // if the app is active => no need to push notifications
            NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(activity, activity.getClass());
            PendingIntent pIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) { // Notification.Builder uses API 11
                Notification.Builder builder = new Notification.Builder(activity);
                builder.setContentIntent(pIntent);
                builder.setContentTitle("Winesaps");
                builder.setContentText(msg);
                builder.setSmallIcon(R.drawable.winesaps);
                builder.setDefaults(Notification.DEFAULT_ALL); // default sound, vibrate pattern and lights
                if (manager != null)
                    manager.notify(1, builder.getNotification()); //"getNotification" deprecated but "build" uses API16
            } else try {
                pIntent.send();
            } catch (PendingIntent.CanceledException ignored) {
            }
        }
    }

    @Override
    public String getKeyboardVendor() {
        return Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
    }

    @Override
    public void runDaemon(int delayMsec, final int periodMsec, final Runnable f) {
        handler.postDelayed(new Runnable() { // @mitrakov 2017-07-17: https://stackoverflow.com/questions/20330355
            @Override
            public void run() {
                f.run();
                handler.postDelayed(this, periodMsec);
            }
        }, delayMsec);
    }

    @Override
    public void runTask(int delayMsec, Runnable f) {
        handler.postDelayed(f, delayMsec); // @mitrakov 2017-07-17: https://stackoverflow.com/questions/20330355
    }

    @SuppressWarnings("unused")
    void stop() {
        thread.quit();
    }
}
