package me.itslars.pathfinder.objects.pathfinding;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    private List<Node> nodes;
    private List<Edge> edges;

    public Graph(List<Node> nodes, List<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public List getNodes() {
        return nodes;
    }

    public static ArrayList<Node> getNeighbors(List<Edge> edges, Node n) {
        ArrayList<Node> neighbors = new ArrayList<>();
        for(Edge edge : edges) {
            if(edge.getStart().equals(n)) {
                neighbors.add(edge.getEnd());
            } else if(edge.getEnd().equals(n)) {
                neighbors.add(edge.getStart());
            }
        }
        return neighbors;
    }

    public static Double getDistanceFrom(List<Edge> edges, Node start, Node end) {
        for(Edge e : edges) {
            if((e.getStart().equals(start) && e.getEnd().equals(end)) ||
                    (e.getEnd().equals(start) && e.getStart().equals(end)))
                return e.getLength();
        }
        return null;
    }
}