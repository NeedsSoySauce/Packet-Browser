import javax.swing.table.AbstractTableModel;

public class PacketTableModel extends AbstractTableModel implements PacketTableConstants {

    private int timestampCol = TIMESTAMP_COL;
    private int srcCol = SRC_COL;
    private int srcPortCol = SRC_PORT_COL;
    private int destCol = DEST_COL;
    private int destPortCol = DEST_PORT_COL;
    private int sizeCol = SIZE_COL;

    private int sum;
    private double mean;

    private String[] columnNames = new String[6];
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

            if ((Integer) getValueAt(rowIndex, columnIndex) == packetSize) {
                return;
            }

            packets[rowIndex].setIpPacketSize(packetSize);
            fireTableCellUpdated(rowIndex, columnIndex);

            // Update the sum and mean values if a packet size has been changed
            if (columnIndex == sizeCol) {
                updateSumAndMean();
                fireTableRowsUpdated(packets.length + 1, packets.length + 2);
            }
        }
    }

    private void updateSumAndMean() {
        sum = 0;
        for (Packet packet : packets) {
            sum += packet.getIpPacketSize();
        }
        mean = packets.length > 0 ? (double) sum / packets.length : 0;
    }

    @Override
    public int getRowCount() {
        return packets.length + 2;
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

        if (rowIndex >= getRowCount() || rowIndex < 0) {
            throw new IndexOutOfBoundsException("Illegal rowIndex");
        } else if (columnIndex >= getColumnCount() || columnIndex < 0) {
            throw new IndexOutOfBoundsException("Illegal columnIndex");
        }

        // Return sum or mean
        if (rowIndex >= packets.length) {

            if (columnIndex != sizeCol) {
                return null;
            }

            if (rowIndex == packets.length) {
                return sum;
            } else if (rowIndex == packets.length + 1) {
                return mean;
            }
        }

        Packet packet = packets[rowIndex];

        if (columnIndex == timestampCol) {
            return packet.getTimeStamp();
        } else if (columnIndex == srcCol) {
            return packet.getSourceHostIP();
        } else if (columnIndex == srcPortCol) {
            return packet.getSourceHostPort();
        } else if (columnIndex == destCol) {
            return packet.getDestinationHostIP();
        } else if (columnIndex == destPortCol) {
            return packet.getSourceHostPort();
        } else {
            return packet.getIpPacketSize();
        }

    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return rowIndex < packets.length && columnIndex == sizeCol;
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
