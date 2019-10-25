import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

public class App extends JFrame implements PacketTableConstants {

    public static final String APP_NAME = "Packet Browser";
    private static final FlowLayout PACKET_TAB_LAYOUT = new FlowLayout(FlowLayout.LEFT, 0, 0);
    private ImageIcon closeTabIcon;
    private Action copyAction, pasteAction;
    private KeyStroke copyKeyStroke = KeyStroke.getKeyStroke("ctrl C");
    private KeyStroke pasteKeyStroke = KeyStroke.getKeyStroke("ctrl V");
    private JPopupMenu popupMenu = new JPopupMenu();
    private JTabbedPane tabbedPane = new JTabbedPane();
    private PacketTable packetTable;
    private ListSelectionListener copyPasteListener = e -> {
        if (!e.getValueIsAdjusting()) {
            updateCopyPasteActions();
        }
    };
    private JFileChooser chooser = new JFileChooser();

    /**
     * Creates a new App with it's related GUI elements
     */
    public App() {

        // Load close tab icon
        try {
            BufferedImage image = ImageIO.read(App.class.getResource("close_icon.gif"));
            closeTabIcon = new ImageIcon(image.getScaledInstance(12, 12, BufferedImage.SCALE_SMOOTH));
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

        // Setup file menu
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

        JMenuItem printTableMenuItem = new JMenuItem("Print");
        printTableMenuItem.addActionListener(e -> printTable());
        printTableMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl P"));
        printTableMenuItem.setEnabled(false);
        fileMenu.add(printTableMenuItem);

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

                int lastRow = -1;
                for (int row : rows) {
                    lastRow = Math.max(row, lastRow);
                }

                int lastCol = -1;
                for ( int col : cols) {
                    lastCol = Math.max(col, lastCol);
                }

                StringBuilder stringBuilder = new StringBuilder();
                Object cellValue;
                String lineSeparator = System.lineSeparator();

                for (int row : rows) {
                    for (int col : cols) {
                        cellValue = packetTable.getValueAt(row, col);
                        stringBuilder.append(cellValue != null ? cellValue : "");
                        if (col != lastCol) {
                            stringBuilder.append("\t");
                        }
                    }
                    if (row != lastRow) {
                        stringBuilder.append(lineSeparator);
                    }
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

        // Setup a menu to change the columns that are displayed in the packetTable
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        menuBar.add(viewMenu);
        JCheckBoxMenuItem timestampMenuItem = new JCheckBoxMenuItem(TIMESTAMP_COL_NAME, true);
        JCheckBoxMenuItem srcIPMenuItem = new JCheckBoxMenuItem(SRC_COL_NAME, true);
        JCheckBoxMenuItem srcPortMenuItem = new JCheckBoxMenuItem(SRC_PORT_COL_NAME, true);
        JCheckBoxMenuItem destIPMenuItem = new JCheckBoxMenuItem(DEST_COL_NAME, true);
        JCheckBoxMenuItem destPortMenuItem = new JCheckBoxMenuItem(DEST_PORT_COL_NAME, true);
        JCheckBoxMenuItem ipPacketSizeMenuItem = new JCheckBoxMenuItem(SIZE_COL_NAME, true);

        ItemListener viewMenuItemListener = e -> {
            String columnName;
            if (e.getSource().equals(timestampMenuItem)) {
                columnName = TIMESTAMP_COL_NAME;
            } else if (e.getSource().equals(srcIPMenuItem)) {
                columnName = SRC_COL_NAME;
            } else if (e.getSource().equals(srcPortMenuItem)) {
                columnName = SRC_PORT_COL_NAME;
            } else if (e.getSource().equals(destIPMenuItem)) {
                columnName = DEST_COL_NAME;
            } else if (e.getSource().equals(destPortMenuItem)) {
                columnName = DEST_PORT_COL_NAME;
            } else if (e.getSource().equals(ipPacketSizeMenuItem)) {
                columnName = SIZE_COL_NAME;
            } else {
                return;
            }
            PacketTable.setColumnVisibility(columnName, e.getStateChange() == ItemEvent.SELECTED);

            if (packetTable != null) {
                packetTable.updateColumnVisibility();
            }
        };

        timestampMenuItem.addItemListener(viewMenuItemListener);
        viewMenu.add(timestampMenuItem);
        srcIPMenuItem.addItemListener(viewMenuItemListener);
        viewMenu.add(srcIPMenuItem);
        srcPortMenuItem.addItemListener(viewMenuItemListener);
        viewMenu.add(srcPortMenuItem);
        destIPMenuItem.addItemListener(viewMenuItemListener);
        viewMenu.add(destIPMenuItem);
        destPortMenuItem.addItemListener(viewMenuItemListener);
        viewMenu.add(destPortMenuItem);
        ipPacketSizeMenuItem.addItemListener(viewMenuItemListener);
        viewMenu.add(ipPacketSizeMenuItem);

        // Update appropriate values whenever the selected tab changes
        tabbedPane.addChangeListener(e -> {
            PacketPanel packetPanel = ((PacketPanel) tabbedPane.getSelectedComponent());
            if (packetPanel == null) {
                printTableMenuItem.setEnabled(false);
                setTitle(APP_NAME);
                return;
            }
            printTableMenuItem.setEnabled(true);
            packetTable = packetPanel.getPacketTable();
            packetTable.updateColumnVisibility();
            updateCopyPasteActions();
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

        for (int col : cols) { // Faster to check along columns than rows in this application
            for (int row : rows) {
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
                packetTable = packetPanel.getPacketTable();

                // Setup shortcuts for copy and paste actions
                packetTable.getInputMap().put(copyKeyStroke, "copyAction");
                packetTable.getActionMap().put("copyAction", copyAction);
                packetTable.getInputMap().put(pasteKeyStroke, "pasteAction");
                packetTable.getActionMap().put("pasteAction", pasteAction);
                packetTable.setComponentPopupMenu(popupMenu);

                tabbedPane.addTab(null, packetPanel);
                tabbedPane.setSelectedComponent(packetPanel);
            } else {
                packetPanel = (PacketPanel) tabbedPane.getSelectedComponent();
                packetTable = packetPanel.getPacketTable();
                packetPanel.openFile(file);
            }

            // Setup listeners to enable or disable copy paste menu items based on what's selected
            PropertyChangeListener fileLoaded = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent e) {
                    // It's possible that multiple PacketPanels could be loading at once, so we need to get the
                    // packetTable attached to the packetPanel that finished loading
                    PacketTable table = ((PacketPanel) e.getSource()).getPacketTable();

                    table.getSelectionModel().addListSelectionListener(copyPasteListener);
                    table.getColumnModel().getSelectionModel().addListSelectionListener(copyPasteListener);
                    packetPanel.removePropertyChangeListener(this);
                }
            };
            packetPanel.addPropertyChangeListener("packetTable", fileLoaded);

            packetTable.updateColumnVisibility();

            filename = packetPanel.getName();
            tabbedPane.setTabComponentAt(tabbedPane.getSelectedIndex(), new PacketTab(filename, packetPanel));
            setTitle(filename);
        } else {
            setTitle("Packet Browser");
        }
    }

    private void printTable() {
        if (packetTable != null) {
            try {
                packetTable.print();
            } catch (PrinterException e) {
                e.printStackTrace();
            }
        }
    }

    class PacketTab extends JPanel {

        Component component;

        PacketTab(String name, Component component) {
            this.component = component;

            setLayout(PACKET_TAB_LAYOUT);
            setOpaque(false);
            add(new JLabel(name));

            JButton closeButton = new JButton(closeTabIcon);
            closeButton.setBorder(null);
            closeButton.setBorderPainted(false);
            closeButton.setContentAreaFilled(false);
            closeButton.setFocusPainted(false);
            closeButton.setOpaque(false);
            closeButton.addActionListener(e -> tabbedPane.remove(component));
            add(closeButton);
        }
    }

}










