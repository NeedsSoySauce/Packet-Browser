import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
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

    private static FlowLayout BORDERED_PANEL_LAYOUT = new FlowLayout(FlowLayout.CENTER, 0, 0);
    private static Border BORDERED_PANEL_BORDER = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

    // Components for the packet browsing mode
    private JComboBox<Object> browseComboBox = new JComboBox<>();
    private JRadioButton srcRadioButton = new JRadioButton("Source");
    private JRadioButton destRadioButton = new JRadioButton("Destination");

    // Components for the packet flow mode
    private JComboBox<Object> flowSrcComboBox = new JComboBox<>();
    private JComboBox<Object> flowDestComboBox = new JComboBox<>();
    private JRadioButton browseRadioButton = new JRadioButton("Browse");
    private JRadioButton flowRadioButton = new JRadioButton("Flow");

    // Components for packet filtering selection
    private JRadioButton ipFilterRadioButton = new JRadioButton("IP");
    private JRadioButton portFilterRadioButton = new JRadioButton("Port");

    // These models are duplicated for each filter mode so that their selection states are saved if the user
    // switches between filter modes
    private DefaultComboBoxModel<Object> browseSrcIPComboBoxModel = new DefaultComboBoxModel<>();
    private DefaultComboBoxModel<Object> browseDestIPComboBoxModel = new DefaultComboBoxModel<>();
    private DefaultComboBoxModel<Object> flowSrcIPComboBoxModel = new DefaultComboBoxModel<>();
    private DefaultComboBoxModel<Object> flowDestIPComboBoxModel = new DefaultComboBoxModel<>();
    private DefaultComboBoxModel<Object> browseSrcPortComboBoxModel = new DefaultComboBoxModel<>();
    private DefaultComboBoxModel<Object> browseDestPortComboBoxModel = new DefaultComboBoxModel<>();
    private DefaultComboBoxModel<Object> flowSrcPortComboBoxModel = new DefaultComboBoxModel<>();
    private DefaultComboBoxModel<Object> flowDestPortComboBoxModel = new DefaultComboBoxModel<>();

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

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        chooser.setFileFilter(new FileNameExtensionFilter("txt files", "txt"));
        chooser.setDialogTitle("Select a file...");

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        BorderedPanel selectionModePanel = new BorderedPanel("Filter by...");
        selectionModePanel.add(ipFilterRadioButton);
        selectionModePanel.add(portFilterRadioButton);
        topPanel.add(selectionModePanel);

        BorderedPanel modePanel = new BorderedPanel("Mode");
        modePanel.add(browseRadioButton);
        modePanel.add(flowRadioButton);
        topPanel.add(modePanel);

        DisableablePanel browsePanel = new DisableablePanel("Browse packets from...");
        browsePanel.add(srcRadioButton);
        browsePanel.add(destRadioButton);
        browsePanel.add(browseComboBox);
        topPanel.add(browsePanel);

        DisableablePanel flowPanel = new DisableablePanel("View packet flow from...");
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

        // Setup radio buttons to select the filter to select packets by
        ButtonGroup filterButtonGroup = new ButtonGroup();
        filterButtonGroup.add(ipFilterRadioButton);
        filterButtonGroup.add(portFilterRadioButton);
        ipFilterRadioButton.setSelected(true);

        ipFilterRadioButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setFilterMode(true);
            }
        });

        portFilterRadioButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setFilterMode(false);
            }
        });

        // Setup radio buttons to select whether we want to select packets based on their source or their destination
        ButtonGroup radioButtonGroup = new ButtonGroup();
        radioButtonGroup.add(srcRadioButton);
        radioButtonGroup.add(destRadioButton);
        srcRadioButton.setSelected(true);

        srcRadioButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                browseComboBox.setModel(ipFilterRadioButton.isSelected() ? browseSrcIPComboBoxModel : browseSrcPortComboBoxModel);
                displaySelectedHostData();
            }
        });

        destRadioButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                browseComboBox.setModel(ipFilterRadioButton.isSelected() ? browseDestIPComboBoxModel : browseDestPortComboBoxModel);
                displaySelectedHostData();
            }
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
            System.out.println(e);
        }
    }

    private void setTableModel(Packet[] packets, boolean isSrcHosts) {
        if (packets != null) {
            model = new PacketTableModel(packets, isSrcHosts);
        } else {
            // No valid packet data available so we display an empty table
            model = new PacketTableModel(new Packet[0], isSrcHosts);
        }

        model.addTableModelListener(tableModelListener);
        packetTable.setModel(model);
    }

    private void displaySelectedHostData() {
        Packet[] packets = null;
        boolean isSrcHosts = srcRadioButton.isSelected();

        if (ipFilterRadioButton.isSelected()) {
            String hostIP = (String) browseComboBox.getSelectedItem();
            if (hostIP != null) {
                packets = simulator.getTableData(hostIP, isSrcHosts);
            }
        } else {
            Integer hostPort = (Integer) browseComboBox.getSelectedItem();
            if (hostPort != null) {
                packets = simulator.getTableData(hostPort, isSrcHosts);
            }
        }
        setTableModel(packets, isSrcHosts);
    }

    private void displaySelectedPacketFlowData() {
        Packet[] packets = null;

        if (ipFilterRadioButton.isSelected()) {
            String srcIP = (String) flowSrcComboBox.getSelectedItem();
            String destIP = (String) flowDestComboBox.getSelectedItem();
            if (srcIP != null && destIP != null) {
                packets = simulator.getPacketFlowTableData(srcIP, destIP);
            }
        } else {
            Integer srcPort = (Integer) flowSrcComboBox.getSelectedItem();
            Integer destPort = (Integer) flowDestComboBox.getSelectedItem();
            if (srcPort != null && destPort != null) {
                packets = simulator.getPacketFlowTableData(srcPort, destPort);
            }
        }
        setTableModel(packets, true);
    }

    private void setFilterMode(boolean filterByIP) {
        if (filterByIP) {
            browseComboBox.setModel(srcRadioButton.isSelected() ? browseSrcIPComboBoxModel : browseDestIPComboBoxModel);
            flowSrcComboBox.setModel(flowSrcIPComboBoxModel);
            flowDestComboBox.setModel(flowDestIPComboBoxModel);
        } else {
            browseComboBox.setModel(srcRadioButton.isSelected() ? browseSrcPortComboBoxModel : browseDestPortComboBoxModel);
            flowSrcComboBox.setModel(flowSrcPortComboBoxModel);
            flowDestComboBox.setModel(flowDestPortComboBoxModel);
        }

        if (browseRadioButton.isSelected()) {
            displaySelectedHostData();
        } else {
            displaySelectedPacketFlowData();
        }

    }

    private void loadComboBoxOptions() {
        String[] srcIPs = simulator.getUniqueSortedSourceHostIPs();
        String[] destIPs = simulator.getUniqueSortedDestHostIPs();

        // Create independent models for each view mode so that their associated combo boxes can remember their state
        browseSrcIPComboBoxModel = new DefaultComboBoxModel<>(srcIPs);
        browseDestIPComboBoxModel = new DefaultComboBoxModel<>(destIPs);
        flowSrcIPComboBoxModel = new DefaultComboBoxModel<>(srcIPs);
        flowDestIPComboBoxModel = new DefaultComboBoxModel<>(destIPs);

        Integer[] srcPorts = simulator.getUniqueSortedSourceHostPorts();
        Integer[] destPorts = simulator.getUniqueSortedDestHostPorts();

        browseSrcPortComboBoxModel = new DefaultComboBoxModel<>(srcPorts);
        browseDestPortComboBoxModel = new DefaultComboBoxModel<>(destPorts);
        flowSrcPortComboBoxModel = new DefaultComboBoxModel<>(srcPorts);
        flowDestPortComboBoxModel = new DefaultComboBoxModel<>(destPorts);

        setFilterMode(ipFilterRadioButton.isSelected());
    }

    static class BorderedPanel extends JPanel {
        TitledBorder border;

        /**
         * Creates a new BorderedPanel with no title
         */
        public BorderedPanel() {
            border = BorderFactory.createTitledBorder(BORDERED_PANEL_BORDER);
            setLayout(BORDERED_PANEL_LAYOUT);
            setBorder(border);
        }

        /**
         * Creates a new BorderedPanel with the given title
         *
         * @param title this panel's title
         */
        public BorderedPanel(String title) {
            this();
            border.setTitle(title);
        }

        @Override
        public Dimension getPreferredSize() {
            // By default the size of a panel's border is not taken into account when determining it's preferred size
            // so this ensures that the text on the TitledBorder is not truncated if the contents are shorter than it
            Dimension dim = super.getPreferredSize();
            dim.setSize(Math.max(dim.getWidth(),
                    border.getBorderInsets(this).right + border.getMinimumSize(this).getWidth()),
                    dim.getHeight());
            return dim;
        }
    }

    static class DisableablePanel extends BorderedPanel {

        /**
         * Creates a new DisableablePanel with no title
         */
        public DisableablePanel() {
            super();
        }

        /**
         * Creates a new DisableablePanel with the given title
         *
         * @param title this panel's title
         */
        public DisableablePanel(String title) {
            super(title);
        }

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

}
