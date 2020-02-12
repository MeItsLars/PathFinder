package me.itslars.pathfinder.api.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerPathFindingFailedEvent extends Event {
    private Player player;
    private FailReason reason;
    private Location startLocation;
    private Location endLocation;
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public PlayerPathFindingFailedEvent(Player player, FailReason reason, Location startLocation, Location endLocation) {
        this.player = player;
        this.reason = reason;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
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

    public FailReason getFailReason() {
        return reason;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public Location getEndLocation() {
        return endLocation;
    }
}
