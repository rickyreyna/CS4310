public class Packet {
    int destination;
    int source;

    public Packet(int source, int destination) {
        this.source = source;
        this.destination = destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }
    public void setSource(int source) {
        this.source = source;
    }

    public int getDestination() {
        return destination;
    }

    public int getSource() {
        return source;
    }
}
