package ru.mitrakov.self.rush;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.net.Network;

public class RushClient extends ApplicationAdapter {
    private final Model model = new Model();
    private final Thread.UncaughtExceptionHandler errorHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            e.printStackTrace();
        }
    };
    private final Network network;
    //private OrthographicCamera camera;
    private Gui gui;
    private SpriteBatch batch;
    private Texture texture;

    public RushClient() {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        Network n = null;
        try {
            n = new Network(new Parser(model), errorHandler);
        } catch (IOException e) {
            errorHandler.uncaughtException(Thread.currentThread(), e);
            System.exit(0);
        }
        network = n;
        network.start();
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        OrthographicCamera camera = new OrthographicCamera(800, 480);
        camera.setToOrtho(false, 800, 480);
        gui = new Gui(model);
        gui.init();
        texture = new Texture(Gdx.files.internal("up.png"));
    }

    @Override
    public void render() {
        try {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                network.send(Parser.SIGN_IN, "\1Tommy\0Tommy".getBytes());
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
                network.send(Parser.ATTACK, "\0Bobby".getBytes());
            }

            Gdx.gl.glClearColor(.35f, .87f, .91f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            batch.begin();
            gui.render(batch);
            batch.end();
        } catch (IOException e) {
            errorHandler.uncaughtException(Thread.currentThread(), e);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        gui.dispose();
    }
}
