public class Host implements Comparable<Host> {

    private String ip;
    private Integer port;

    /**
     * Creates a new Host with the given ip
     *
     * @param ip the hosts ip address
     */
    public Host(String ip) {
        this(ip, null);
    }

    /**
     * Creates a new host with the given ip and port
     *
     * @param ip   the hosts ip address
     * @param port the hosts port
     */
    public Host(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * @return this host's ip address
     */
    public String getIp() {
        return ip;
    }

    /**
     * Sets this host's ip to the given ip
     *
     * @param ip the hosts new ip address
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return this host's port
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Sets this host's port to the given port number
     *
     * @param port the new port number
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return ip;
    }

    @Override
    public int compareTo(Host o) {
        // Split the ips into their numeric parts
        String[] thisNumbers = ip.split("\\.");
        String[] oNumbers = o.getIp().split("\\.");

        // Compare the numeric parts of each ip (assumes the ip is a valid IPv4 address)
        for (int i = 0; i < 4; i++) {
            // If the string representations are not equal then their integer difference will not be equal to 0
            if (!thisNumbers[i].equals(oNumbers[i])) {
                return Integer.parseInt(thisNumbers[i]) - Integer.parseInt(oNumbers[i]);
            }
        }
        return 0;
    }

}
