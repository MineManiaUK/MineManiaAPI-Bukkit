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

package com.github.minemaniauk.bukkitapi.inventory;

import com.github.cozyplugins.cozylibrary.inventory.CozyInventory;
import com.github.cozyplugins.cozylibrary.inventory.InventoryItem;
import com.github.cozyplugins.cozylibrary.inventory.action.action.ClickAction;
import com.github.cozyplugins.cozylibrary.user.PlayerUser;

import java.util.*;
import com.github.minemaniauk.api.MineManiaLocation;
import com.github.minemaniauk.api.user.MineManiaUser;
import com.github.minemaniauk.bukkitapi.DatabaseConnection;
import com.github.minemaniauk.bukkitapi.MenuSections;
import com.github.minemaniauk.bukkitapi.MineManiaAPI_BukkitPlugin;
import com.github.squishylib.configuration.ConfigurationSection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GamesMenuInventory extends CozyInventory {

    public GamesMenuInventory() { super(54, "&f₴₴₴₴₴₴₴₴⏅"); }

    @Override
    protected void onGenerate(PlayerUser playerUser) {

        // Bedwars button
        this.setTeleportItem(
                "bedwars",
                "&r&l&dBedwars",
                Material.PINK_STAINED_GLASS_PANE,
                List.of("&r&7Teleport to Bedwars"),
                List.of(0, 1, 9, 10),
                1
        );

        // Hide and Seek button
        this.setTeleportItem(
                "hideandseek",
                "&r&l&aHide and Seek",
                Material.PINK_STAINED_GLASS_PANE,
                List.of("&r&7Teleport to Hide and Seek"),
                List.of(2, 3, 11, 12),
                1
        );

        // Skywars button
        this.setTeleportItem(
                "skywars",
                "&r&l&dSkywars",
                Material.PINK_STAINED_GLASS_PANE,
                List.of("&r&7Teleport to Skywars"),
                List.of(4, 5, 13, 14),
                1
        );

        // Capture the flag button
        this.setTeleportItem(
                "capturetheflag",
                "&r&l&9Capture The Flag",
                Material.PINK_STAINED_GLASS_PANE,
                List.of("&r&7Teleport the Capture the Flag "),
                List.of(6, 7, 15, 16),
                1
        );

        MongoCollection<Document> col = DatabaseConnection.getMongoDatabase().getCollection("MenuServers");

// Pull menu.server.main  --> returns Map<String,Object>
        Map<String, Object> servers = MenuSections.loadMenuSectionMap(col, "server.games");
        if (servers.isEmpty()) return;

        for (Map.Entry<String, Object> entry : servers.entrySet()) {
            String key = entry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> s = (Map<String, Object>) entry.getValue();
            String name = (String) ChatColor.RESET.toString() + s.get("name");
            String matStr = (String) s.get("item");
            Material material = Material.matchMaterial(matStr);

            @SuppressWarnings("unchecked")
            List<String> lore = (s.get("lore") instanceof List)
                    ? ((List<?>) s.get("lore"))
                    .stream()
                    .map(line -> "&r" + String.valueOf(line))
                    .toList()
                    : java.util.Collections.emptyList();

            int pos = s.get("position") == null ? 0 : ((Number) s.get("position")).intValue();

            setTeleportItem(
                    key,
                    name,
                    material,
                    lore,
                    Collections.singletonList(pos),
                    0
            );
        }

    }


    /**
     * Used to set a teleport item into the inventory.
     *
     * @param serverName The name of the server.
     * @param name       The name of the item.
     * @param material   The material of the item.
     * @param lore       The lore of the item.
     * @param slots      The slots to place the item.
     * @param modelData  The model data of the item.
     */
    private void setTeleportItem(@NotNull String serverName, @NotNull String name, @NotNull Material material, @NotNull List<String> lore, List<Integer> slots, @NotNull Integer modelData) {
        this.setItem(new InventoryItem()
                .setMaterial(material)
                .setCustomModelData(modelData)
                .setName(name)
                .setLore(lore)
                .addSlotList(slots)
                .addAction((ClickAction) (user, type, inventory) -> {
                    MineManiaUser mineManiaUser = MineManiaAPI_BukkitPlugin.getInstance().getUser(user.getUuid());
                    MineManiaLocation location = new MineManiaLocation(serverName, "null", 0, 0, 0);

                    // Teleport the player.
                    mineManiaUser.getActions().teleport(location);
                }));

    }
}
