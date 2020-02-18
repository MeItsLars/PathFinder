package me.itslars.pathfinder;

import me.itslars.pathfinder.api.PathFinderApi;
import me.itslars.pathfinder.objects.pathfinding.Node;
import me.itslars.pathfinder.util.PathFinder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Commands implements CommandExecutor {

    private Main main;

    public Commands(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(!(sender instanceof Player)) {
            sender.sendMessage("§cYou have to be a player to execute this command.");
            return false;
        }

        Player player = (Player) sender;

        if(!player.hasPermission("pathfinder.*")) {
            player.sendMessage("§cInsufficient permissions.");
            return false;
        }

        if(args.length > 0) {
            if (args[0].equals("search")) {
                if(args.length != 4) {
                    player.sendMessage("§cInvalid arguments. Use /pathfinder search [destX] [destY] [destZ]");
                    return false;
                }

                double x = 0, y = 0, z = 0;

                try {
                    x = Double.parseDouble(args[1]);
                    y = Double.parseDouble(args[2]);
                    z = Double.parseDouble(args[3]);
                } catch(Exception e) {
                    player.sendMessage("Invalid double values.");
                }

                Location destination = new Location(player.getWorld(), x, y, z);

                PathFinder.navigatePath(player, player.getLocation(), destination);
            } else if(args[0].equals("tools")) {
                player.getInventory().addItem(
                        generateHoe("§r§eNode creator", "§rRight click this to", "§rcreate a node at", "§ryour current location", "", "§rLeft click to remove node"),
                        generateHoe("§r§eEdge creator", "§rRight click this to", "§rcreate a connection", "§rbetween two nodes", "", "§rLeft click to remove node"));
            } else if(args[0].equals("edit")) {
                if(args.length == 2) {
                    if(args[1].equalsIgnoreCase("on")) {
                        main.nodeMap.values().forEach(ArmorStand::remove);
                        main.nodeMap.clear();

                        for(Node node : main.nodes) {
                            node.getLocation().getChunk().load();
                            ArmorStand nodeStand = (ArmorStand) node.getLocation().getWorld().spawnEntity(node.getLocation(), EntityType.ARMOR_STAND);
                            nodeStand.setGravity(false);
                            nodeStand.setInvulnerable(true);
                            nodeStand.setCustomNameVisible(true);
                            nodeStand.setCustomName("§ePathFinder §f- Node §7#" + node.getNodeID());
                            main.nodeMap.put(node.getNodeID(), nodeStand);
                        }
                    } else if(args[1].equalsIgnoreCase("off")) {
                        main.nodeMap.values().forEach(ArmorStand::remove);
                        main.nodeMap.clear();
                    }
                } else player.sendMessage("§cUsage: §f/pathfinder edit [on/off]");
            }
        } else {
            player.sendMessage("§ePathFinder:");
            player.sendMessage("§f/pathfinder search [x] [y] [z] §7- Searches route");
            player.sendMessage("§f/pathfinder tools §7- Get creation tools");
            player.sendMessage("§f/pathfinder edit [on/off]§7- Toggle pathfinder editing mode");
        }

        return true;
    }

    private ItemStack generateHoe(String displayName, String... lore) {
        ItemStack is = new ItemStack(Material.IRON_HOE);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(displayName);
        im.setLore(Stream.of(lore).collect(Collectors.toList()));
        is.setItemMeta(im);
        return is;
    }
}
