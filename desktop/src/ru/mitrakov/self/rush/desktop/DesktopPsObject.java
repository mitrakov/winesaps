package ru.mitrakov.self.rush.desktop;

import javax.swing.*;

import static javax.swing.JOptionPane.*;

import ru.mitrakov.self.rush.*;

/**
 * Created by mitrakov on 26.07.2017
 */
class DesktopPsObject extends PsObject {
    private final JFrame parent;
    private final Icon icon;

    DesktopPsObject(IBillingProvider billingProvider, JFrame parent, Icon icon) {
        super(billingProvider);
        this.parent = parent;
        this.icon = icon;
    }

    @Override
    public void hide() {
    }

    @Override
    public void pushNotification(String msg) {
        if (!parent.isVisible()) { // if the app is active => no need to push notifications
            int res = showOptionDialog(null, msg, "Winesaps", OK_CANCEL_OPTION, QUESTION_MESSAGE, icon, null, null);
            if (res == OK_OPTION)
                parent.setVisible(true);
        }
    }
}
