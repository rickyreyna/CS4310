import java.util.ArrayList;
import java.util.List;

public class Node {
    public int nodeID, cost;
    List<RoutingTable> RT = new ArrayList<RoutingTable>();
    List<Integer> neighbors = new ArrayList<Integer>();


    public Node(int node) {
        this.nodeID = node;
    }

    // Constructor for first time node
    public Node(int node1, int node2, int cost) {
        this.nodeID = node1;
        RT.add(new RoutingTable(node2, cost));
    }


    public void setNodeID(int node) {
        this.nodeID = node;
    }


    public int getNodeID() {
        return this.nodeID;
    }

    public List<RoutingTable> getRoutingTable() {
        return RT;
    }

    public List<Integer> getNeighbors() {
        return neighbors;
    }


    public void populateNodeNeighbors() {
        for(int i = 0; i < RT.size(); ++i) {
            neighbors.add(RT.get(i).getDestination());
        }
    }

    public Packet sendPacket(Packet packet) {
        for(int i = 0; i < RT.size(); ++i) {
            if(packet.getDestination() == RT.get(i).getDestination()) {
                packet.setSource(RT.get(i).getNext_hop());
                RT.get(i).print();
                return packet;
            }
        }
        return packet;
    }

    // Neighbors send DV packet to current node
    public boolean sendDvPacket(Node neighbor) {
        boolean update = false;
        // Get's the cost to send packet to neighbor
        for(int i = 0; i < RT.size(); ++i) {
            if(RT.get(i).getDestination() == neighbor.getNodeID()) {
                cost = RT.get(i).getCost();
            }
        }

        boolean flag = false;
        // Checks if neighbors has a better path for nodes in current Routing Table
        for(int i = 0; i < neighbor.RT.size(); ++i) {
            for(int j = 0; j < RT.size(); ++j) {
                if(RT.get(j).getDestination() == neighbor.RT.get(i).getDestination()) {
                    if(RT.get(j).getCost() > neighbor.RT.get(i).getCost() + cost) {
                        for(int k = 0; k < RT.size(); ++k) {
                            if(RT.get(k).getDestination() == neighbor.getNodeID()) {
                                RT.get(j).setCost(neighbor.RT.get(i).getCost() + cost);
                                RT.get(j).setNext_hop(RT.get(k).getNext_hop());
                            }
                        }
                        update = true;
                    }
                   flag = true;
                }

            }
            // If neighbors DV has a node that is not in current Routing Table, add it to the RT
            if(flag == false && neighbor.RT.get(i).getDestination() != this.nodeID) {
                for(int k = 0; k < RT.size(); ++k) {
                    if(RT.get(k).getDestination() == neighbor.getNodeID()) {
                        RT.add(new RoutingTable(neighbor.RT.get(i).getDestination(), neighbor.RT.get(i).getCost() + cost, RT.get(k).getNext_hop()));
                    }
                }
                update = true;
            }
            else {
                flag = false;
            }
        }
        return update;
    }

    public void fillRoutingTable(int destination, int cost, int next_hop) {
        RT.add(new RoutingTable(destination, cost, next_hop));
    }

    public void printRT() {
        System.out.println("Routing Table:");
        for(int i = 0; i < RT.size(); ++i) {
            System.out.println("Des: " + RT.get(i).getDestination() + " Cost: " + RT.get(i).getCost() + " Next-Hop: " + RT.get(i).getNext_hop());
        }
    }
}
