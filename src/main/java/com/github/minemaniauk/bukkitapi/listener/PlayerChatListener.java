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

package com.github.minemaniauk.bukkitapi.listener;

import com.github.kerbity.kerb.result.CompletableResultSet;
import com.github.minemaniauk.api.MineManiaAPI;
import com.github.minemaniauk.api.format.ChatFormat;
import com.github.minemaniauk.api.kerb.event.player.PlayerChatEvent;
import com.github.minemaniauk.api.kerb.event.player.PlayerPostChatEvent;
import com.github.minemaniauk.bukkitapi.BukkitAdapter;
import com.github.minemaniauk.bukkitapi.MineManiaAPI_Bukkit;
import com.github.minemaniauk.bukkitapi.dependency.PlaceholderAPIDependency;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the player chat listener.
 * Used to listen for player messages and send them to kerb.
 */
public class PlayerChatListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.getMessage().matches("^[0-9]$")) {
            event.setCancelled(true);
            return;
        }
        // Stop players from seeing the message.
        try {
            event.getRecipients().clear();
        } catch (UnsupportedOperationException ignored) {
            event.setCancelled(true);
        }

        // Call the post-chat event.
        CompletableResultSet<PlayerPostChatEvent> completableResultSet = MineManiaAPI_Bukkit
                .getInstance()
                .getAPI()
                .callEvent(new PlayerPostChatEvent(
                        BukkitAdapter.getUser(event.getPlayer()),
                        event.getMessage()
                ));

        // Wait for the final result set.
        List<PlayerPostChatEvent> resultSet = completableResultSet.waitForFinalResult();

        // Check if the event was cancelled.
        if (completableResultSet.containsCancelled()) return;

        // The final chat format.
        ChatFormat chatFormat = new ChatFormat();
        List<String> serverWhiteList = new ArrayList<>();

        for (PlayerPostChatEvent postChatEvent : resultSet) {
            if (postChatEvent == null) continue;
            chatFormat.combine(postChatEvent.getChatFormat());
            serverWhiteList.addAll(postChatEvent.getServerWhitelist());
        }

        // Broadcast the final chat event and not expect results.
        MineManiaAPI_Bukkit.getInstance().getAPI().callEvent(new PlayerChatEvent(
                BukkitAdapter.getUser(event.getPlayer()),
                PlaceholderAPIDependency.getInstance().parse(
                        chatFormat.parse(event.getMessage()),
                        event.getPlayer()
                ),
                serverWhiteList
        ));
    }
}
