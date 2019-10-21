import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
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
     * Creates a new PacketPanel with it's related GUI elements
     */
    public PacketPanel(File file) {
        this.file = file;

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

        openFile(file);

        setVisible(true);
    }

    /**
     * @return this panel's PacketTable
     */
    public PacketTable getPacketTable() {
        return packetTable;
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
