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
import com.github.cozyplugins.cozylibrary.user.ConsoleUser;
import com.github.cozyplugins.cozylibrary.user.PlayerUser;
import com.github.cozyplugins.cozylibrary.user.User;
import com.github.minemaniauk.api.MineManiaAPI;
import com.github.minemaniauk.api.MineManiaAPIContract;
import com.github.minemaniauk.api.kerb.event.player.PlayerChatEvent;
import com.github.minemaniauk.api.kerb.event.useraction.UserActionHasPermissionListEvent;
import com.github.minemaniauk.api.kerb.event.useraction.UserActionIsOnlineEvent;
import com.github.minemaniauk.api.kerb.event.useraction.UserActionIsVanishedEvent;
import com.github.minemaniauk.api.kerb.event.useraction.UserActionMessageEvent;
import com.github.minemaniauk.api.user.MineManiaUser;
import com.github.minemaniauk.bukkitapi.listener.PlayerChatListener;
import com.github.smuddgge.squishyconfiguration.ConfigurationFactory;
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents the instance of the
 * mine mania api for bukkit.
 */
public final class MineManiaAPI_Bukkit extends CozyPlugin implements MineManiaAPIContract {

    private static @NotNull MineManiaAPI_Bukkit instance;
    private @NotNull Configuration configuration;
    private @NotNull MineManiaAPI api;

    @Override
    public boolean enableCommandDirectory() {
        return false;
    }

    @Override
    public void onCozyEnable() {

        // Set the instance.T
        MineManiaAPI_Bukkit.instance = this;

        // Set up the configuration file.
        this.configuration = ConfigurationFactory.YAML
                .create(this.getDataFolder(), "config")
                .setDefaultPath("config.yml");
        this.configuration.load();

        // Set up the api.
        this.api = MineManiaAPI.createAndSet(this.configuration, this);

        // Register events.
        this.getServer().getPluginManager().registerEvents(new PlayerChatListener(), this);
    }

    @Override
    public @NotNull MineManiaUser getUser(@NotNull UUID uuid) {
        return new MineManiaUser(uuid, Bukkit.getPlayer(uuid).getName());
    }

    @Override
    public @NotNull MineManiaUser getUser(@NotNull String name) {
        return new MineManiaUser(Bukkit.getPlayer(name).getUniqueId(), name);
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
    public @NotNull PlayerChatEvent onChatEvent(@NotNull PlayerChatEvent event) {

        // Check if this server should not send the message.
        if (!event.getServerWhiteList().contains(this.getClientName())) return event;


        // Send all the players online the message.
        for (Player player : Bukkit.getOnlinePlayers()) {
            new PlayerUser(player).sendMessage(event.getFormattedMessage());
        }

        new ConsoleUser().sendMessage(event.getFormattedMessage());

        return (PlayerChatEvent) event.complete();
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
     * Used to get the instance of the bukkit api.
     * This can be called from any other plugin.
     *
     * @return The instance of the bukkit api.
     */
    public static @NotNull MineManiaAPI_Bukkit getInstance() {
        return MineManiaAPI_Bukkit.instance;
    }
}
