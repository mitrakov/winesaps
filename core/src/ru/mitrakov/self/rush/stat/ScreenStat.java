package ru.mitrakov.self.rush.stat;

import java.util.Locale;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;

import ru.mitrakov.self.rush.PsObject;

import static ru.mitrakov.self.rush.Winesaps.*;

/**
 * Statistics Screen
 * @author Mitrakov
 * @see Stat
 */
class ScreenStat extends ScreenAdapter {
    /** List of Categories (defined by the Server) */
    private static final String[] categories = new String[]{
            "Time elapsed (µs): ",
            "Uptime (min):      ",
            "RPS:               ",
            "Current used SIDs: ",
            "Current battles:   ",
            "Current users:     ",
            "Total battles:     ",
            "Total users:       ",
            "Senders count:     ",
            "Receivers count:   ",
            "Fake SIDs count:   ",
            "Total AI spawned:  ",
            "Battle refs up:    ",
            "Battle refs down:  ",
            "Round refs up:     ",
            "Round refs down:   ",
            "Field refs up:     ",
            "Field refs down:   ",
            "Current env size:  ",
            "Waiting count:     ",
    };

    /** Platform specific object */
    private final PsObject psObject;
    /** Reference to Stat */
    private final Stat stat;
    /** LibGdx skin */
    private final Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
    /** LibGdx Scene2D Stage */
    private final Stage stage = new Stage(new FitViewport(WIDTH, HEIGHT));
    /** Left table */
    private final Table tableLeft = new Table();
    /** Middle table */
    private final Table tableMid = new Table();
    /** Right table */
    private final Table tableRight = new Table();
    /** "SRTT = X.XXXX" label */
    private final Label lblSrtt = new Label("", skin, "white");
    /** "Connected/disconnected" label */
    private final Label lblConnected = new Label("", skin, "white");
    /** Map [CategoryIndex -> LabelThatShowsValue], e.g. "1 -> Label('25')" means that Uptime is 25 min */
    private final IntMap<Label> lblValues = new IntMap<Label>(16);

    /**
     * Creates a new Statistics Screen
     * @param stat {@link Stat}
     * @param psObject Platform Specific Object (NON-NULL)
     */
    ScreenStat(Stat stat, PsObject psObject) {
        assert stat != null && psObject != null;
        this.stat = stat;
        this.psObject = psObject;

        Table table = new Table();
        table.setFillParent(true);
        table.add(tableLeft).space(40);
        table.add(tableMid).space(40);
        table.add(tableRight).space(40);
        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK))
            psObject.hide();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    /**
     * Initializes the screen
     * @return this
     */
    ScreenStat init() {
        for (int i = 0; i < categories.length; i++) {
            Label category = new Label(categories[i], skin, "white");
            Label value = new Label("0", skin, "white");
            lblValues.put(i, value);
            Table table = i < categories.length / 2 ? tableLeft : tableMid;
            table.add(category).left();
            table.add(value).right();
            table.row();
        }
        tableRight.row();
        tableRight.add(lblSrtt);
        tableRight.row();
        tableRight.add(lblConnected);
        tableRight.row();
        tableRight.add(new TextButton("Call Function", skin, "default") {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Gdx.input.getTextInput(new Input.TextInputListener() {
                        @Override
                        public void input(String text) {
                            stat.sendCmd(text);
                        }

                        @Override
                        public void canceled() {
                        }
                    }, "", "", "");
                }
            });
        }});
        return this;
    }

    /**
     * Sets the value for a given category
     * @param category category index (zero-based)
     * @param value value (0-65535)
     */
    void setValue(int category, int value) {
        Label label = lblValues.get(category);
        if (label != null)
            label.setText(String.valueOf(value));
        if (category == 19 && value > 0) // 19 is "Waiting count", sorry for hardcode
            psObject.pushNotification("Someone is waiting for enemy!", true);
    }

    /**
     * Shows message box
     * @param message text
     */
    void showMessage(String message) {
        new Dialog("", skin, "default").text(message).button("OK").show(stage);
    }

    /**
     * Sets current value of SRTT
     * @param srtt Smoothed Round Trip Time
     */
    void setSrtt(float srtt) {
        lblSrtt.setText(String.format(Locale.getDefault(), "SRTT = %.2f", srtt));
    }

    /**
     * Sets current value of connection flag
     * @param value TRUE to display "Connected", and FALSE to display "Disconnected"
     */
    void setConnected(boolean value) {
        lblConnected.setText(value ? "Connected" : "Disconnected");
    }
}
