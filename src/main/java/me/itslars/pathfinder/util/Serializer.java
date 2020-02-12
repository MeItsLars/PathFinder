package me.itslars.pathfinder.util;

import me.itslars.pathfinder.objects.pathfinding.Node;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Serializer {
    public static String nodeToString(Node node) {
        Location location = node.getLocation();
        String value = node.getNodeID() + "#" + location.getWorld().getName() + "#" + location.getX() + "#" + location.getY() + "#" + location.getZ();
        return value.replaceAll("\\.", "\\~");
    }

    public static Node stringToNode(String nodeString) {
        String[] parts = nodeString.replaceAll("\\~", "\\.").split("#");

        return new Node(parts[0], new Location(Bukkit.getWorld(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]),
                Double.parseDouble(parts[4])));
    }
}
