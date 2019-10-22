import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
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

    private JComboBox<String> browseComboBox = new JComboBox<>();
    private DefaultComboBoxModel<String> srcComboBoxModel = new DefaultComboBoxModel<>();
    private DefaultComboBoxModel<String> destComboBoxModel = new DefaultComboBoxModel<>();
    private JRadioButton srcRadioButton = new JRadioButton("Source");
    private JRadioButton destRadioButton = new JRadioButton("Destination");

    private JComboBox<String> flowSrcComboBox = new JComboBox<>();
    private JComboBox<String> flowDestComboBox = new JComboBox<>();
    private JRadioButton browseRadioButton = new JRadioButton("Browse");
    private JRadioButton flowRadioButton = new JRadioButton("Flow");

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
        Integer lineIndex = packet.getLineIndex();
        if (lineIndex == null) {
            return;
        }
        lines.set(lineIndex - 1, packet.getTabDelimitedData());
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

        JPanel topPanel = new JPanel();
        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

        FlowLayout flowLayout = new FlowLayout();
        flowLayout.setHgap(4);
        flowLayout.setVgap(0);

        JPanel modePanel = new JPanel();
        modePanel.setLayout(flowLayout);
        modePanel.setBorder(BorderFactory.createTitledBorder(etchedBorder, "Mode"));
        modePanel.add(browseRadioButton);
        modePanel.add(flowRadioButton);
        topPanel.add(modePanel);

        DisableablePanel browsePanel = new DisableablePanel();
        browsePanel.setLayout(flowLayout);
        browsePanel.setBorder(BorderFactory.createTitledBorder(etchedBorder, "View packets from..."));
        browsePanel.add(srcRadioButton);
        browsePanel.add(destRadioButton);
        browsePanel.add(browseComboBox);
        topPanel.add(browsePanel);

        DisableablePanel flowPanel = new DisableablePanel();
        flowPanel.setLayout(flowLayout);
        flowPanel.setBorder(BorderFactory.createTitledBorder(etchedBorder, "View packet flow from..."));
        flowPanel.add(flowSrcComboBox);
        flowPanel.add(new JLabel("to"));
        flowPanel.add(flowDestComboBox);
        topPanel.add(flowPanel);
        flowPanel.setEnabled(false);

        // Setup radio buttons to select the mode to view packets in
        ButtonGroup modeButtonGroup = new ButtonGroup();
        modeButtonGroup.add(browseRadioButton);
        modeButtonGroup.add(flowRadioButton);
        browseRadioButton.setSelected(true);

        flowRadioButton.addItemListener(l -> {
            boolean isSelected = l.getStateChange() == ItemEvent.SELECTED;
            if (l.getStateChange() == ItemEvent.SELECTED) {
                // Display flow content
                displaySelectedPacketFlowData();
            }
            flowPanel.setEnabled(isSelected);
        });

        browseRadioButton.addItemListener(l -> {
            boolean isSelected = l.getStateChange() == ItemEvent.SELECTED;
            if (l.getStateChange() == ItemEvent.SELECTED) {
                // Display browse content
                displaySelectedHostData();
            }
            browsePanel.setEnabled(isSelected);
        });

        // Setup radio buttons to select whether we want to select packets based on their source or their destination
        ButtonGroup radioButtonGroup = new ButtonGroup();
        radioButtonGroup.add(srcRadioButton);
        radioButtonGroup.add(destRadioButton);
        srcRadioButton.setSelected(true);

        srcRadioButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                browseComboBox.setModel(srcComboBoxModel);
            }
            displaySelectedHostData();
        });

        destRadioButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                browseComboBox.setModel(destComboBoxModel);
            }
            displaySelectedHostData();
        });

        // Setup a combo box to select IPs from based on the selected radio button
        browseComboBox.addActionListener(e -> displaySelectedHostData());
        flowSrcComboBox.addActionListener(e -> displaySelectedPacketFlowData());
        flowDestComboBox.addActionListener(e -> displaySelectedPacketFlowData());


        // Setup packet table
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
     *
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
            System.out.println("2");
        }
    }

    private void displaySelectedHostData() {
        String hostIP = (String) browseComboBox.getSelectedItem();
        if (hostIP != null) {
            boolean isSrcHosts = srcRadioButton.isSelected();
            Packet[] packets = simulator.getTableData(hostIP, isSrcHosts);
            model = new PacketTableModel(packets, isSrcHosts);
            model.addTableModelListener(tableModelListener);
            packetTable.setModel(model);
        }
    }

    private void displaySelectedPacketFlowData() {
        String srcIP = (String) flowSrcComboBox.getSelectedItem();
        String destIP = (String) flowDestComboBox.getSelectedItem();
        if (srcIP != null && destIP != null) {
            Packet[] packets = simulator.getPacketFlowTableData(srcIP, destIP);
            model = new PacketTableModel(packets, true);
            model.addTableModelListener(tableModelListener);
            packetTable.setModel(model);
        }
    }

    private void loadComboBoxOptions() {
        String[] srcIPs = simulator.getUniqueSortedSourceHostIPs();
        String[] destIPs = simulator.getUniqueSortedDestHostIPs();

        // Create independent models for each view mode so that their associated combo boxes can remember their state
        srcComboBoxModel = new DefaultComboBoxModel<>(srcIPs);
        destComboBoxModel = new DefaultComboBoxModel<>(destIPs);
        browseComboBox.setModel(srcRadioButton.isSelected() ? srcComboBoxModel : destComboBoxModel);

        flowSrcComboBox.setModel(new DefaultComboBoxModel<>(srcIPs));
        flowDestComboBox.setModel(new DefaultComboBoxModel<>(destIPs));
    }

}
