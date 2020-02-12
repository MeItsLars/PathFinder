package me.itslars.pathfinder.objects.pathfinding;

public class Edge {
    private Node start, end;

    public Edge(Node start, Node end) {
        this.start = start;
        this.end = end;
    }

    public Edge clone() {
        return new Edge(start.clone(), end.clone());
    }

    public double getLength() {
        return start.getLocation().distance(end.getLocation());
    }

    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }
}