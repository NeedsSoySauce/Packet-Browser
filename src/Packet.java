import java.util.Arrays;

public class Packet {

    private Host srcHost, destHost;
    private Double time;
    private Integer size, lineIndex;
    private String[] data;

    /**
     * Creates a new Packet
     *
     * @param tabDelimitedData a string of tab delimited data where the first eight elements are in the form "{@code
     *                         <id> <timestamp> <src ip> <src port> <dest ip> <dest port> <ethernet frame size> <IP
     *                         packet size}"
     */
    public Packet(String tabDelimitedData) {
        String[] data = tabDelimitedData.split("\\t", 9);

        // Extend all data so that it's length is at least 8
        if (data.length < 8) {
            data = Arrays.copyOf(data, 8);
            // Replace all null values with empty strings
            for (int i = 0; i < data.length; i++) {
                if (data[i] == null) {
                    data[i] = "";
                }
            }
        }

        this.data = data;
        lineIndex = data[0].isEmpty() ? null : Integer.parseInt(data[0]);
        srcHost = new Host(data[2], data[3].isEmpty() ? null : Integer.parseInt(data[3]));
        destHost = new Host(data[4], data[5].isEmpty() ? null : Integer.parseInt(data[5]));
        time = data[1].isEmpty() ? null : Double.parseDouble(data[1]);
        size = data[7].isEmpty() ? null : Integer.parseInt(data[7]);
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
     * @return this packet's source host's ip
     */
    public Host getSourceHost() {
        return srcHost;
    }

    /**
     * @param host this packets new host
     */
    public void setSourceHost(Host host) {
        srcHost = host;
        data[2] = host.getIp();
    }

    /**
     * @return this packet's destination host's ip
     */
    public Host getDestinationHost() {
        return destHost;
    }

    /**
     * @param host this packets new host
     */
    public void setDestinationHost(Host host) {
        destHost = host;
        data[3] = host.getIp();
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
        return time;
    }

    /**
     * @param time the time this packet was recorded
     */
    public void setTimeStamp(double time) {
        this.time = time;
        data[1] = String.valueOf(time);
    }

    /**
     * @return the size of this packet in bytes
     */
    public int getIpPacketSize() {
        return size;
    }

    /**
     * @param size the size of this packet in bytes
     */
    public void setIpPacketSize(int size) {
        this.size = size;
        data[7] = String.valueOf(size);
    }

    @Override
    public String toString() {
        return String.format("src=%s, dest=%s, time=%.2f, size=%d", srcHost, destHost, time, size);
    }

}
