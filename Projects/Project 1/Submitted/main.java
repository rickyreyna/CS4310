import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner; // Class to read text files
import java.io.File; // File class

public class main {
    // imports data of topology into a list of nodes
    public static ArrayList<Node> importTopologyData(String nameOfFile, ArrayList<Node> nodes) {
        // Gets all nodes from the topology file
        try {
            File topologyFile = new File(nameOfFile);
            Scanner scnr = new Scanner(topologyFile);
            boolean flag1 = false, flag2 = false;
            int sourceNode, destinationNode, cost;

            int index1 = 0, index2 = 0;


            while (scnr.hasNext()) {
                sourceNode = scnr.nextInt();
                destinationNode = scnr.nextInt();
                cost = scnr.nextInt();

                // Checks if nodes already exists
                for(int i = 0; i < nodes.size(); ++i) {
                    if(sourceNode == nodes.get(i).getNodeID()) {
                        flag1 = true;
                        index1 = i;
                    }
                    if(destinationNode == nodes.get(i).getNodeID()) {
                        flag2 = true;
                        index2 = i;
                    }
                }

                // If nodes does not exist, they will be added
                if(flag1 == false && flag2 == false) {
                    Node Node1 = new Node(sourceNode, destinationNode, cost);
                    nodes.add(Node1);
                    Node Node2 = new Node(destinationNode, sourceNode, cost);
                    nodes.add(Node2);
                }

                // If both nodes exists. Update Routing Table
                else if(flag1 == true && flag2 == true) {
                    nodes.get(index1).fillRoutingTable(destinationNode, cost, destinationNode);
                    nodes.get(index2).fillRoutingTable(sourceNode, cost, sourceNode);
                    flag1 = flag2 = false; // Resets flags for the next nodes
                    for(int i = 0; i < nodes.size(); ++i) {
                        nodes.get(i).getNodeID();
                    }
                }

                else if(flag1 == true && flag2 == false) {
                    nodes.get(index1).fillRoutingTable(destinationNode, cost, destinationNode);
                    Node Node2 = new Node(destinationNode, sourceNode, cost);
                    nodes.add(Node2);
                    flag1 = false; // Resets flag for the next nodes
                }

                else if(flag1 == false && flag2 == true) {
                    nodes.get(index2).fillRoutingTable(sourceNode, cost, sourceNode);
                    Node Node1 = new Node(sourceNode, destinationNode, cost);
                    nodes.add(Node1);
                    flag2 = false; // Resets flag for the next nodes
                }
            }
            scnr.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("Error: File not found.");
        }

        return nodes;

    }

    // To route your own packet, create a Packet object with it's source and desired destination then pass it as an argument.
    public static void routePacket(Packet packet, ArrayList<Node> nodes) {
        int index = 0;
        Packet temp = new Packet(packet.getSource(), packet.getDestination());
        while(temp.getSource() != packet.getDestination() ) {
            for(int i = 0; i < nodes.size(); ++i) {
                if(packet.getSource() == nodes.get(i).getNodeID()) {
                    index = i;
                }
            }
            temp = nodes.get(index).sendPacket(packet);
        }
        System.out.println("Packet has arrived! It is in node: " + temp.getSource());
    }

    public static void main(String[] args){
        ArrayList<Node> nodes = new ArrayList<Node>();

        String fileName = "";
        int numOfRoundsToSimulate = 10;
        if (0 < args.length) {
            fileName = args[0];
            numOfRoundsToSimulate = Integer.parseInt(args[1]);

        }
        importTopologyData(fileName, nodes);

        // After getting all the data from topology. This will save the node's neighbors.
        for(int i = 0; i < nodes.size(); ++i) {
            nodes.get(i).populateNodeNeighbors();
        }

        List<Integer> nodeNeighbors = new ArrayList<Integer>();
        int numOfRoundsTillConvergence = 0, count = 2, lastNode = 0;
        boolean convergence = false;
        // Updates all nodes routing table with the best path
        while(convergence == false || numOfRoundsToSimulate != 0) {
            boolean update = false;
            // First loop get's all the neighbors from the first node, each iteration moves to the next node
            for(int i = 0; i < nodes.size(); ++i) {
                nodeNeighbors = nodes.get(i).getNeighbors();
                // Second loop will traverse to the list of neighbors of the node
                for(int k = 0; k < nodeNeighbors.size(); ++k) {
                    for(int j = 0; j < nodes.size(); ++j) {
                        // Find the nodeID that is the neighbor of the node that is currently receiving the DV pack
                        if(nodes.get(j).getNodeID() == nodeNeighbors.get(k) ) {
                           // If it is found, the node will get that nodes routing table and will check for better paths and nodes that aren't in the current routing table
                           update =  nodes.get(i).sendDvPacket(nodes.get(j));
                           // If there was an update in the routing table, it should iterate one more time to let everyone know about new path.
                           if(update == true) {
                               count = 2;
                               lastNode = nodes.get(j).getNodeID(); // Get's the last Node ID used to converge
                           }
                        }
                    }
                }
            }

            if(update == false) {
                count--;
            }
            if(count == 0) {
                convergence = true;
            }

            if(convergence == false) {
                numOfRoundsTillConvergence++; // Counts the number of numOfRoundsTillConvergence it has gone through
            }
            numOfRoundsToSimulate--;
        }

        // This calculates the total number of DB packets sent every round
        int dvPacketsSentEveryRound = 0;
        for(int i = 0; i < nodes.size(); ++i) {
            nodeNeighbors = nodes.get(i).getNeighbors();
            dvPacketsSentEveryRound += nodeNeighbors.size();
        }

        System.out.println("\n---------------------------- FINAL ROUTING TABLE ----------------------------");
        for(int i = 0; i < nodes.size(); ++i) {
            System.out.println("\nNode: " + nodes.get(i).getNodeID());
            nodes.get(i).printRT();
            System.out.println("Neighbors: " + nodes.get(i).neighbors);
        }
        System.out.println("\n\n1. How many rounds did it take each network to converge?");
        System.out.println("Total rounds till convergence: " + numOfRoundsTillConvergence);
        System.out.println("2. What is the ID of the last node to converge in each network?");
        System.out.println("ID of last node used for convergence:  " + lastNode);
        System.out.println("3. How many DV messages were sent in total until each network converged?");
        System.out.println("Total number of DV packets till convergence: " + (dvPacketsSentEveryRound*numOfRoundsTillConvergence) + "\n\n");

        // Checks which topology file we are using for the packet simulation
        if(fileName.equals("topology1.txt")) {
            int index = 0;
            Packet packet = new Packet(0, 3);
            System.out.println("1. For the first topology: node 0 receives a data packet destined to node 3.");
            System.out.println("Sending packet with source \"" + packet.getSource() + "\" and destination \"" + packet.getDestination() + "\"");
            Packet temp = new Packet(packet.getSource(), packet.getDestination());
            while(temp.getSource() != packet.getDestination() ) {
                for(int i = 0; i < nodes.size(); ++i) {
                    if(packet.getSource() == nodes.get(i).getNodeID()) {
                        index = i;
                    }
                }
                temp = nodes.get(index).sendPacket(packet);
            }
            System.out.println("Packet arrived at the destination:  " + temp.getSource());
        }
        else if(fileName.equals("topology2.txt")) {
            int index = 0;
            Packet packet = new Packet(0, 7);
            System.out.println("1. For the first topology: node 0 receives a data packet destined to node 7.");
            System.out.println("Sending packet with source \"" + packet.getSource() + "\" and destination \"" + packet.getDestination() + "\"");
            Packet temp = new Packet(packet.getSource(), packet.getDestination());
            while(temp.getSource() != packet.getDestination() ) {
                for(int i = 0; i < nodes.size(); ++i) {
                    if(packet.getSource() == nodes.get(i).getNodeID()) {
                        index = i;
                    }
                }
                temp = nodes.get(index).sendPacket(packet);
            }
            System.out.println("Packet has arrived! It is in node: " + temp.getSource());
        }
        else if(fileName.equals("topology3.txt")) {
            int index = 0;
            Packet packet = new Packet(0, 23);
            System.out.println("1. For the first topology: node 0 receives a data packet destined to node 23.");
            System.out.println("Sending packet with source \"" + packet.getSource() + "\" and destination \"" + packet.getDestination() + "\"");
            Packet temp = new Packet(packet.getSource(), packet.getDestination());
            while(temp.getSource() != packet.getDestination() ) {
                for(int i = 0; i < nodes.size(); ++i) {
                    if(packet.getSource() == nodes.get(i).getNodeID()) {
                        index = i;
                    }
                }
                temp = nodes.get(index).sendPacket(packet);
            }
            System.out.println("Packet has arrived! It is in node: " + temp.getSource());
        }
    }
}
