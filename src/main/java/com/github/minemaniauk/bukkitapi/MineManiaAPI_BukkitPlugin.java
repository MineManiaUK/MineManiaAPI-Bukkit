/*
 * MineManiaAPI
 * Used for interacting with the database and message broker.
 *
 * Copyright (C) 2023  MineManiaUK Staff
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.minemaniauk.bukkitapi;

import com.github.cozyplugins.cozylibrary.CozyPlugin;
import com.github.cozyplugins.cozylibrary.command.CommandManager;
import com.github.cozyplugins.cozylibrary.command.command.command.ProgrammableCommand;
import com.github.cozyplugins.cozylibrary.command.command.command.programmable.ProgrammableExecutor;
import com.github.cozyplugins.cozylibrary.command.datatype.CommandArguments;
import com.github.cozyplugins.cozylibrary.command.datatype.CommandStatus;
import com.github.cozyplugins.cozylibrary.placeholder.PlaceholderManager;
import com.github.cozyplugins.cozylibrary.user.ConsoleUser;
import com.github.cozyplugins.cozylibrary.user.PlayerUser;
import com.github.cozyplugins.cozylibrary.user.User;
import com.github.minemaniauk.api.MineManiaAPI;
import com.github.minemaniauk.api.MineManiaAPIContract;
import com.github.minemaniauk.api.MineManiaLocation;
import com.github.minemaniauk.api.database.collection.UserCollection;
import com.github.minemaniauk.api.database.record.UserRecord;
import com.github.minemaniauk.api.kerb.event.player.PlayerChatEvent;
import com.github.minemaniauk.api.kerb.event.useraction.*;
import com.github.minemaniauk.api.user.MineManiaUser;
import com.github.minemaniauk.bukkitapi.inventory.InviteListInventory;
import com.github.minemaniauk.bukkitapi.inventory.MenuInventory;
import com.github.minemaniauk.bukkitapi.listener.PlayerChatListener;
import com.github.smuddgge.squishyconfiguration.ConfigurationFactory;
import com.github.smuddgge.squishyconfiguration.console.Console;
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import com.github.smuddgge.squishydatabase.Query;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents the instance of the
 * mine mania api for bukkit.
 */
public final class MineManiaAPI_BukkitPlugin extends CozyPlugin implements MineManiaAPIContract, Listener {

    private static @NotNull MineManiaAPI_BukkitPlugin instance;
    private @NotNull Configuration configuration;
    private @NotNull MineManiaAPI api;
    private @NotNull Map<UUID, MineManiaLocation> teleportMap;

    public MineManiaAPI_BukkitPlugin(@NotNull JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean isCommandTypesEnabled() {
        return false;
    }

    @Override
    public void onEnable() {

        // Set the instance.T
        MineManiaAPI_BukkitPlugin.instance = this;

        // Set up the configuration file.
        this.configuration = ConfigurationFactory.YAML
                .create(this.getPlugin().getDataFolder(), "config")
                .setDefaultPath("config.yml");
        this.configuration.load();

        // Set up the api.
        this.api = MineManiaAPI.createAndSet(this.configuration, this);

        // Set up the teleport list.
        this.teleportMap = new HashMap<>();

        // Register events.
        this.getPlugin().getServer().getPluginManager().registerEvents(new PlayerChatListener(), this.getPlugin());
        this.getPlugin().getServer().getPluginManager().registerEvents(this, this.getPlugin());
    }

    @Override
    protected void onDisable() {

    }

    @Override
    protected void onLoadCommands(@NotNull CommandManager commandManager) {

        commandManager.addCommand(new ProgrammableCommand("menu")
                .setDescription("Used to open the main menu.")
                .setSyntax("/menu")
                .setPlayer(new ProgrammableExecutor<>() {
                    @Override
                    public @Nullable CommandStatus onUser(@NotNull PlayerUser user, @NotNull CommandArguments arguments) {
                        new MenuInventory().open(user.getPlayer());
                        return new CommandStatus();
                    }
                })
        );

        commandManager.addCommand(new ProgrammableCommand("invites")
                .setDescription("Used to open the game invites menu.")
                .setSyntax("/invites")
                .setPlayer(new ProgrammableExecutor<>() {
                    @Override
                    public @Nullable CommandStatus onUser(@NotNull PlayerUser user, @NotNull CommandArguments arguments) {
                        new InviteListInventory().open(user.getPlayer());
                        return new CommandStatus();
                    }
                })
        );
    }

    @Override
    protected void onLoadPlaceholders(@NotNull PlaceholderManager placeholderManager) {

    }

    @Override
    public @NotNull MineManiaUser getUser(@NotNull UUID uuid) {
        UserRecord record = this.getAPI().getDatabase()
                .getTable(UserCollection.class)
                .getFirstRecord(new Query().match("mc_uuid", uuid.toString()));

        if (record == null) {
            return new MineManiaUser(uuid, Bukkit.getOfflinePlayer(uuid).getName());
        }

        return new MineManiaUser(uuid, record.getMinecraftName());
    }

    @Override
    public @NotNull MineManiaUser getUser(@NotNull String name) {
        UserRecord record = this.getAPI().getDatabase()
                .getTable(UserCollection.class)
                .getFirstRecord(new Query().match("mc_name", name));


        if (record == null) {
            return new MineManiaUser(Bukkit.getOfflinePlayer(name).getUniqueId(), name);
        }

        return new MineManiaUser(record.getMinecraftUuid(), name);
    }

    @Override
    public @Nullable UserActionHasPermissionListEvent onHasPermission(@NotNull UserActionHasPermissionListEvent event) {
        Optional<Player> optionalPlayer = BukkitAdapter.getPlayer(event.getUser());

        // Return with the correct permission checks unless the player is null.
        return optionalPlayer.map(player -> event.setResult(player::hasPermission)).orElse(null);
    }

    @Override
    public @Nullable UserActionIsOnlineEvent onIsOnline(@NotNull UserActionIsOnlineEvent event) {
        return (UserActionIsOnlineEvent) event.set(BukkitAdapter.getPlayer(event.getUser()).isPresent());
    }

    @Override
    public @Nullable UserActionIsVanishedEvent onIsVanished(@NotNull UserActionIsVanishedEvent event) {
        Optional<Player> optionalPlayer = BukkitAdapter.getPlayer(event.getUser());
        return optionalPlayer.map(
                player -> (UserActionIsVanishedEvent) event.set(this.isVanished(player))
        ).orElse(null);
    }

    @Override
    public @Nullable UserActionMessageEvent onMessage(@NotNull UserActionMessageEvent event) {
        Optional<Player> optionalPlayer = BukkitAdapter.getPlayer(event.getUser());
        if (optionalPlayer.isEmpty()) return null;

        // Send the message to the user.
        User user = new PlayerUser(optionalPlayer.get());
        user.sendMessage(event.getMessage());

        // Complete the event.
        return (UserActionMessageEvent) event.complete();
    }

    @Override
    public @Nullable UserActionTeleportEvent onTeleport(@NotNull UserActionTeleportEvent event) {

        // Check if the user is teleporting to this server.
        if (!event.getLocation().getServerName().equalsIgnoreCase(this.getAPI().getServerName())) return null;

        // Check if the specific world location doesnt matter.
        if (event.getLocation().getWorldName().equalsIgnoreCase("null")) {
            return (UserActionTeleportEvent) event.setComplete(true);
        }

        // Attempt to get the user as an online player.
        Optional<Player> optionalPlayer = this.getPlayer(event.getUser());

        // Check if the player is already online.
        if (optionalPlayer.isPresent()) {
            optionalPlayer.get().teleport(event.getLocation().getLocation(new BukkitLocationConverter()));
            return (UserActionTeleportEvent) event.setComplete(true);
        }

        // Otherwise, add to a teleport list.
        this.teleportMap.put(event.getUser().getUniqueId(), event.getLocation());
        return (UserActionTeleportEvent) event.setComplete(true);
    }

    @Override
    public @NotNull PlayerChatEvent onChatEvent(@NotNull PlayerChatEvent event) {

        Console.log("Received chat event.");

        // Check if this server should not send the message.
        if (!event.getServerWhiteList().contains(this.getClientName())) {
            Console.log("This server was not specified to be part of the chat channel.");
            return event;
        }

        Console.log("Sending the message to all players.");
        Console.log("Message: " + event.getFormattedMessage());

        // Send all the players online the message.
        for (Player player : Bukkit.getOnlinePlayers()) {
            new PlayerUser(player).sendMessage(event.getFormattedMessage());
        }

        new ConsoleUser().sendMessage(event.getFormattedMessage());

        return (PlayerChatEvent) event.complete();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        // Check if they are in the database.
        UserRecord record = this.getAPI().getDatabase()
                .getTable(UserCollection.class)
                .getUserRecord(event.getPlayer().getUniqueId())
                .orElse(null);

        if (record == null) {
            UserRecord userRecord = new UserRecord();
            userRecord.mc_name = event.getPlayer().getName();
            userRecord.mc_uuid = event.getPlayer().getUniqueId().toString();

            this.getAPI().getDatabase()
                    .getTable(UserCollection.class)
                    .insertRecord(userRecord);
        } else {
            record.mc_name = event.getPlayer().getName();
            this.getAPI().getDatabase()
                    .getTable(UserCollection.class)
                    .insertRecord(record);
        }

        // Check if they are in the teleport map.
        if (!teleportMap.containsKey(event.getPlayer().getUniqueId())) return;

        // Get the instance of the location to teleport to.
        MineManiaLocation location = this.teleportMap.get(event.getPlayer().getUniqueId());

        // Teleport the player.
        PlayerUser user = new PlayerUser(event.getPlayer());
        user.forceTeleport(location.getLocation(new BukkitLocationConverter()));
    }

    /**
     * Used to get the configuration file instance.
     *
     * @return The configuration file instance.
     */
    public @NotNull Configuration getConfiguration() {
        return this.configuration;
    }

    /**
     * Used to get this client name used in
     * kerb events.
     *
     * @return This client name.
     */
    public @NotNull String getClientName() {
        return this.api.getKerbClient().getName();
    }

    /**
     * Used to get the instance of the api connection.
     *
     * @return The instance of the api connection.
     */
    public @NotNull MineManiaAPI getAPI() {
        return this.api;
    }

    /**
     * Used to check if a player is vanished.
     *
     * @param player The instance of a player.
     * @return True if they are vanished.
     */
    public boolean isVanished(@NotNull Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }

    /**
     * Used to get the instance of the online player
     * from a mine mania user.
     *
     * @param user The instance of the user.
     * @return The optional player instance.
     */
    public @NotNull Optional<Player> getPlayer(@NotNull MineManiaUser user) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getUniqueId().equals(user.getUniqueId())) return Optional.of(player);
        }
        return Optional.empty();
    }

    /**
     * Used to get a player's paws.
     *
     * @param playerUuid The player's uuid.
     * @return The number of paws they have.
     */
    public long getPaws(@NotNull UUID playerUuid) {
        return this.getAPI().getDatabase()
                .getTable(UserCollection.class)
                .getUserRecord(playerUuid)
                .orElse(new UserRecord())
                .getPaws();
    }

    /**
     * Used to get the instance of the bukkit api.
     * This can be called from any other plugin.
     *
     * @return The instance of the bukkit api.
     */
    public static @NotNull MineManiaAPI_BukkitPlugin getInstance() {
        return MineManiaAPI_BukkitPlugin.instance;
    }
}
