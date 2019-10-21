import javax.swing.table.AbstractTableModel;

public class PacketTableModel extends AbstractTableModel {

    public static final String TIMESTAMP_COL_NAME = "Timestamp";
    public static final String SRC_COL_NAME = "Source IP";
    public static final String DEST_COL_NAME = "Destination IP";
    public static final String SIZE_COL_NAME = "IP Packet Size";
    public static final String SUM_ROW_NAME = "Sum";
    public static final String MEAN_ROW_NAME = "Average";

    public static final int TIMESTAMP_COL = 0;
    public static final int SRC_COL = 1;
    public static final int DEST_COL = 2;
    public static final int SIZE_COL = 3;

    private String[] columnNames = new String[4];
    private Object[][] data;
    private Packet[] packets;

    /**
     * Creates a new PacketTableModel. If isSrcHosts is true, the "Source IP" column will precede the "Destination
     * IP" column.
     *
     * @param packets    the packets to be displayed in a table
     * @param isSrcHosts true if the packets are from the source, and false if they're from the destination
     */
    public PacketTableModel(Packet[] packets, boolean isSrcHosts) {

        columnNames[TIMESTAMP_COL] = TIMESTAMP_COL_NAME;
        columnNames[SRC_COL] = SRC_COL_NAME;
        columnNames[DEST_COL] = DEST_COL_NAME;
        columnNames[SIZE_COL] = SIZE_COL_NAME;

        int srcCol = SRC_COL;
        int destCol = DEST_COL;
        if (!isSrcHosts) {
            int temp = srcCol;
            srcCol = destCol;
            destCol = temp;
            columnNames[srcCol] = SRC_COL_NAME;
            columnNames[destCol] = DEST_COL_NAME;
        }

        this.packets = packets;
        data = new Object[packets.length + 2][4];
        for (int i = 0; i < packets.length; i++) {
            data[i][TIMESTAMP_COL] = packets[i].getTimeStamp();
            data[i][srcCol] = packets[i].getSourceHost();
            data[i][destCol] = packets[i].getDestinationHost();
            data[i][SIZE_COL] = packets[i].getIpPacketSize();
        }

        data[data.length - 2][SIZE_COL - 1] = SUM_ROW_NAME;
        data[data.length - 1][SIZE_COL - 1] = MEAN_ROW_NAME;

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
            if (columnIndex == SIZE_COL) {
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
        data[data.length - 2][SIZE_COL] = sum;
        data[data.length - 1][SIZE_COL] = (double) sum / packets.length;
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
        return rowIndex < data.length - 2 && columnIndex == SIZE_COL;
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
