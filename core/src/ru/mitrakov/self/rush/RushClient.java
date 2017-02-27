package ru.mitrakov.self.rush;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.net.Network;

public class RushClient extends ApplicationAdapter {
    private final Model model = new Model();
    private Gui gui;           // ....
    private SpriteBatch batch; // ....
    private Controller controller;

    public RushClient() {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        Thread.UncaughtExceptionHandler errorHandler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        };
        try {
            Network network = new Network(new Parser(model), errorHandler);
            network.start();
            model.setSender(new Sender(network, errorHandler));
        } catch (Exception e) {
            errorHandler.uncaughtException(Thread.currentThread(), e);
            System.exit(0);
        }
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        OrthographicCamera camera = new OrthographicCamera(800, 480);
        camera.setToOrtho(false, 800, 480);
        gui = new Gui(model);
        gui.init();
        controller = new Controller(model, camera);
    }

    @Override
    public void render() {
        // controller and batch must be created here
        controller.check();
        Gdx.gl.glClearColor(.35f, .87f, .91f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        gui.render(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        gui.dispose();
    }
}
