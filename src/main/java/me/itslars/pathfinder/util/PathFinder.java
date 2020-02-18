package me.itslars.pathfinder.util;

import me.itslars.pathfinder.Main;
import me.itslars.pathfinder.api.events.FailReason;
import me.itslars.pathfinder.api.events.PlayerPathFindingFailedEvent;
import me.itslars.pathfinder.objects.pathfinding.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PathFinder {

    // Map that contains the active player path runnable
    private static Map<Player, BukkitTask> activePlayerRunnable = new HashMap<>();
    private static Map<Integer, BukkitTask> secondActiveRunnable = new HashMap<>();

    @Deprecated
    public static void navigatePath(Player player, Location startLocation, Location endLocation) {
        navigatePath(player, startLocation, endLocation, true);
    }

    /**
     * This method starts a navigation between two points.
     * startLocation end endLocation are connected to the two nodes that are closest.
     *
     * @param player the player that is navigating
     * @param startLocation the location at which the navigation begins
     * @param endLocation the location at which the navigation ends
     */
    public static void navigatePath(Player player, Location startLocation, Location endLocation, boolean toCoordinates) {
        final Main main = Main.getInstance();
        cancelPlayerActivePath(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                Node startNode = getNodeClosestTo(startLocation);
                if(startNode == null) {
                    PlayerPathFindingFailedEvent event = new PlayerPathFindingFailedEvent(player, FailReason.START_NODE_NOT_FOUND, startLocation, endLocation);
                    Bukkit.getPluginManager().callEvent(event);
                    return;
                }

                Node endNode = getNodeClosestTo(endLocation);
                if(endNode == null) {
                    PlayerPathFindingFailedEvent event = new PlayerPathFindingFailedEvent(player, FailReason.END_NODE_NOT_FOUND, startLocation, endLocation);
                    Bukkit.getPluginManager().callEvent(event);
                    return;
                }

                if(startNode.equals(endNode)) {
                    PlayerPathFindingFailedEvent event = new PlayerPathFindingFailedEvent(player, FailReason.PLAYER_ALREADY_AT_GOAL, startLocation, endLocation);
                    Bukkit.getPluginManager().callEvent(event);
                    return;
                }

                List<Node> nodes = main.nodes.stream().map(Node::clone).collect(Collectors.toList());
                List<Edge> edges = main.edges.stream().map(Edge::clone).collect(Collectors.toList());

                Graph graph = new Graph(nodes, edges);
                AStarSearch search = new AStarSearch(graph);
                Node result = search.run(startNode, endNode);

                if(result == null) {
                    PlayerPathFindingFailedEvent event = new PlayerPathFindingFailedEvent(player, FailReason.ROUTE_NOT_FOUND, startLocation, endLocation);
                    Bukkit.getPluginManager().callEvent(event);
                    return;
                }

                List<Location> path = new ArrayList<>();

                if (toCoordinates) path.add(endLocation);

                while(result != null) {
                    path.add(result.getLocation());
                    result = result.getPreviousNode();
                }

                path.add(player.getLocation());

                BukkitTask task = PathManager.showPath(main, player, nodes, path);
                activePlayerRunnable.put(player, task);

                BukkitTask bukkitTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!secondActiveRunnable.get(getTaskId()).isCancelled()) {
                            navigatePath(player, player.getLocation(), endLocation);
                            secondActiveRunnable.remove(getTaskId());
                        }
                    }
                }.runTaskLaterAsynchronously(main, 10 * 20L);
                secondActiveRunnable.put(bukkitTask.getTaskId(), task);
            }
        }.runTaskAsynchronously(main);
    }

    public static void cancelPlayerActivePath(Player player) {
        if(activePlayerRunnable.containsKey(player)) {
            BukkitTask task = activePlayerRunnable.get(player);
            task.cancel();
            PathManager.activePlayerRoute.remove(player);
        }
    }

    /**
     * Method for getting the node closest to the given location
     * @param location the location of which the closest node is being found
     * @return closest node
     */
    private static Node getNodeClosestTo(Location location) {
        Node closestNode = null;
        for(Node node : Main.getInstance().nodes) {
            if(closestNode == null) closestNode = node;
            else if(closestNode.getLocation().distance(location) > node.getLocation().distance(location)) closestNode = node;
        }
        return closestNode;
    }
}
