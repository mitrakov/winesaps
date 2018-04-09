package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Class to store different animation frames for different character actions (Run, Climb, Swim and so on)
 * @author mitrakov
 */
class AnimationData<T> {
    /** Animation type (idle, run, etc.) */
    enum AnimationType {Idle, Run, Climb, Ladder}

    /**
     * Simple class to store all possible animations for a character
     */
    private static class AnimChar {
        /** Map: [AnimationType -> LibGDX Animation] */
        private final ObjectMap<AnimationType, Animation<TextureRegion>> animations =
                new ObjectMap<AnimationType, Animation<TextureRegion>>(AnimationType.values().length);

        /**
         * Creates new instance of AnimChar
         * @param atlas texture atlas to grab animation frames from
         * @param frameDuration frame duration (may be overwritten for some animation types)
         */
        AnimChar(TextureAtlas atlas, float frameDuration) {
            for (AnimationType type : AnimationType.values()) {
                Animation.PlayMode mode = type == AnimationType.Ladder ? Animation.PlayMode.NORMAL
                        : Animation.PlayMode.LOOP;
                if (type == AnimationType.Climb) frameDuration = .03f;
                if (type == AnimationType.Ladder) frameDuration = .025f; // 0.4s / 16 frames
                Array<TextureAtlas.AtlasRegion> frames = atlas.findRegions(type.name());
                Animation<TextureRegion> animation = new Animation<TextureRegion>(frameDuration, frames, mode);
                animations.put(type, animation);
            }
        }
    }

    /** Speed by X axis, WU per sec */
    final float speedX;
    /** Coordinate, in world units (in most cases 1 WU = 1 pixel) */
    float x, y;
    /** Flag if an actor is turned to the right */
    boolean dirRight = true;
    /** Reset flag, is used for non-animated moving (teleportation and so on) */
    boolean reset = false;

    /** Map: [T -> AnimChar] (T is usually instance of Character, but may be any type) */
    private final ObjectMap<T, AnimChar> animChars = new ObjectMap<T, AnimChar>();
    /** Current animation type */
    private AnimationType curType = AnimationType.Idle;
    /** Current time, in seconds */
    private float t;

    /**
     * Creates a new instance of AnimationData
     * @param speedX current speed on X axis (worldUnits/sec)
     */
    AnimationData(float speedX) {
        this.speedX = speedX;
    }

    /**
     * Adds a new AnimChar to animation store for the given key
     * @param key any key (usually instance of Character)
     * @param atlas Texture Atlas that contains frames needed
     * @param frameDuration animation speed
     * @return "this"
     */
    public AnimationData add(T key, TextureAtlas atlas, float frameDuration) {
        animChars.put(key, new AnimChar(atlas, frameDuration));
        return this;
    }

    /**
     * Gets the current frame (taking into account current time "t") for the animation matched by the given key
     * @param key key (usually instance of Character)
     * @return current animation frame (of NULL if a key matches nothing)
     */
    TextureRegion getFrame(T key) {
        AnimChar animChar = animChars.get(key);
        if (animChar != null) {
            Animation<TextureRegion> animation = animChar.animations.get(curType);
            if (animation != null)
                return animation.getKeyFrame(t);
        }
        return null;
    }

    /**
     * Gets the max possible width of the animation matched by the given key
     * @param key key (usually instance of Character)
     * @return texture width (or 0 if a key matches nothing)
     */
    int getWidth(T key) {
        AnimChar animChar = animChars.get(key);
        if (animChar != null) {
            Animation<TextureRegion> animation = animChar.animations.get(AnimationType.Run); // "Run" is usually wider
            if (animation != null)
                return animation.getKeyFrame(t).getRegionWidth();
        }
        return 0;
    }

    /**
     * @return current animation type
     */
    AnimationType getAnimation() {
        return curType;
    }

    /**
     * Increases current time to move the animation
     * @param dt delta time (sec)
     */
    void addDt(float dt) {
        t += dt;
    }

    /**
     * Sets the animation type on/off
     * @param type animation type
     * @param value true to set and false to unset animation
     */
    void setAnimation(AnimationType type, boolean value) {
        switch (type) {
            case Run:
            case Climb:
            case Ladder:
                if (value && curType != type) {
                    curType = type;
                    t = 0;
                }
                if (!value && curType == type) {
                    curType = AnimationType.Idle;
                    t = 0;
                }
                break;
            default:
        }
    }
}
