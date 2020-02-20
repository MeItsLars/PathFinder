package me.itslars.pathfinder;

import me.itslars.pathfinder.objects.pathfinding.Edge;
import me.itslars.pathfinder.objects.pathfinding.Node;
import me.itslars.pathfinder.util.PathFinder;
import nl.dusdavidgames.minetopia.common.framework.database.objects.PreparedStatement;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;

@SuppressWarnings("Duplicates")
public class Events implements Listener {

    private Main main;

    public Events(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            ItemStack is = e.getItem();
            if(is != null) {
                ItemMeta im = is.getItemMeta();
                if(im != null) {
                    String name = im.getDisplayName();
                    if(name != null) {
                        Player player = e.getPlayer();

                        if(name.equals("§r§eNode creator")) {
                            Node node = new Node(UUID.randomUUID().toString(), player.getLocation());
                            main.nodes.add(node);

                            new PreparedStatement("INSERT INTO `minetopia`.`pathfinder_nodes` " +
                                    "(uuid, world, x, y, z) VALUES " +
                                    "(?, ?, ?, ?, ?);")
                                    .setString(node.getNodeID())
                                    .setString(node.getLocation().getWorld().getName())
                                    .setDouble(node.getLocation().getX())
                                    .setDouble(node.getLocation().getY())
                                    .setDouble(node.getLocation().getZ())
                                    .execute();


                            player.sendMessage("§aNew node created!");
                            if(main.nodeMap.isEmpty()) player.sendMessage("§7(Use '/pathfinder edit on' to display it)");
                            else {
                                ArmorStand nodeStand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
                                nodeStand.setGravity(false);
                                nodeStand.setInvulnerable(true);
                                nodeStand.setCustomName("§ePathFinder §f- Node §7#" + node.getNodeID());
                                nodeStand.setCustomNameVisible(true);

                                main.nodeMap.put(node.getNodeID(), nodeStand);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onStandClick(PlayerInteractAtEntityEvent e) {
        ItemStack is = e.getPlayer().getInventory().getItemInMainHand();
        if(is != null && e.getRightClicked() instanceof ArmorStand) {
            ItemMeta im = is.getItemMeta();
            if(im != null) {
                String name = im.getDisplayName();
                if(name != null) {
                    if(name.equals("§r§eEdge creator")) {
                        ArmorStand nodeStand = (ArmorStand) e.getRightClicked();
                        if(!nodeStand.getName().contains("PathFinder §f- ")) return;
                        Player player = e.getPlayer();

                        String nodeID = nodeStand.getCustomName().substring(nodeStand.getCustomName().indexOf('#') + 1);

                        Node clickedNode = null;

                        for(Node node : main.nodes) {
                            if(node.getNodeID().equals(nodeID)) {
                                clickedNode = node;
                                break;
                            }
                        }

                        if(clickedNode != null) {
                            if(player.isSneaking()) {
                                String startNodeString = main.playerSelectedStartNode.get(player);
                                if(startNodeString == null) {
                                    player.sendMessage("§cYou have not selected a start node! Use §fright click §con an armor stand to select one!");
                                    return;
                                }

                                Node startNode = null;

                                for(Node node : main.nodes) {
                                    if(node.getNodeID().equals(startNodeString)) {
                                        startNode = node;
                                        break;
                                    }
                                }

                                if(startNode == null) {
                                    player.sendMessage("§aYou have not selected a start node! Use §fright click §con an armor stand to select one!");
                                } else if(startNode.equals(clickedNode)) {
                                    player.sendMessage("§aYour edge start can't be equal to your edge end.");
                                } else {
                                    for(Edge edge : main.edges) {
                                        if((edge.getStart().equals(startNode) && edge.getEnd().equals(clickedNode)) ||
                                                (edge.getEnd().equals(startNode) && edge.getStart().equals(clickedNode))) {
                                            player.sendMessage("§cAn edge between these nodes already exists!");
                                            return;
                                        }
                                    }

                                    Edge edge = new Edge(startNode, clickedNode);
                                    main.edges.add(edge);
                                    player.sendMessage("§aEdge created!");
                                    main.playerSelectedStartNode.remove(player);


                                    new PreparedStatement("INSERT INTO `minetopia`.`pathfinder_edges` " +
                                            "(from_uuid, to_uuid) VALUES " +
                                            "(?, ?);")
                                            .setString(startNode.getNodeID())
                                            .setString(clickedNode.getNodeID())
                                            .execute();
                                }
                            } else {
                                main.playerSelectedStartNode.put(player, clickedNode.getNodeID());
                                player.sendMessage("§aSet edge start location. Use §fshift + right click §aon an armor stand to set the edge end location.");
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onStandRemove(EntityDamageByEntityEvent e) {
        if(e.getDamager() instanceof Player && e.getEntity() instanceof ArmorStand) {
            Player player = (Player) e.getDamager();
            ArmorStand nodeStand = (ArmorStand) e.getEntity();

            if(!nodeStand.getName().contains("PathFinder §f- ")) return;
            e.setCancelled(true);

            ItemStack is = player.getInventory().getItemInMainHand();
            if(is != null) {
                ItemMeta im = is.getItemMeta();
                if(im != null) {
                    String name = im.getDisplayName();
                    if(name != null) {
                        if(name.equals("§r§eNode creator") || name.equals("§r§eEdge creator")) {
                            String nodeID = nodeStand.getCustomName().substring(nodeStand.getCustomName().indexOf('#') + 1);

                            Node clickedNode = null;

                            for(Node node : main.nodes) {
                                if(node.getNodeID().equals(nodeID)) {
                                    clickedNode = node;
                                    break;
                                }
                            }

                            if(clickedNode != null) {
                                main.nodes.remove(clickedNode);

                                // Removing all edges
                                Iterator<Edge> edgeIterator = main.edges.iterator();
                                while(edgeIterator.hasNext()) {
                                    Edge edge = edgeIterator.next();
                                    if(edge.getStart().equals(clickedNode) || edge.getEnd().equals(clickedNode)) {
                                        edgeIterator.remove();
                                    }
                                }

                                new PreparedStatement("DELETE FROM `minetopia`.`pathfinder_edges` WHERE " +
                                        "from_uuid=? OR to_uuid=?")
                                        .setString(clickedNode.getNodeID())
                                        .setString(clickedNode.getNodeID())
                                        .execute();
                                new PreparedStatement("DELETE FROM `minetopia`.`pathfinder_nodes` WHERE " +
                                        "uuid=?")
                                        .setString(clickedNode.getNodeID())
                                        .execute();

                                nodeStand.remove();

                                player.sendMessage("§aNode and corresponding edges removed!");
                                return;
                            }
                        }
                    }
                }
            }
            // If not destroyed by hand
            player.sendMessage("§cPlease use a PathFinder tool to remove these armor stands.\n§7(/pathfinder tools)");
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        boolean hasArmorStand = Arrays.stream(e.getChunk().getEntities())
                .filter(entity -> entity instanceof ArmorStand)
                .map(entity -> (ArmorStand) entity)
                .anyMatch(Main.getInstance().nodeMap.values()::contains);

        if (hasArmorStand) e.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        main.playerSelectedStartNode.remove(player);
        PathFinder.cancelPlayerActivePath(player);
    }
}
