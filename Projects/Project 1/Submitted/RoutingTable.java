public class RoutingTable {
    public int destination;
    public int cost;
    public int next_hop;

    public RoutingTable(int destination, int cost, int next_hop) {
        this.destination = destination;
        this.cost = cost;
        this.next_hop = next_hop;
    }
    // Constructor for first time node creation
    public RoutingTable(int destination, int cost) {
        this.destination = destination;
        this.cost = cost;
        this.next_hop = destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public void setNext_hop(int next_hop) {
        this.next_hop = next_hop;
    }


    public int getCost() {
        return cost;
    }

    public int getDestination() {
        return destination;
    }

    public int getNext_hop() {
        return next_hop;
    }

    public void print() {
        System.out.println("Packet is sent to node: " + next_hop);
    }
}
