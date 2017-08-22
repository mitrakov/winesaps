package ru.mitrakov.self.rush;

import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.assets.AssetManager;

/**
 * Simple Gdx-specific wrapper to handle all Music and Sound instances. Supports "mute" operations.
 * This class is intended to have a single instance
 * @author mitrakov
 */
@SuppressWarnings("WeakerAccess")
public class AudioManager {

    private final AssetManager assetManager;
    private final ObjectMap<String, String> soundNames = new ObjectMap<String, String>(16); // to decrease GC pressure
    private boolean musicMuted = false;
    private boolean soundMuted = false;
    private Music curMusic;
    private String curMusicName = "";

    /**
     * Creates a new instance of AudioManager
     * @param assetManager - Asset Manager (NON-NULL)
     * @param musicMuted - starting mute state for music (default is false)
     * @param soundMuted - starting mute state for sounds (default is false)
     */
    public AudioManager(AssetManager assetManager, boolean musicMuted, boolean soundMuted) {
        assert assetManager != null;
        this.assetManager = assetManager;
        this.musicMuted = musicMuted;
        this.soundMuted = soundMuted;
    }

    /**
     * Plays the music. If the other music instance is already playing, it will be stopped
     * @param name - name of a music asset WITHOUT any paths and extensions like ".mp3" and so on
     * @param loop - loop flag
     */
    public void music(String name, boolean loop) {
        assert name != null;
        if (!curMusicName.equals(name)) {
            if (curMusic != null) {
                /*curMusic.stop(); see note#7 below*/ curMusic.pause(); curMusic.setPosition(0);
            }
            curMusic = assetManager.get(String.format("music/%s.mp3", name));
            if (curMusic != null) {
                curMusicName = name;
                curMusic.setVolume(.4f);
                curMusic.setLooping(loop);
                if (!musicMuted)
                    curMusic.play();
            }
        }
    }

    /**
     * Plays the sound. If the other sound instance is already playing, both of them will be played simultaneously.
     * Please ensure your SFX is less than 1 Mb
     * @param name - name of a sound asset WITHOUT any paths and extensions like ".wav" and so on
     */
    public void sound(String name) {
        if (!soundMuted) {
            String path = soundNames.get(name);
            if (path == null) {
                path = String.format("sfx/%s.wav", name);
                soundNames.put(name, path);
            }
            assetManager.<Sound>get(path).play();
        }
    }

    /**
     * Turns the music on/off
     * @param value - true to mute music
     */
    public void muteMusic(boolean value) {
        musicMuted = value;
        if (curMusic != null) {
            if (musicMuted)
                curMusic.pause();
            else curMusic.play();
        }
    }

    /**
     * Turns the sounds on/off
     * @param value - true to mute sounds
     */
    public void muteSound(boolean value) {
        soundMuted = value;
    }

    /**
     * Turns the music and sounds on/off
     * @param value - true to mute music and sounds
     */
    public void muteAll(boolean value) {
        muteMusic(value);
        muteSound(value);
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
