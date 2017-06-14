package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Created by mitrakov on 14.06.2017
 */

public class AnimationData<T> {

    public enum AnimationType {Idle, Run, Crawl, Enter, Leave}

    static class AnimChar {

        public ObjectMap<AnimationType, Animation<TextureRegion>> animations =
                new ObjectMap<AnimationType, Animation<TextureRegion>>(AnimationType.values().length);

        public AnimChar(TextureAtlas atlas, float frameDuration) {
            for (AnimationType type : AnimationType.values()) {
                Animation.PlayMode mode = type == AnimationType.Enter || type == AnimationType.Leave
                        ? Animation.PlayMode.NORMAL
                        : Animation.PlayMode.LOOP;
                Array<TextureAtlas.AtlasRegion> frames = atlas.findRegions(type.name());
                Animation<TextureRegion> animation = new Animation<TextureRegion>(frameDuration, frames, mode);
                animations.put(type, animation);
            }
        }
    }

    final float speedX;
    float x, y;
    int delay;
    boolean dirRight = true;
    private AnimationType curType = AnimationType.Run;
    private float t;

    public ObjectMap<T, AnimChar> animChars = new ObjectMap<T, AnimChar>();

    public AnimationData(float speedX) {
        this.speedX = speedX;
    }

    public AnimationData add(T key, TextureAtlas atlas, float frameDuration) {
        animChars.put(key, new AnimChar(atlas, frameDuration));
        return this;
    }

    public void addDt(float dt) {
        t += dt;
    }

    public void setAnimationType(AnimationType type) {
        curType = type;
        t = 0;
    }

    public TextureRegion getFrame(T key) {
        AnimChar animChar = animChars.get(key);
        if (animChar != null) {
            Animation<TextureRegion> animation = animChar.animations.get(curType);
            if (animation != null) {
                return animation.getKeyFrame(t);
            }
        }
        return null;
    }
}
