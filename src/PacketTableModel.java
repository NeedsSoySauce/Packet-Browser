import javax.swing.table.AbstractTableModel;

public class PacketTableModel extends AbstractTableModel implements PacketTableColumns {

    private int timestampCol = TIMESTAMP_COL;
    private int srcCol = SRC_COL;
    private int srcPortCol = SRC_PORT_COL;
    private int destCol = DEST_COL;
    private int destPortCol = DEST_PORT_COL;
    private int sizeCol = SIZE_COL;

    private String[] columnNames = new String[6];
    private Object[][] data;
    private Packet[] packets;

    /**
     * Creates a new PacketTableModel. If isSrcHosts is true, the "Source IP" column will precede the "Destination IP"
     * column.
     *
     * @param packets    the packets to be displayed in a table
     * @param isSrcHosts true if the packets are from the source, and false if they're from the destination
     */
    public PacketTableModel(Packet[] packets, boolean isSrcHosts) {

        columnNames[TIMESTAMP_COL] = TIMESTAMP_COL_NAME;
        columnNames[SRC_COL] = SRC_COL_NAME;
        columnNames[SRC_PORT_COL] = SRC_PORT_COL_NAME;
        columnNames[DEST_COL] = DEST_COL_NAME;
        columnNames[DEST_PORT_COL] = DEST_PORT_COL_NAME;
        columnNames[SIZE_COL] = SIZE_COL_NAME;

        if (!isSrcHosts) {
            srcCol = DEST_COL;
            destCol = SRC_COL;
            columnNames[srcCol] = SRC_COL_NAME;
            columnNames[destCol] = DEST_COL_NAME;

            srcPortCol = DEST_PORT_COL;
            destPortCol = SRC_PORT_COL;
            columnNames[srcPortCol] = SRC_PORT_COL_NAME;
            columnNames[destPortCol] = DEST_PORT_COL_NAME;
        }

        this.packets = packets;
        data = new Object[packets.length + 2][columnNames.length];
        for (int i = 0; i < packets.length; i++) {
            data[i][timestampCol] = packets[i].getTimeStamp();
            data[i][srcCol] = packets[i].getSourceHostIP();
            data[i][srcPortCol] = packets[i].getSourceHostPort();
            data[i][destCol] = packets[i].getDestinationHostIP();
            data[i][destPortCol] = packets[i].getDestinationHostPort();
            data[i][sizeCol] = packets[i].getIpPacketSize();
        }

        updateSumAndMean();
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

        // All packet sizes must be a positive integer
        int packetSize;
        String value = ((String) aValue).trim();

        try {
            packetSize = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // Packet size was not an integer
            return;
        }

        if (packetSize >= 0) {
            data[rowIndex][columnIndex] = packetSize;
            packets[rowIndex].setIpPacketSize(packetSize);
            fireTableCellUpdated(rowIndex, columnIndex);

            // Update the sum and mean values if a packet size has been changed
            if (columnIndex == sizeCol) {
                updateSumAndMean();
                fireTableRowsUpdated(data.length - 2, data.length - 1);
            }
        }
    }

    private void updateSumAndMean() {
        int sum = 0;
        for (Packet packet : packets) {
            sum += packet.getIpPacketSize();
        }
        data[data.length - 2][sizeCol] = sum;
        data[data.length - 1][sizeCol] = packets.length > 0 ? (double) sum / packets.length : 0;
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data[rowIndex][columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return rowIndex < data.length - 2 && columnIndex == sizeCol;
    }

    /**
     * Returns the packet represented at the given row index
     *
     * @param rowIndex the row to get the packet from
     * @return the packet at the given row index
     */
    public Packet getPacketAt(int rowIndex) {
        return packets[rowIndex];
    }
}
