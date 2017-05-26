package ru.mitrakov.self.rush;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Created by mitrakov on 06.04.2017
 */
@SuppressWarnings("WeakerAccess")
public class AudioManager {
    private final ObjectMap<String, Sound> sounds = new ObjectMap<String, Sound>(10); // to avoid memory allocations

    private boolean muted = false;
    private Music curMusic;
    private String curMusicName = "";

    public AudioManager(String defaultMusic) {
        if (defaultMusic != null)
            music(defaultMusic);
    }

    public void music(String name) {
        assert name != null;
        if (!muted && !curMusicName.equals(name)) {
            if (curMusic != null)
                curMusic.dispose();
            curMusic = Gdx.audio.newMusic(Gdx.files.internal(String.format("tune/%s.mp3", name)));
            if (curMusic != null) {
                curMusicName = name;
                curMusic.setVolume(.2f);
                curMusic.setLooping(true);
                curMusic.play();
            } else throw new RuntimeException(String.format("Music %s not found", name));
        }
    }

    public void sound(String name) {
        // see note#5 below
        assert name != null;
        if (!muted) {
            if (sounds.containsKey(name))
                sounds.get(name).play();
            else {
                Sound sound = Gdx.audio.newSound(Gdx.files.internal(String.format("wav/%s.wav", name)));
                sounds.put(name, sound);
                sound.play(.7f);
            }
        }
    }

    public void mute(boolean value) {
        muted = value;
        if (curMusic != null) {
            if (muted)
                curMusic.pause();
            else curMusic.play();
        }
    }

    public void dispose() {
        if (curMusic != null)
            curMusic.dispose();
        for (Sound sound : sounds.values()) {
            sound.dispose();
        }
    }
}

// note#5 (@mitrakov, 2017-05-03): firstly for sounds I used a map of pre-loaded resources:
//   for (FileHandle handle : Gdx.files.internal("wav").list()) {
//       sounds.put(handle.nameWithoutExtension(), Gdx.audio.newSound(handle));
//   }
// it works properly on Android, and on Desktop if assets are extracted to working directory.
// ... but this way doesn't work with FAT jars od Desktop! (list() returns an empty array)
// proof1: https://github.com/libgdx/libgdx/issues/1375
// proof2: https://github.com/libgdx/libgdx/wiki/File-handling#user-content-listing-and-checking-properties-of-files
// my question on StackOverflow: http://stackoverflow.com/questions/43742762
// so don't use list() on internal files in LibGdx
