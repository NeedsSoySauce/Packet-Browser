import java.util.Arrays;

public class Host implements Comparable<Host> {

    private String ip;

    /**
     * Creates a new Host
     *
     * @param ip the hosts ip address
     */
    public Host(String ip) {
        this.ip = ip;
    }

    /**
     * Sets this hosts ip to the given ip
     * @param ip the hosts new ip address
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the hosts ip address
     */
    public String getIp() {
        return ip;
    }

    @Override
    public String toString() {
        return ip;
    }

    @Override
    public int compareTo(Host o) {

        // Split the ips into their numeric parts
        int[] thisNumbers = Arrays.stream(ip.split("\\.")).mapToInt(Integer::parseInt).toArray();
        int[] oNumbers = Arrays.stream(o.ip.split("\\.")).mapToInt(Integer::parseInt).toArray();
        int diff;

        // Compare the numeric parts of each ip (assumes the IP is a valid IPv4 address)
        for (int i = 0; i < 3; i++) {
            diff = thisNumbers[i] - oNumbers[i];
            if (diff != 0) {
                return diff;
            }
        }

        return thisNumbers[3] - oNumbers[3];
    }

}
