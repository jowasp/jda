package the.bytecode.club.jda.gui;

import org.objectweb.asm.tree.ClassNode;
import the.bytecode.club.jda.settings.Settings;

import javax.swing.*;

public abstract class Viewer extends JPanel {

    public ClassNode cn;
    public String name;
    public String container;

    private static final long serialVersionUID = -2965538493489119191L;

    public void updateName() {
        this.setName(name + (Settings.SHOW_CONTAINER_NAME.getBool() ? "(" + container + ")" : ""));
    }
}
