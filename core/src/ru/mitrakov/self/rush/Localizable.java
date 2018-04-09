package ru.mitrakov.self.rush;

import com.badlogic.gdx.utils.I18NBundle;

/**
 * Interface for GUI elements that may be localized to different languages
 * @author mitrakov
 */
public interface Localizable {
    /**
     * Invoked when a user changes the GUI language
     * @param bundle LibGDX internationalization bundle
     */
    void onLocaleChanged(I18NBundle bundle);
}
