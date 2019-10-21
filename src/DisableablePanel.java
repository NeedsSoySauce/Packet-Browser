import javax.swing.*;
import java.awt.*;

public class DisableablePanel extends JPanel {

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        // Recursively disable all components in this panel
        for (Component component : getComponents()) {
            if (component instanceof DisableablePanel) {
                component.setEnabled(enabled);
            }
            component.setEnabled(enabled);
        }
    }
}
