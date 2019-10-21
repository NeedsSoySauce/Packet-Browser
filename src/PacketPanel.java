import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class PacketPanel extends JPanel {

    private JComboBox<Host> comboBox = new JComboBox<>();
    private DefaultComboBoxModel<Host> srcComboBoxModel = new DefaultComboBoxModel<>();
    private DefaultComboBoxModel<Host> destComboBoxModel = new DefaultComboBoxModel<>();
    private JRadioButton srcRadioButton = new JRadioButton("Source");
    private JRadioButton destRadioButton = new JRadioButton("Destination");

    private JFileChooser chooser = new JFileChooser();
    private File file;

    private Simulator simulator;
    private List<String> lines;

    private PacketTable packetTable = new PacketTable();
    private PacketTableModel model;
    private TableModelListener tableModelListener = e -> {

        // Ignore changes to the last two lines
        if (e.getFirstRow() >= model.getRowCount() - 2) {
            return;
        }

        // Write changes to each packet to the file
        Packet packet = model.getPacketAt(e.getFirstRow());
        lines.set(packet.getLineIndex() - 1, packet.getTabDelimitedData());
        try {
            Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
        } catch (IOException err) {
            // Failed to write lines
        }
    };

    /**
     * Creates a new App with it's related GUI
     */
    public PacketPanel(File file) {
        this.file = file;
        openFile(file);

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        chooser.setFileFilter(new FileNameExtensionFilter("txt files", "txt"));
        chooser.setDialogTitle("Select a file...");

        // Setup radio buttons to select whether we want to select packets based on their source or their destination
        ButtonGroup radioButtonGroup = new ButtonGroup();
        JPanel topPanel = new JPanel();
        srcRadioButton.setSelected(true);
        srcRadioButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                comboBox.setModel(srcComboBoxModel);
            }
            displaySelectedHostData();
        });
        radioButtonGroup.add(srcRadioButton);
        topPanel.add(srcRadioButton);

        destRadioButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                comboBox.setModel(destComboBoxModel);
            }
            displaySelectedHostData();
        });
        radioButtonGroup.add(destRadioButton);
        topPanel.add(destRadioButton);

        // Setup a combo box to select ips from based on the selected radio button
        comboBox.addActionListener(e -> displaySelectedHostData());
        comboBox.setVisible(false);
        topPanel.add(comboBox);

        // Setup table
        JPanel packetTablePanel = new JPanel();
        packetTablePanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        packetTablePanel.setLayout(new BorderLayout());
        JScrollPane packetTableScrollPane = new JScrollPane(packetTable);
        packetTablePanel.add(packetTableScrollPane);

        add(topPanel);
        add(packetTablePanel);

        // Setup menu and popup menu for copy and paste
        JPopupMenu popupMenu = new JPopupMenu();
        packetTable.setComponentPopupMenu(popupMenu);
        JMenu editMenu = new JMenu("Edit");

        // Setup copy menu item
        JMenuItem copyMenuItem = new JMenuItem("Copy");
        JMenuItem copyPopupMenuItem = new JMenuItem("Copy");

        Action copyAction = new AbstractAction("Copy") {
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

        KeyStroke copyKeyStroke = KeyStroke.getKeyStroke("ctrl C");
        copyAction.putValue(Action.ACCELERATOR_KEY, copyKeyStroke);
        packetTable.getInputMap().put(copyKeyStroke, "copyAction");
        packetTable.getActionMap().put("copyAction", copyAction);

        copyMenuItem.setAction(copyAction);
        copyPopupMenuItem.setAction(copyAction);
        popupMenu.add(copyPopupMenuItem);
        editMenu.add(copyMenuItem);

        // Setup paste menu item
        JMenuItem pasteMenuItem = new JMenuItem("Paste");
        JMenuItem pastePopupMenuItem = new JMenuItem("Paste");

        Action pasteAction = new AbstractAction("Paste") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Paste clipboard contents into every selected cell (that's editable)
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable data = clipboard.getContents(null);
                String content;
                try {
                    content = data.getTransferData(DataFlavor.stringFlavor).toString();
                } catch (UnsupportedFlavorException | IOException ex) {
                    return;
                }

                int[] rows = packetTable.getSelectedRows();
                int[] cols = packetTable.getSelectedColumns();

                for (int row : rows) {
                    for (int col : cols) {
                        if (packetTable.isCellEditable(row, col)) {
                            packetTable.setValueAt(content, row, col);
                        }
                    }
                }
            }
        };
        pasteAction.setEnabled(false);

        KeyStroke pasteKeyStroke = KeyStroke.getKeyStroke("ctrl V");
        pasteAction.putValue(Action.ACCELERATOR_KEY, pasteKeyStroke);
        packetTable.getInputMap().put(pasteKeyStroke, "pasteAction");
        packetTable.getActionMap().put("pasteAction", pasteAction);

        pastePopupMenuItem.setAction(pasteAction);
        pasteMenuItem.setAction(pasteAction);
        popupMenu.add(pastePopupMenuItem);
        editMenu.add(pasteMenuItem);

        // Enable/disable edit options based on whether a cell is selected
        packetTable.getSelectionModel().addListSelectionListener(e -> {
            boolean isCellSelected = packetTable.getSelectedRow() != -1;
            copyAction.setEnabled(isCellSelected);
            pasteAction.setEnabled(isCellSelected);
        });

        setVisible(true);
    }

    /**
     * Opens a file and displays it's contents
     * @param file the file to open
     */
    public void openFile(File file) {
        if (file != null) {
            this.file = file;
            setName(file.getName());
            simulator = new Simulator(file);
            loadLines();
            loadComboBoxOptions();
            displaySelectedHostData();
        }
    }

    private void loadLines() {
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void displaySelectedHostData() {
        Host host = (Host) comboBox.getSelectedItem();
        if (host != null) {
            boolean isSrcHosts = srcRadioButton.isSelected();
            Packet[] packets = simulator.getTableData(host.toString(), isSrcHosts);
            model = new PacketTableModel(packets, isSrcHosts);
            model.addTableModelListener(tableModelListener);
            packetTable.setModel(model);
        }
    }

    private void loadComboBoxOptions() {
        srcComboBoxModel = new DefaultComboBoxModel<>(simulator.getUniqueSortedSourceHosts());
        destComboBoxModel = new DefaultComboBoxModel<>(simulator.getUniqueSortedDestHosts());
        comboBox.setModel(srcRadioButton.isSelected() ? srcComboBoxModel : destComboBoxModel);
        comboBox.setVisible(true);
    }

}
