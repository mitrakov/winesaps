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

    public void music(String name, boolean loop) {
        assert name != null;
        if (!muted && !curMusicName.equals(name)) {
            if (curMusic != null)
                curMusic.stop();
            curMusic = assetManager.get(String.format("music/%s.mp3", name));
            if (curMusic != null) {
                curMusicName = name;
                curMusic.setVolume(.2f);
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
