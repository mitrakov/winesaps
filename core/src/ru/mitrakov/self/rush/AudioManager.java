package ru.mitrakov.self.rush;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.files.FileHandle;

/**
 * Created by mitrakov on 06.04.2017
 */
@SuppressWarnings("WeakerAccess")
public class AudioManager {
    private final ObjectMap<String, Sound> sounds = new ObjectMap<String, Sound>(10);
    private Music curMusic;

    public AudioManager(String defaultMusic) {
        for (FileHandle handle : Gdx.files.internal("wav").list()) {
            sounds.put(handle.nameWithoutExtension(), Gdx.audio.newSound(handle));
        }
        if (defaultMusic != null)
            music(defaultMusic);
    }

    public void music(String name) {
        assert name != null;
        if (curMusic != null)
            curMusic.dispose();
        curMusic = Gdx.audio.newMusic(Gdx.files.internal(String.format("tune/%s.mp3", name)));
        if (curMusic != null) {
            curMusic.setVolume(.2f);
            curMusic.setLooping(true);
            curMusic.play();
        } else throw new RuntimeException(String.format("Music %s not found", name));
    }

    public void pauseMusic(boolean pause) {
        if (curMusic != null) {
            if (pause)
                curMusic.pause();
            else curMusic.play();
        }
    }

    public void sound(String name) {
        assert name != null;
        if (sounds.containsKey(name))
            sounds.get(name).play();
        else throw new RuntimeException(String.format("Sound %s not found", name));
    }

    public void dispose() {
        if (curMusic != null)
            curMusic.dispose();
        for (Sound sound : sounds.values()) {
            sound.dispose();
        }
    }
}
