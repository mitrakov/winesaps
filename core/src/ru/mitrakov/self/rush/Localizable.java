package ru.mitrakov.self.rush;

import com.badlogic.gdx.utils.I18NBundle;

/**
 * Interface for GUI elements that may be localized to different languages
 * @author mitrakov
 */
public interface Localizable {
    void onLocaleChanged(I18NBundle bundle);
}
