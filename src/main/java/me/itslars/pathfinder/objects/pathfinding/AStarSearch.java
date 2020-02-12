package me.itslars.pathfinder.objects.pathfinding;

import java.util.*;

public class AStarSearch {

    private List<Edge> edges;
    private Set<Node> searched;
    private Queue<Node> unsearched;

    public AStarSearch(Graph graph) {
        edges = graph.getEdges();
    }

    /*
     * Finds and prints shortest path from start to end using A* search
     */
    public Node run(Node start, Node end) {
        //Initialize empty set and empty PriorityQueue
        searched = new HashSet<>();
        unsearched = new PriorityQueue<>();

        //Set the current node to @param start
        Node current;
        //Set start node's heuristic values (g(x) and h(x))
        start.setDistance(0.0);
        start.setHeuristic(end);
        //Add @param start to the queue
        unsearched.add(start);

        while(!unsearched.isEmpty()) {
            //Pop the PriorityQueue and set current to the top element;
            current = unsearched.poll();

            //If the current node is our target, print the path and end
            if (current.equals(end)) {
                return current;
            }
            //Move current node to the searched list.
            searched.add(current);
            updateNeighbor(current, end);
        }
        //We did not find the shortest path.
        return null;
    }

    /*
     * @param curr node whose neighbors are to be checked/updated.
     * @param destination node which heuristics will be calculated from (AKA distance from @param destination)
     */
    public void updateNeighbor(Node curr, Node destination) {
        List<Node> neighbors = Graph.getNeighbors(edges, curr);

        //distance is the current node's distance to start
        Double distance = curr.getDistance();
        for (Node neighbor : neighbors) {
            //temp is the distance from current node to a neighbor
            Double temp = Graph.getDistanceFrom(edges, curr, neighbor);
            //If searched already contains neighbor, no need to double check. Continue in loop.
            if (!searched.contains(neighbor)) {
                if (distance + temp < neighbor.getDistance()) {
                    //Shorter path has been found. Update neighboring node.
                    neighbor.setPreviousNode(curr);
                    neighbor.setDistance(distance + temp);
                    neighbor.setHeuristic(destination);
                    //Allow neighbor to be searched through by adding it to the unsearched queue.
                    unsearched.add(neighbor);
                }
            }
        }
    }
}
