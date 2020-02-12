package me.itslars.pathfinder;

import me.itslars.pathfinder.api.PathFinderApi;
import me.itslars.pathfinder.objects.pathfinding.Edge;
import me.itslars.pathfinder.objects.pathfinding.Node;
import me.itslars.pathfinder.util.PathFinder;
import me.itslars.pathfinder.util.SchedulerManager;
import me.itslars.pathfinder.util.Serializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Main extends JavaPlugin {

    private static Main main;

    // Global graph data
    public List<Node> nodes = new ArrayList<>();
    public List<Edge> edges = new ArrayList<>();

    // Graph editing data
    public Map<String, ArmorStand> nodeMap = new HashMap<>();
    public Map<Player, String> playerSelectedStartNode = new HashMap<>();

    public Main() {
        main = this;
    }

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        getCommand("pathfinder").setExecutor(new Commands(this));
        Bukkit.getPluginManager().registerEvents(new Events(this), this);

        loadGraph();
        SchedulerManager.startEdgeIndicationScheduler(this);
    }

    @Override
    public void onDisable() {
        nodeMap.values().forEach(ArmorStand::remove);
        saveGraph();
    }

    private void loadGraph() {
        Bukkit.getLogger().info("[PathFinder] Initializing PathFinder graph...");
        long startMillis = System.currentTimeMillis();

        Map<String, Node> nodesMap = new HashMap<>();

        List<String> nodesList = getConfig().getStringList("nodes");
        if(nodes != null) {
            nodesList.forEach(ns -> {
                Node node = Serializer.stringToNode(ns);
                nodes.add(node);
                nodesMap.put(node.getNodeID(), node);
            });
        }

        List<String> edgesList = getConfig().getStringList("edges");
        if(edges != null) {
            edgesList.forEach(es -> {
                String[] parts = es.split("#");
                Node startNode = nodesMap.get(parts[0]);
                Node endNode = nodesMap.get(parts[1]);
                if(startNode != null && endNode != null) {
                    edges.add(new Edge(startNode, endNode));
                }
            });
        }

        long difference = System.currentTimeMillis() - startMillis;
        Bukkit.getLogger().info("[PathFinder] Finished initializing PathFinder graph. Took " + difference + "ms");
    }

    private void saveGraph() {
        getConfig().set("nodes", null);
        getConfig().set("edges", null);
        saveConfig();

        List<String> nodeList = nodes.stream().map(Serializer::nodeToString).collect(Collectors.toList());
        getConfig().set("nodes", nodeList);
        saveConfig();

        List<String> edgeList = edges.stream().map(e -> e.getStart().getNodeID() + "#" + e.getEnd().getNodeID()).collect(Collectors.toList());
        getConfig().set("edges", edgeList);
        saveConfig();
    }

    public static Main getInstance() {
        return main;
    }
}
