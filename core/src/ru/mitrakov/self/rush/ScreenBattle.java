package ru.mitrakov.self.rush;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;

import ru.mitrakov.self.rush.model.Model;

/**
 * Created by mitrakov on 01.03.2017
 */

public class ScreenBattle extends ScreenAdapter {
    private Gui gui;
    private Controller controller;
    private SpriteBatch batch = new SpriteBatch();

    public ScreenBattle(Model model) {
        OrthographicCamera camera = new OrthographicCamera(800, 480);
        camera.setToOrtho(false, 800, 480);
        gui = new Gui(model);
        gui.init();
        controller = new Controller(model, camera);
    }

    @Override
    public void render(float delta) {
        // gui, controller, batch must NOT be NULL (assert omitted)
        controller.checkInput(gui);
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
