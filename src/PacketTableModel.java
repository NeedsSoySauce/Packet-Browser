import javax.swing.table.AbstractTableModel;

public class PacketTableModel extends AbstractTableModel {

    private String[] columnNames = {"Timestamp", "Destination", "IP Packet Size"};
    private Object[][] data;
    private Packet[] packets;

    /**
     * Creates a new PacketTableModel
     *
     * @param packets    the packets to be displayed in a table
     * @param isSrcHosts true if the packets are from the source, and false if they're from the destination
     */
    public PacketTableModel(Packet[] packets, boolean isSrcHosts) {
        this.packets = packets;
        data = new Object[packets.length + 2][3];
        for (int i = 0; i < packets.length; i++) {
            data[i][0] = packets[i].getTimeStamp();
            data[i][1] = !isSrcHosts ? packets[i].getDestinationHost() : packets[i].getSourceHost();
            data[i][2] = packets[i].getIpPacketSize();
        }

        data[data.length - 2][1] = "Sum";
        data[data.length - 1][1] = "Average";

        updateSumAndMean();

        if (isSrcHosts) {
            columnNames[1] = "Source";
        }
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

        if (packetSize > 0) {
            data[rowIndex][columnIndex] = packetSize;
            packets[rowIndex].setIpPacketSize(packetSize);
            fireTableCellUpdated(rowIndex, columnIndex);

            // Update the sum and mean values if a packet size has been changed
            if (columnIndex == 2) {
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
        data[data.length - 2][2] = sum;
        data[data.length - 1][2] = (double) sum / packets.length;
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
        return rowIndex < data.length - 2 && columnIndex == 2;
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
