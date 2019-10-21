import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class App extends JFrame {

    public static final String APP_NAME = "Packet Browser";
    private ImageIcon closeTabIcon;
    private Action copyAction, pasteAction;
    private KeyStroke copyKeyStroke = KeyStroke.getKeyStroke("ctrl C");
    private KeyStroke pasteKeyStroke = KeyStroke.getKeyStroke("ctrl V");
    private JPopupMenu popupMenu = new JPopupMenu();
    private JTabbedPane tabbedPane = new JTabbedPane();
    private PacketTable packetTable;
    private ListSelectionListener copyPasteListener = e -> updateCopyPasteActions();
    private JFileChooser chooser = new JFileChooser();

    /**
     * Creates a new App with it's related GUI elements
     */
    public App() {

        // Load close tab icon
        try {
            BufferedImage image = ImageIO.read(App.class.getResource("close_icon.gif"));
            closeTabIcon = new ImageIcon(image.getScaledInstance(16, 16, BufferedImage.SCALE_SMOOTH));
        } catch (IOException | NullPointerException e) {
            // Failed to load image
        }

        setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

        // Setup file chooser
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

        // Setup menu and popup menu for copy and paste
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(editMenu);

        // Setup copy menu items
        JMenuItem copyPopupMenuItem = new JMenuItem("Copy");
        JMenuItem copyMenuItem = new JMenuItem("Copy");

        copyAction = new AbstractAction("Copy") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] rows = packetTable.getSelectedRows();
                int[] cols = packetTable.getSelectedColumns();

                StringBuilder stringBuilder = new StringBuilder();
                String lineSeparator = System.lineSeparator();

                for (int row : rows) {
                    for (int col : cols) {
                        stringBuilder.append(packetTable.getValueAt(row, col));
                        stringBuilder.append("\t");
                    }
                    stringBuilder.append(lineSeparator);
                }

                StringSelection selection = new StringSelection(stringBuilder.toString());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, null);
            }
        };
        copyAction.setEnabled(false);
        copyAction.putValue(Action.ACCELERATOR_KEY, copyKeyStroke);

        copyPopupMenuItem.setAction(copyAction);
        popupMenu.add(copyPopupMenuItem);
        copyMenuItem.setAction(copyAction);
        editMenu.add(copyMenuItem);

        // Setup paste menu items
        JMenuItem pasteMenuItem = new JMenuItem("Paste");
        JMenuItem pastePopupMenuItem = new JMenuItem("Paste");

        pasteAction = new AbstractAction("Paste") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Paste clipboard contents into every selected cell (that's editable)
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable data = clipboard.getContents(null);
                String clipboardContent;
                try {
                    clipboardContent = data.getTransferData(DataFlavor.stringFlavor).toString();
                } catch (UnsupportedFlavorException | IOException ex) {
                    return;
                }

                int[] rows = packetTable.getSelectedRows();
                int[] cols = packetTable.getSelectedColumns();

                for (int row : rows) {
                    for (int col : cols) {
                        if (packetTable.isCellEditable(row, col) && packetTable.getValueAt(row, col) != clipboardContent) {
                            packetTable.setValueAt(clipboardContent, row, col);
                        }
                    }
                }
            }
        };
        pasteAction.setEnabled(false);
        pasteAction.putValue(Action.ACCELERATOR_KEY, pasteKeyStroke);

        pastePopupMenuItem.setAction(pasteAction);
        popupMenu.add(pastePopupMenuItem);
        pasteMenuItem.setAction(pasteAction);
        editMenu.add(pasteMenuItem);

        // Whenever the selected tab changes, update the listeners on the current and previous tab
        tabbedPane.addChangeListener(e -> {
            PacketPanel packetPanel = ((PacketPanel) tabbedPane.getSelectedComponent());
            if (packetPanel == null) {
                setTitle(APP_NAME);
                return;
            }
            packetTable = packetPanel.getPacketTable();
            setTitle(packetPanel.getName());
        });
        add(tabbedPane);

        setSize(800, 600);
        setTitle("Packet Browser");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private boolean isEditableCellSelected() {
        int[] rows = packetTable.getSelectedRows();
        int[] cols = packetTable.getSelectedColumns();

        for (int row : rows) {
            for (int col : cols) {
                if (packetTable.isCellEditable(row, col)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateCopyPasteActions() {
        boolean isCellSelected = packetTable.getSelectedRow() != -1;
        copyAction.setEnabled(isCellSelected);
        pasteAction.setEnabled(isEditableCellSelected());
    }

    private void openFile(boolean openInNewTab) {
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            File file = chooser.getSelectedFile();
            PacketPanel packetPanel;
            String filename;

            if (openInNewTab || tabbedPane.getSelectedComponent() == null) {
                packetPanel = new PacketPanel(file);
                tabbedPane.addTab(null, packetPanel);
                tabbedPane.setSelectedComponent(packetPanel);
            } else {
                packetPanel = (PacketPanel) tabbedPane.getSelectedComponent();
                packetPanel.openFile(file);
            }

            // Setup listeners to enable or disable popup menu items based on what's selected
            packetTable = packetPanel.getPacketTable();
            packetTable.getSelectionModel().addListSelectionListener(copyPasteListener);
            updateCopyPasteActions();

            // Set shortcuts for copy and paste
            packetTable.getInputMap().put(copyKeyStroke, "copyAction");
            packetTable.getActionMap().put("copyAction", copyAction);
            packetTable.getInputMap().put(pasteKeyStroke, "pasteAction");
            packetTable.getActionMap().put("pasteAction", pasteAction);

            packetPanel.getPacketTable().setComponentPopupMenu(popupMenu);
            filename = packetPanel.getName();
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

            JButton closeButton = new JButton(closeTabIcon);
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










