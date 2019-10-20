import javax.swing.*;

public class RunApp implements Runnable {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            // Use default look and feel
        }
        SwingUtilities.invokeLater(new RunApp());
    }

    @Override
    public void run() {
        new App();
    }


}
