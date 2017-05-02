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

    private boolean muted = false;
    private Music curMusic;
    private String curMusicName = "";

    public AudioManager(String defaultMusic) {
        // @mitrakov (2017-05-02) The way:
        // FileHandle[] wavs = Gdx.files.internal("wav").list();
        // doesn't work when get packed to a FAT Jar (https://github.com/libgdx/libgdx/issues/1375)
        // This is my own question on StackOverflow: stackoverflow.com/questions/43742762
        // so the best way is to enumerate them manually
        FileHandle[] wavs = new FileHandle[] {
                Gdx.files.internal("wav/ability.wav"),
                Gdx.files.internal("wav/click.wav"),
                Gdx.files.internal("wav/die.wav"),
                Gdx.files.internal("wav/food.wav"),
                Gdx.files.internal("wav/game.wav"),
                Gdx.files.internal("wav/Mine.wav"),
                Gdx.files.internal("wav/round.wav"),
                Gdx.files.internal("wav/thing.wav"),
                Gdx.files.internal("wav/Umbrella.wav"),
        };
        for (FileHandle handle : wavs) {
            sounds.put(handle.nameWithoutExtension(), Gdx.audio.newSound(handle));
        }
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

    public void mute(boolean value) {
        muted = value;
        if (curMusic != null) {
            if (muted)
                curMusic.pause();
            else curMusic.play();
        }
    }

    public void sound(String name) {
        assert name != null;
        if (!muted) {
            if (sounds.containsKey(name))
                sounds.get(name).play();
            else throw new RuntimeException(String.format("Sound %s not found", name));
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
