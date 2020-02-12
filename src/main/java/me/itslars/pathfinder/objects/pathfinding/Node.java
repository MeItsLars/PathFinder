package me.itslars.pathfinder.objects.pathfinding;

import org.bukkit.Location;

public class Node implements Comparable {

    private String nodeID;
    private double distance = Double.MAX_VALUE;
    private Location location;
    private double heuristic;
    private Node previousNode = null;

    public Node(String nodeID, Location location) {
        this.nodeID = nodeID;
        this.location = location;
    }

    public Node clone() {
        return new Node(nodeID, location.clone());
    }

    public double getDistance() {
        return distance;
    }

    public Node getPreviousNode() {
        return previousNode;
    }

    public Location getLocation() {
        return location;
    }

    public String getNodeID() {
        return nodeID;
    }

    public void setPreviousNode(Node node) {
        this.previousNode = node;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setHeuristic(Node destination) {
        this.heuristic = destination.getLocation().distance(location);
    }

    @Override
    public boolean equals(Object o) {
        return nodeID.equals(((Node) o).getNodeID());
    }

    @Override
    public int compareTo(Object o) {
        Node n = (Node) o;
        return Double.compare(heuristic + distance, n.heuristic + n.distance);
    }
}
