package ru.mitrakov.self.rush;

import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.assets.AssetManager;

/**
 * Created by mitrakov on 06.04.2017
 */
@SuppressWarnings("WeakerAccess")
public class AudioManager {

    private final AssetManager assetManager;
    private boolean muted = false;
    private Music curMusic;
    private String curMusicName = "";

    public AudioManager(AssetManager assetManager) {
        assert assetManager != null;
        this.assetManager = assetManager;
    }

    public synchronized void music(String name, boolean loop) {
        assert name != null;
        if (!muted && !curMusicName.equals(name)) {
            if (curMusic != null) {
                /*curMusic.stop(); see note#7 below*/ curMusic.pause(); curMusic.setPosition(0);
            }
            curMusic = assetManager.get(String.format("music/%s.mp3", name));
            if (curMusic != null) {
                curMusicName = name;
                curMusic.setVolume(.3f);
                curMusic.setLooping(loop);
                curMusic.play();
            }
        }
    }

    public void sound(String name) {
        if (!muted)
            assetManager.<Sound>get(String.format("sfx/%s.wav", name)).play();
    }

    public void mute(boolean value) {
        muted = value;
        if (curMusic != null) {
            if (muted)
                curMusic.pause();
            else curMusic.play();
        }
    }
}

// note#5 (@mitrakov, 2017-05-03): NOT ACTUAL ANYMORE (2017-07-06)
//
// note#7 (@mitrakov (2017-07-18): possible bug in LibGDX on Android. When the display is switched off and then it
// switches on again, "stop()" method doesn't work! It just RESTARTS the music again! As a result there are 2 music
// instances started to play simultaneously.
// However (!) "pause()" works correctly! So I replaced "stop()" with "pause()+setPosition(0)"
// Please see my question on https://stackoverflow.com/questions/45165572
//