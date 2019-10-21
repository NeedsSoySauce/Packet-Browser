import java.util.Arrays;

public class Packet {

    private Host srcHost, destHost;
    private double time;
    private int size, lineIndex;
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
        lineIndex = data[0].isEmpty() ? 0 : Integer.parseInt(data[0]);
        srcHost = new Host(data[2]);
        destHost = new Host(data[4]);
        time = data[1].isEmpty() ? 0 : Double.parseDouble(data[1]);
        size = data[7].isEmpty() ? 0 : Integer.parseInt(data[7]);
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
    public String getSourceHost() {
        return srcHost.getIp();
    }

    /**
     * @param src a string that consists of four decimal numbers between 0 and 255 separated by dots
     */
    public void setSourceHost(String src) {
        srcHost.setIp(src);
        data[2] = src;
    }

    /**
     * @return this packet's destination host's ip
     */
    public String getDestinationHost() {
        return destHost.getIp();
    }

    /**
     * @param dest a string that consists of four decimal numbers between 0 and 255 separated by dots
     */
    public void setDestinationHost(String dest) {
        destHost.setIp(dest);
        data[3] = dest;
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
