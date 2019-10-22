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
    private ArrayList<Packet> packets = new ArrayList<>();

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
                packet.setLineIndex(i + 1);
                boolean isValidSrcIP = pattern.matcher(packet.getSourceHost().getIp()).matches();
                boolean isValidDestIP = pattern.matcher(packet.getDestinationHost().getIp()).matches();
                if (isValidSrcIP && isValidDestIP) {
                    packets.add(packet);
                }
            }
        } catch (IOException e) {
            System.out.println("1");
        }
    }

    /**
     * @return an ArrayList of valid Packet objects
     */
    public ArrayList<Packet> getValidPackets() {
        return packets;
    }

    /**
     * @return an array of unique hosts sorted by their ip address
     */
    private Host[] getUniqueSortedHosts(boolean isSrcHost) {

        Function<Packet, Host> getHost = isSrcHost ? Packet::getSourceHost : Packet::getDestinationHost;
        ArrayList<String> hostIPs = new ArrayList<>();
        packets.forEach(packet -> hostIPs.add(getHost.apply(packet).getIp()));

        // Add all host ips to a HashSet to get the unique elements
        Set<String> ipsSet = new HashSet<>(hostIPs);

        // Create a host for each unique source host
        ArrayList<Host> hostArrayList = new ArrayList<>();
        ipsSet.forEach(ip -> hostArrayList.add(new Host(ip)));

        Host[] uniqueHosts = hostArrayList.toArray(new Host[0]);
        Arrays.sort(uniqueHosts);
        return uniqueHosts;
    }

    /**
     * @return an array of unique source hosts sorted by their ip address
     */
    public Host[] getUniqueSortedSourceHosts() {
        return getUniqueSortedHosts(true);
    }

    /**
     * @return an array of unique destination hosts sorted by their ip address
     */
    public Host[] getUniqueSortedDestHosts() {
        return getUniqueSortedHosts(false);
    }

    /**
     * Returns an array of valid Packet objects whose source or destination ip addresses (depending on whether isSrcHost
     * is true) match the given ip address.
     *
     * @param ip        the ip to get data for
     * @param isSrcHost true to match the given ip against each packets source host, otherwise false to match against
     *                  each packet's destination host
     * @return an array of matching packet objects
     */
    public Packet[] getTableData(String ip, boolean isSrcHost) {
        Predicate<Packet> predicate;

        if (isSrcHost) {
            predicate = packet -> packet.getSourceHost().getIp().equals(ip);
        } else {
            predicate = packet -> packet.getDestinationHost().getIp().equals(ip);
        }

        return packets.stream().filter(predicate).toArray(Packet[]::new);
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
        predicate = packet -> packet.getSourceHost().getIp().equals(srcIP) && packet.getDestinationHost().getIp().equals(destIP);
        return packets.stream().filter(predicate).toArray(Packet[]::new);
    }

}




























