package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;

/**
 * Scroll Pane with "slidable" pages
 * @see <a href="http://nexsoftware.net/wp/2013/05/09/libgdx-making-a-paged-level-selection-screen/">http://nexsoftware.net</a>
 */
public class PagedScrollPane extends ScrollPane {

    private boolean wasPanDragFling = false;

    private float pageSpacing;

    private Table content;

    public PagedScrollPane () {
        super(null);
        setup();
    }

    public PagedScrollPane (Skin skin) {
        super(null, skin);
        setup();
    }

    public PagedScrollPane (Skin skin, String styleName) {
        super(null, skin, styleName);
        setup();
    }

    public PagedScrollPane (Actor widget, ScrollPaneStyle style) {
        super(null, style);
        setup();
    }

    private void setup() {
        content = new Table();
        content.defaults().space(50);
        setOverscroll(false, false); // by mitrakov
        super.setWidget(content);
    }

    public void addPages (Actor... pages) {
        for (Actor page : pages) {
            content.add(page).expandY().fillY();
        }
    }

    public void addPage (Actor page) {
        content.add(page).expandY().fillY();
    }

    @Override
    public void act (float delta) {
        super.act(delta);
        if (wasPanDragFling && !isPanning() && !isDragging() && !isFlinging()) {
            wasPanDragFling = false;
            scrollToPage();
        } else {
            if (isPanning() || isDragging() || isFlinging()) {
                wasPanDragFling = true;
            }
        }
    }

    @Override
    public void setWidget (Actor widget) {
        if (widget != null) // by mitrakov
            throw new UnsupportedOperationException("Use PagedScrollPane#addPage.");
    }

    @Override
    public void setWidth (float width) {
        super.setWidth(width);
        if (content != null) {
            for (Cell cell : content.getCells()) {
                cell.width(width);
            }
            content.invalidate();
        }
    }

    @Override
    public void clear() {
        content.clear(); // by mitrakov
    }

    public void setPageSpacing (float pageSpacing) {
        if (content != null) {
            content.defaults().space(pageSpacing);
            for (Cell cell : content.getCells()) {
                cell.space(pageSpacing);
            }
            content.invalidate();
        }
    }

    private void scrollToPage () {
        final float width = getWidth();
        final float scrollX = getScrollX();
        final float maxX = getMaxX();

        if (scrollX >= maxX || scrollX <= 0) return;

        Array<Actor> pages = content.getChildren();
        float pageX = 0;
        float pageWidth = 0;
        if (pages.size > 0) {
            for (Actor a : pages) {
                pageX = a.getX();
                pageWidth = a.getWidth();
                if (scrollX < (pageX + pageWidth * 0.5)) {
                    break;
                }
            }
            setScrollX(MathUtils.clamp(pageX - (width - pageWidth) / 2, 0, maxX));
        }
    }
}
