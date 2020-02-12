package me.itslars.pathfinder.api;

import me.itslars.pathfinder.util.PathManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PathFinderApi {

    public static boolean hasActiveRoute(Player player) {
        return PathManager.activePlayerRoute.containsKey(player);
    }

    public static double getRemainingRouteDistance(Player player) {
        if (!hasActiveRoute(player)) return -1;

        Location previous = null;
        double result = 0;

        for (Location location : PathManager.activePlayerRoute.get(player)) {
            if (previous != null) result += previous.distance(location);
            previous = location;
        }

        return result;
    }
}
