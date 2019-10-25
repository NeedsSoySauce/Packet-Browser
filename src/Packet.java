import java.util.Arrays;

interface TraceFileConstants {
    int ID_COL = 0;
    int TIMESTAMP_COL = 1;
    int SRC_IP_COL = 2;
    int SRC_PORT_COL = 3;
    int DEST_IP_COL = 4;
    int DEST_PORT_COL = 5;
    int IP_PACKET_SIZE_COL = 7;
    int MAX_COL = Arrays.stream(new int[]{
            ID_COL, TIMESTAMP_COL, SRC_IP_COL, SRC_PORT_COL, DEST_IP_COL, DEST_PORT_COL, IP_PACKET_SIZE_COL
    }).max().getAsInt();
}

public class Packet implements TraceFileConstants {
    private Host srcHost, destHost;
    private Double timestamp;
    private Integer size, lineIndex;
    private String[] data;

    /**
     * Creates a new Packet
     *
     * @param tabDelimitedData a string of tab delimited data where the first eight elements are in the form "{@code
     *                         <id> <timestamp> <src ip> <src port> <dest ip> <dest port> <ethernet frame size> <IP
     *                         packet size}"
     * @throws IllegalArgumentException Invalid data in tabDelimitedData
     */
    public Packet(String tabDelimitedData) throws IllegalArgumentException {
        String[] data = tabDelimitedData.split("\\t", MAX_COL + 2);

        // Extend all data so that it's length is at least 8
        if (data.length < MAX_COL + 1) {
            data = Arrays.copyOf(data, MAX_COL + 1);
            // Replace all null values with empty strings
            for (int i = 0; i < data.length; i++) {
                if (data[i] == null) {
                    data[i] = "";
                }
            }
        }

        this.data = data;
        try {
            lineIndex = data[ID_COL].isEmpty() ? null : Integer.parseInt(data[ID_COL]);
            srcHost = new Host(data[SRC_IP_COL], data[SRC_PORT_COL].isEmpty() ? null : Integer.parseInt(data[SRC_PORT_COL]));
            destHost = new Host(data[DEST_IP_COL], data[DEST_PORT_COL].isEmpty() ? null : Integer.parseInt(data[DEST_PORT_COL]));
            timestamp = data[TIMESTAMP_COL].isEmpty() ? null : Double.parseDouble(data[TIMESTAMP_COL]);
            size = data[IP_PACKET_SIZE_COL].isEmpty() ? null : Integer.parseInt(data[IP_PACKET_SIZE_COL]);
        } catch (NumberFormatException e) {
            System.out.println(e);
            throw new IllegalArgumentException("Invalid data in tabDelimitedData");
        }

    }

    /**
     * @return a string of tab delimited data where the first eight elements are in the form "{@code
     * <id> <timestamp> <src ip> <src port> <dest ip> <dest port> <ethernet frame size> <IP
     * packet size}"
     */
    public String getTabDelimitedData() {
        return String.join("\t", data);
    }


    /**
     * @return this packet's source host
     */
    public Host getSourceHost() {
        return srcHost;
    }

    /**
     * @param host this packets new host
     */
    public void setSourceHost(Host host) {
        srcHost = host;
        data[SRC_IP_COL] = host.getIp();
    }

    /**
     * @return this packet's destination host
     */
    public Host getDestinationHost() {
        return destHost;
    }

    /**
     * @param host this packets new host
     */
    public void setDestinationHost(Host host) {
        destHost = host;
        data[DEST_IP_COL] = host.getIp();
    }

    /**
     * @return this packet's source host's ip
     */
    public String getSourceHostIP() {
        return srcHost.getIp();
    }

    /**
     * @return this packet's source host's port
     */
    public Integer getSourceHostPort() {
        return srcHost.getPort();
    }

    /**
     * @return this packet's destination host's ip
     */
    public String getDestinationHostIP() {
        return destHost.getIp();
    }

    /**
     * @return this packet's destination host's port
     */
    public Integer getDestinationHostPort() {
        return destHost.getPort();
    }

    /**
     * @return the line this packet's data was read from
     */
    public int getLineIndex() {
        return lineIndex;
    }

    /**
     * @param lineIndex the line this packet's data was read from
     */
    public void setLineIndex(int lineIndex) {
        this.lineIndex = lineIndex;
    }

    /**
     * @return this packets timestamp
     */
    public double getTimeStamp() {
        return timestamp;
    }

    /**
     * @param time the timestamp this packet was recorded
     */
    public void setTimeStamp(double time) {
        this.timestamp = time;
        data[TIMESTAMP_COL] = String.valueOf(time);
    }

    /**
     * @return the size of this packet in bytes
     */
    public Integer getIpPacketSize() {
        return size;
    }

    /**
     * @param size the size of this packet in bytes
     */
    public void setIpPacketSize(Integer size) {
        this.size = size;
        data[IP_PACKET_SIZE_COL] = String.valueOf(size);
    }

    @Override
    public String toString() {
        return String.format("src=%s, dest=%s, timestamp=%.2f, size=%d", srcHost, destHost, timestamp, size);
    }

}
