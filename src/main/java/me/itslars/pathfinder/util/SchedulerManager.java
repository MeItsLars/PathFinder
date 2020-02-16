package me.itslars.pathfinder.util;

import me.itslars.pathfinder.Main;
import me.itslars.pathfinder.objects.pathfinding.Edge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SchedulerManager {
    public static void startEdgeIndicationScheduler(Main main) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if(!main.nodeMap.isEmpty()) {
                    for(Edge edge : main.edges) {
                        drawLine(edge.getStart().getLocation(), edge.getEnd().getLocation(), 0.5);
                    }
                }
            }
        }.runTaskTimer(main, 0L, 20L);
    }

    private static void drawLine(Location point1, Location point2, double space) {
        double distance = point1.distance(point2);
        Vector p1 = point1.toVector();
        Vector p2 = point2.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
        double length = 0;
        for (; length < distance; p1.add(vector)) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getLocation().distance(point1) < 100 || player.getLocation().distance(point2) < 100) {
                    player.spawnParticle(Particle.FLAME, p1.getX(), p1.getY() + 2, p1.getZ(), 1, 0, 0, 0, 0);
                }
            }
            length += space;
        }
    }
}
