import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class App extends JFrame {

    private JTabbedPane tabbedPane = new JTabbedPane();
    public static final ImageIcon closeIcon = new ImageIcon(App.class.getResource("close_icon.gif"));

    private JFileChooser chooser = new JFileChooser();

    /**
     * Creates a new App with it's related GUI
     */
    public App() {

        setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

        chooser.setFileFilter(new FileNameExtensionFilter("txt files", "txt"));
        chooser.setDialogTitle("Select a file...");

        // Setup menu bar
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        JMenuItem openMenuItem = new JMenuItem("Open...");
        openMenuItem.addActionListener(e -> openFile(false));
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        fileMenu.add(openMenuItem);

        JMenuItem openNewTabMenuItem = new JMenuItem("Open in new tab...");
        openNewTabMenuItem.addActionListener(e -> openFile(true));
        openNewTabMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl shift O"));
        fileMenu.add(openNewTabMenuItem);

        fileMenu.add(new JSeparator());

        JMenuItem quitMenuItem = new JMenuItem("Quit");
        quitMenuItem.addActionListener(e -> dispose());
        fileMenu.add(quitMenuItem);

//        // Setup menu for copy and paste
//        JMenu editMenu = new JMenu("Edit");
//        editMenu.setMnemonic(KeyEvent.VK_E);
//        menuBar.add(editMenu);
//
//        // Setup copy menu item
//        JMenuItem copyPopupMenuItem = new JMenuItem("Copy");
//        JMenuItem copyMenuItem = new JMenuItem("Copy");
//
//        Action copyAction = new AbstractAction("Copy") {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                int[] rows = packetTable.getSelectedRows();
//                int[] cols = packetTable.getSelectedColumns();
//
//                StringBuilder stringBuilder = new StringBuilder();
//                String lineSeparator = System.lineSeparator();
//
//                for (int row : rows) {
//                    for (int col : cols) {
//                        stringBuilder.append(packetTable.getValueAt(row, col));
//                        stringBuilder.append("\t");
//                    }
//                    stringBuilder.append(lineSeparator);
//                }
//
//                StringSelection selection = new StringSelection(stringBuilder.toString());
//                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//                clipboard.setContents(selection, null);
//
//            }
//        };
//        copyAction.setEnabled(false);
//
//        KeyStroke copyKeyStroke = KeyStroke.getKeyStroke("ctrl C");
//        copyAction.putValue(Action.ACCELERATOR_KEY, copyKeyStroke);
//        packetTable.getInputMap().put(copyKeyStroke, "copyAction");
//        packetTable.getActionMap().put("copyAction", copyAction);
//
//        copyMenuItem.setAction(copyAction);
//        copyPopupMenuItem.setAction(copyAction);
//        popupMenu.add(copyPopupMenuItem);
//        editMenu.add(copyMenuItem);
//
//        // Setup paste menu item
//        JMenuItem pasteMenuItem = new JMenuItem("Paste");
//        JMenuItem pastePopupMenuItem = new JMenuItem("Paste");
//
//        Action pasteAction = new AbstractAction("Paste") {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                // Paste clipboard contents into every selected cell (that's editable)
//                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//                Transferable data = clipboard.getContents(null);
//                String content;
//                try {
//                    content = data.getTransferData(DataFlavor.stringFlavor).toString();
//                } catch (UnsupportedFlavorException | IOException ex) {
//                    return;
//                }
//
//                int[] rows = packetTable.getSelectedRows();
//                int[] cols = packetTable.getSelectedColumns();
//
//                for (int row : rows) {
//                    for (int col : cols) {
//                        if (packetTable.isCellEditable(row, col)) {
//                            packetTable.setValueAt(content, row, col);
//                        }
//                    }
//                }
//            }
//        };
//        pasteAction.setEnabled(false);
//
//        KeyStroke pasteKeyStroke = KeyStroke.getKeyStroke("ctrl V");
//        pasteAction.putValue(Action.ACCELERATOR_KEY, pasteKeyStroke);
//        packetTable.getInputMap().put(pasteKeyStroke, "pasteAction");
//        packetTable.getActionMap().put("pasteAction", pasteAction);
//
//        pastePopupMenuItem.setAction(pasteAction);
//        pasteMenuItem.setAction(pasteAction);
//        popupMenu.add(pastePopupMenuItem);
//        editMenu.add(pasteMenuItem);
//
//        // Enable/disable edit options based on whether a cell is selected
//        packetTable.getSelectionModel().addListSelectionListener(e -> {
//            boolean isCellSelected = packetTable.getSelectedRow() != -1;
//            copyAction.setEnabled(isCellSelected);
//            pasteAction.setEnabled(isCellSelected);
//        });

        tabbedPane.addTab(null, new PacketPanel(null));
        add(tabbedPane);

        setTitle("Packet Browser");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);
    }

    private void openFile(boolean openInNewTab) {
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            PacketPanel packetPanel;
            String filename;
            File file = chooser.getSelectedFile();

            if (openInNewTab || tabbedPane.getSelectedComponent() == null) {
                packetPanel = new PacketPanel(file);
                filename = packetPanel.getName();
                tabbedPane.addTab(filename, packetPanel);
                tabbedPane.setSelectedComponent(packetPanel);
            } else {
                packetPanel = (PacketPanel) tabbedPane.getSelectedComponent();
                packetPanel.openFile(file);
                filename = packetPanel.getName();
                tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), filename);
            }
            tabbedPane.setTabComponentAt(tabbedPane.getSelectedIndex(), new PacketTab(filename, packetPanel));
            setTitle(filename);
        } else {
            setTitle("Packet Browser");
        }
    }

    class PacketTab extends JPanel {

        Component component;

        PacketTab(String name, Component component) {
            this.component = component;

            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            setOpaque(false);
            add(new JLabel(name));

            JButton closeButton = new JButton(new ImageIcon(App.closeIcon.getImage().getScaledInstance(16, 16, BufferedImage.SCALE_FAST)));
            closeButton.setBorder(null);
            closeButton.setBorderPainted(false);
            closeButton.setContentAreaFilled(false);
            closeButton.setFocusPainted(false);
            closeButton.setOpaque(false);

            closeButton.addActionListener(e -> {
                tabbedPane.remove(component);
            });

            add(closeButton);
        }
    }

}










