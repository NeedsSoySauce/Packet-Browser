import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
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
                if (pattern.matcher(packet.getSourceHost()).matches() && pattern.matcher(packet.getSourceHost()).matches()) {
                    packets.add(packet);
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * @return an ArrayList of valid Packet objects
     */
    public ArrayList<Packet> getValidPackets() {
        return packets;
    }

    /**
     * @param hostIPs an array of host ip addresses
     * @return an array of unique hosts sorted by their ip address
     */
    private Host[] getUniqueSortedHosts(ArrayList<String> hostIPs) {
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
        ArrayList<String> srcHostIPs = new ArrayList<>();
        packets.forEach(packet -> srcHostIPs.add(packet.getSourceHost()));
        return getUniqueSortedHosts(srcHostIPs);
    }

    /**
     * @return an array of unique destination hosts sorted by their ip address
     */
    public Host[] getUniqueSortedDestHosts() {
        ArrayList<String> destHostIPs = new ArrayList<>();
        packets.forEach(packet -> destHostIPs.add(packet.getDestinationHost()));
        return getUniqueSortedHosts(destHostIPs);
    }

    /**
     * @param ip        the ip to get data for
     * @param isSrcHost true to match the given ip against each packets source host, otherwise false to match against
     *                  each packet's destination host
     * @return an array of valid Packet objects whose source or destination ip addresses (depending on whether isSrcHost
     * is true) match the given ip address.
     */
    public Packet[] getTableData(String ip, boolean isSrcHost) {
        Predicate<Packet> predicate;

        if (isSrcHost) {
            predicate = packet -> packet.getSourceHost().equals(ip);
        } else {
            predicate = packet -> packet.getDestinationHost().equals(ip);
        }

        return packets.stream().filter(predicate).toArray(Packet[]::new);
    }

}
