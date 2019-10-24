import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Simulator {

    private Pattern pattern = Pattern.compile("(?:2(?:[0-4]\\d|5[0-5])|1\\d{2}|[1-9]\\d|\\d)(?:\\.(?:2" +
            "(?:[0-4]\\d|5[0-5])|1\\d{2}|[1-9]\\d|\\d)){3}");

    private ArrayList<Packet> validIPPackets = new ArrayList<>();
    private ArrayList<Packet> validPortPackets = new ArrayList<>();

    /**
     * Creates a new Simulator
     *
     * @param file a file containing tab delimited lines of packet data
     * @see Packet#Packet(String)
     */
    public Simulator(File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (int i = 0; i < lines.size(); i++) {
                Packet packet = new Packet(lines.get(i));
                packet.setLineIndex(i);
                if (hasValidIPData(packet)) {
                    validIPPackets.add(packet);
                    if (hasValidPortData(packet)) {
                        validPortPackets.add(packet);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private boolean hasValidIPData(Packet packet) {
        return pattern.matcher(packet.getSourceHostIP()).matches()
                && pattern.matcher(packet.getDestinationHostIP()).matches();
    }

    private boolean hasValidPortData(Packet packet) {
        return packet.getSourceHostPort() != null && packet.getDestinationHostPort() != null;
    }

    /**
     * @return an ArrayList of valid Packet objects
     */
    public ArrayList<Packet> getValidIPPackets() {
        return validIPPackets;
    }

    /**
     * @return an array of unique hosts sorted by their ip address
     */
    private String[] getUniqueSortedHostIPs(boolean isSrcHost) {

        Function<Packet, String> getHost = isSrcHost ? Packet::getSourceHostIP : Packet::getDestinationHostIP;
        ArrayList<String> hostIPs = new ArrayList<>();
        validIPPackets.forEach(packet -> hostIPs.add(getHost.apply(packet)));

        // Add all host ips to a HashSet to get the unique elements
        Set<String> ipsSet = new HashSet<>(hostIPs);

        // Create a host for each unique source host
        ArrayList<Host> hostArrayList = new ArrayList<>();
        ipsSet.forEach(ip -> hostArrayList.add(new Host(ip)));

        Host[] uniqueHosts = hostArrayList.toArray(new Host[0]);
        Arrays.sort(uniqueHosts);
        return Arrays.stream(uniqueHosts).map(Host::getIp).toArray(String[]::new);
    }

    /**
     * @return an array of unique source hosts sorted by their ip address
     */
    public String[] getUniqueSortedSourceHostIPs() {
        return getUniqueSortedHostIPs(true);
    }

    /**
     * @return an array of unique destination hosts sorted by their ip address
     */
    public String[] getUniqueSortedDestHostIPs() {
        return getUniqueSortedHostIPs(false);
    }

    /**
     * Returns an array of valid Packet objects whose source or destination ip addresses (depending on whether isSrcHost
     * is true) match the given ip address.
     *
     * @param ip        the ip to get data for
     * @param isSrcHost true to match the given ip against each validIPPackets source host, otherwise false to match
     *                  against each packet's destination host
     * @return an array of matching packet objects
     */
    public Packet[] getTableData(String ip, boolean isSrcHost) {
        Predicate<Packet> predicate;

        if (isSrcHost) {
            predicate = packet -> packet.getSourceHostIP().equals(ip);
        } else {
            predicate = packet -> packet.getDestinationHostIP().equals(ip);
        }

        return validIPPackets.stream().filter(predicate).toArray(Packet[]::new);
    }

    /**
     * Returns an array of valid Packet objects whose source and destination ip addresses match the given ip addresses.
     *
     * @param srcIP  the source ip address
     * @param destIP the destination ip address
     * @return an array of matching packet objects
     */
    public Packet[] getPacketFlowTableData(String srcIP, String destIP) {
        Predicate<Packet> predicate;
        predicate = packet -> packet.getSourceHostIP().equals(srcIP) && packet.getDestinationHostIP().equals(destIP);
        return validIPPackets.stream().filter(predicate).toArray(Packet[]::new);
    }

    /**
     * @return an array of unique sorted port numbers
     */
    private Integer[] getUniqueSortedHostPorts(boolean isSrcHost) {

        // Fix this so it only grabs packets with valid src and dest port values
        Function<Packet, Integer> getHost = isSrcHost ? Packet::getSourceHostPort : Packet::getDestinationHostPort;
        ArrayList<Integer> hostPorts = new ArrayList<>();
        validPortPackets.forEach(packet -> hostPorts.add(getHost.apply(packet)));

        // Add all host ports to a HashSet to get the unique elements
        Set<Integer> portsSet = new HashSet<>(hostPorts);

        Integer[] uniquePorts = portsSet.toArray(new Integer[0]);
        Arrays.sort(uniquePorts);
        return uniquePorts;
    }

    /**
     * @return an array of unique sorted source port numbers
     */
    public Integer[] getUniqueSortedSourceHostPorts() {
        return getUniqueSortedHostPorts(true);
    }

    /**
     * @return an array of unique sorted destination port numbers
     */
    public Integer[] getUniqueSortedDestHostPorts() {
        return getUniqueSortedHostPorts(false);
    }

    /**
     * Returns an array of valid Packet objects whose source or destination port numbers (depending on whether isSrcHost
     * is true) match the given port number.
     *
     * @param port      the port number to get data for
     * @param isSrcHost true to match the given port number against each validIPPackets source port, otherwise false to
     *                  match against each packet's destination port
     * @return an array of matching packet objects
     */
    public Packet[] getTableData(Integer port, boolean isSrcHost) {
        Predicate<Packet> predicate;

        if (isSrcHost) {
            predicate = packet -> packet.getSourceHostPort().equals(port);
        } else {
            predicate = packet -> packet.getDestinationHostPort().equals(port);
        }
        return validPortPackets.stream().filter(predicate).toArray(Packet[]::new);
    }

    /**
     * Returns an array of valid Packet objects whose source and destination port numbers match the given port numbers.
     *
     * @param srcPort  the source port number
     * @param destPort the destination port number
     * @return an array of matching packet objects
     */
    public Packet[] getPacketFlowTableData(Integer srcPort, Integer destPort) {
        Predicate<Packet> predicate;
        predicate = packet -> packet.getSourceHostPort().equals(srcPort) && packet.getDestinationHostPort().equals(destPort);
        return validPortPackets.stream().filter(predicate).toArray(Packet[]::new);
    }


}




























