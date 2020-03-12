package me.itslars.pathfinder;

import com.google.gson.JsonObject;
import me.itslars.pathfinder.api.PathFinderApi;
import me.itslars.pathfinder.objects.pathfinding.Edge;
import me.itslars.pathfinder.objects.pathfinding.Node;
import me.itslars.pathfinder.util.PathFinder;
import me.itslars.pathfinder.util.SchedulerManager;
import me.itslars.pathfinder.util.Serializer;
import nl.dusdavidgames.minetopia.common.framework.database.objects.PreparedStatement;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
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
        getCommand("pathfinder").setExecutor(new Commands(this));
        Bukkit.getPluginManager().registerEvents(new Events(this), this);

        new PreparedStatement("CREATE TABLE `pathfinder_nodes` (" +
                "`uuid` VARCHAR(36) NOT NULL," +
                "`world` VARCHAR(50) NOT NULL," +
                "`x` DOUBLE NOT NULL DEFAULT 0," +
                "`y` DOUBLE NOT NULL DEFAULT 0," +
                "`z` DOUBLE NOT NULL DEFAULT 0," +
                "PRIMARY KEY (`uuid`)" +
                ")" +
                "COLLATE='utf8mb4_0900_ai_ci';")
                .executeUpdate();

        new PreparedStatement("CREATE TABLE `pathfinder_edges` (" +
                "`from_uuid` VARCHAR(36) NULL," +
                "`to_uuid` VARCHAR(36) NULL" +
                ")" +
                "COLLATE='utf8mb4_0900_ai_ci';")
                .executeUpdate();


        loadGraph();
        SchedulerManager.startEdgeIndicationScheduler(this);
    }

    @Override
    public void onDisable() {
        nodeMap.values().forEach(ArmorStand::remove);
    }

    private void loadGraph() {
        Bukkit.getLogger().info("[PathFinder] Initializing PathFinder graph...");
        long startMillis = System.currentTimeMillis();

        // First load all nodes. Then, load all edges
        new PreparedStatement("SELECT * FROM `minetopia`.`pathfinder_nodes`")
                .execute()
                .thenAccept(result -> {
                    result.asArray().forEach(jsonElement -> {
                        JsonObject object = (JsonObject) jsonElement;
                        if(Bukkit.getWorld("CHAXIA") != null) {
                            if(object.get("world").getAsString().equalsIgnoreCase("CHAXIA")) {
                                Node node = new Node(object.get("uuid").getAsString(),
                                        new Location(Bukkit.getWorld(object.get("world").getAsString()),
                                                object.get("x").getAsDouble(),
                                                object.get("y").getAsDouble(),
                                                object.get("z").getAsDouble()));
                                nodes.add(node);
                            }
                        } else if(Bukkit.getWorld("GRINDING") != null) {
                            if(object.get("world").getAsString().equalsIgnoreCase("GRINDING")) {
                                Node node = new Node(object.get("uuid").getAsString(),
                                        new Location(Bukkit.getWorld(object.get("world").getAsString()),
                                                object.get("x").getAsDouble(),
                                                object.get("y").getAsDouble(),
                                                object.get("z").getAsDouble()));
                                nodes.add(node);
                            }
                        }  else if(Bukkit.getWorld("ARCHIEF") != null) {
                            if(object.get("world").getAsString().equalsIgnoreCase("ARCHIEF")) {
                                Node node = new Node(object.get("uuid").getAsString(),
                                        new Location(Bukkit.getWorld(object.get("world").getAsString()),
                                                object.get("x").getAsDouble(),
                                                object.get("y").getAsDouble(),
                                                object.get("z").getAsDouble()));
                                nodes.add(node);
                            }
                        }
                    });

                    // Load all edges
                    new PreparedStatement("SELECT * FROM `minetopia`.`pathfinder_edges`")
                            .execute()
                            .thenAccept(result2 -> {
                                result2.asArray().forEach(jsonElement -> {
                                    JsonObject object = (JsonObject) jsonElement;

                                    Optional<Node> fromNode = nodes.stream()
                                            .filter(node -> node.getNodeID().equals(object.get("from_uuid").getAsString()))
                                            .findAny();

                                    Optional<Node> toNode = nodes.stream()
                                            .filter(node -> node.getNodeID().equals(object.get("to_uuid").getAsString()))
                                            .findAny();

                                    if (fromNode.isPresent() && toNode.isPresent()) {
                                        Edge edge = new Edge(fromNode.get(), toNode.get());
                                        edges.add(edge);
                                    }
                                });


                                long difference = System.currentTimeMillis() - startMillis;
                                Bukkit.getLogger().info("[PathFinder] Finished initializing PathFinder graph. Took " + difference + "ms");
                            });
                });
    }

    public static Main getInstance() {
        return main;
    }
}
