package me.itslars.pathfinder.util;

import me.itslars.pathfinder.Main;
import me.itslars.pathfinder.objects.InterpolatedPath;
import me.itslars.pathfinder.api.events.PlayerGoalReachEvent;
import me.itslars.pathfinder.objects.pathfinding.Node;
import net.minecraft.server.v1_12_R1.EnumParticle;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class PathManager {

    public static Map<Player, List<Location>> activePlayerRoute = new HashMap<>();

    private static final double pathShowRefreshRate = 120.0;

    // NON-INTERPOLATED PATH VERSION:
    public static BukkitTask showPath(Main main, Player player, List<Node> nodes, List<Location> pathLocations) {
        final List<Location> particleLocations = new ArrayList<>();

        if (pathLocations.size() > 0) {
            Location current = pathLocations.get(0);

            for (int i = 1; i < pathLocations.size(); i++) {
                Location node = pathLocations.get(i);
                particleLocations.addAll(drawLine(current, node, 1));
                current = node;
            }
        }

        Collections.reverse(particleLocations);

        particleLocations.forEach(loc -> spawnParticlePacket(player, loc, false));

        return new BukkitRunnable() {
            int progress = 0;
            double previous = 0;
            double delta = particleLocations.size() / pathShowRefreshRate;

            @Override
            public void run() {
                if(!player.isOnline()) {
                    cancel();
                    return;
                }

                // Code for managing player path progress
                int subIndex = 0;
                boolean remove = false;
                for(int i = particleLocations.size() - 1; i >= 0; i--) {
                    Location location = particleLocations.get(i);
                    if(location.distance(player.getLocation()) < 3) {
                        subIndex = i;
                        remove = true;
                        break;
                    }
                }

                if(remove) particleLocations.subList(0, subIndex + 1).clear();
                activePlayerRoute.put(player, particleLocations);

                // Check if player has reached goal
                if(particleLocations.isEmpty()) {
                    cancel();

                    // Firing event for reaching goal
                    PlayerGoalReachEvent event = new PlayerGoalReachEvent(player, nodes);
                    Bukkit.getPluginManager().callEvent(event);
                    activePlayerRoute.remove(player);
                }

                // Code for displaying the path
                for(int i = (int) previous; i < previous + delta && i < particleLocations.size(); i++) {
                    boolean finish = i == particleLocations.size() - 1;
                    spawnParticlePacket(player, particleLocations.get(i), finish);
                }

                // Code for managing path display progress
                progress++;
                previous += delta;
                if(progress >= 40) {
                    progress = 0;
                    previous = 0;
                }
            }
        }.runTaskTimerAsynchronously(main, 0L, 1L);
    }

    // INTERPOLATED PATH VERSION:
    /* public static BukkitTask showPath(Main main, Player player, List<Node> nodes, List<Location> pathLocations) {
        final InterpolatedPath interpolatedPath = new InterpolatedPath(new Location(player.getLocation().getWorld(), 0, 1.5, 0).toVector(),
                pathLocations.stream().map(Location::toVector).collect(Collectors.toList()));

        final List<Location> particleLocations = new ArrayList<>();
        for (double i = 0; i < interpolatedPath.getPathLenght(); i++) {
            particleLocations.add(interpolatedPath.getPathPosition(i).toLocation(player.getWorld()));
        }

        Collections.reverse(particleLocations);

        return new BukkitRunnable() {
            int progress = 0;
            double previous = 0;
            double delta = particleLocations.size() / pathShowRefreshRate;

            @Override
            public void run() {
                if(!player.isOnline()) {
                    cancel();
                    return;
                }

                // Code for managing player path progress
                int subIndex = 0;
                boolean remove = false;
                for(int i = particleLocations.size() - 1; i >= 0; i--) {
                    Location location = particleLocations.get(i);
                    if(location.distance(player.getLocation()) < 3) {
                        subIndex = i;
                        remove = true;
                        break;
                    }
                }

                if(remove) particleLocations.subList(0, subIndex + 1).clear();
                activePlayerRoute.put(player, particleLocations);

                // Check if player has reached goal
                if(particleLocations.isEmpty()) {
                    cancel();

                    // Firing event for reaching goal
                    PlayerGoalReachEvent event = new PlayerGoalReachEvent(player, nodes);
                    Bukkit.getPluginManager().callEvent(event);
                    activePlayerRoute.remove(player);
                }

                // Code for displaying the path
                for(int i = (int) previous; i < previous + delta && i < particleLocations.size(); i++) {
                    boolean finish = i == particleLocations.size() - 1;
                    spawnParticlePacket(player, particleLocations.get(i), finish);
                }

                // Code for managing path display progress
                progress++;
                previous += delta;
                if(progress >= 40) {
                    progress = 0;
                    previous = 0;
                }
            }
        }.runTaskTimerAsynchronously(main, 0L, 1L);
    }*/

    private static void spawnParticlePacket(Player player, Location location, boolean finish) {
        PacketPlayOutWorldParticles packet;

        if(finish) {
            packet = new PacketPlayOutWorldParticles(EnumParticle.VILLAGER_HAPPY,
                    true,
                    (float) location.getX(),
                    (float) location.getY(),
                    (float) location.getZ(),
                    0, 1, 0, 0, 10);
        } else {
            packet = new PacketPlayOutWorldParticles(EnumParticle.FLAME,
                    true,
                    (float) location.getX(),
                    (float) location.getY(),
                    (float) location.getZ(),
                    0, 0, 0, 0, 1);
        }

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    private static List<Location> drawLine(Location point1, Location point2, double space) {
        List<Location> result = new ArrayList<>();

        double distance = point1.distance(point2);
        org.bukkit.util.Vector p1 = point1.toVector();
        org.bukkit.util.Vector p2 = point2.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
        double length = 0;
        for (; length < distance; p1.add(vector)) {
            result.add(p1.clone().add(new Vector(0, 2, 0)).toLocation(point1.getWorld()));
            length += space;
        }

        return result;
    }
}
