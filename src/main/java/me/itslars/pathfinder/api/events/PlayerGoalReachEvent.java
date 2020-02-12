package me.itslars.pathfinder.api.events;

import me.itslars.pathfinder.objects.pathfinding.Node;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class PlayerGoalReachEvent extends Event {
    private Player player;
    private List<Node> nodes;
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public PlayerGoalReachEvent(Player player, List<Node> nodes) {
        this.player = player;
        this.nodes = nodes;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Node> getNodes() {
        return nodes;
    }
}
