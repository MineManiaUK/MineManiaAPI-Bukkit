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
import java.util.stream.Collectors;
import com.github.minemaniauk.api.MineManiaLocation;
import com.github.minemaniauk.api.user.MineManiaUser;
import com.github.minemaniauk.bukkitapi.MineManiaAPI_BukkitPlugin;
import com.github.squishylib.configuration.Configuration;
import com.github.squishylib.configuration.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents the main menu inventory.
 * You can create a new instance of this and use the
 * {@link CozyInventory#open(Player)} method to open the
 * inventory for a player.
 */
public class MenuInventory extends CozyInventory {

    /**
     * Used to create a new instance of the menu.
     */
    public MenuInventory() {
        super(54, "&f₴₴₴₴₴₴₴₴☀");
    }

    @Override
    protected void onGenerate(PlayerUser openUser) {

        Configuration serversYaml = MineManiaAPI_BukkitPlugin.getInstance().getServers();

        // SMP button.
        this.setTeleportItem(
                "smp",
                "&a&lSMP",
                Material.PINK_STAINED_GLASS_PANE,
                List.of("&7Click to teleport to the public smp."),
                List.of(0, 1, 9, 10),
                1
        );

        // World of calm button.
        this.setTeleportItem(
                "worldofcalm",
                "&b&lWorld of Calm",
                Material.PINK_STAINED_GLASS_PANE,
                List.of("&7Click to teleport to the world of calm."),
                List.of(2, 3, 11, 12),
                1
        );

        // Games button.
        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName("&d&lGames")
                .setLore("&7Click to view the game servers.")
                .addSlot(4, 5, 13, 14)
                .addAction((ClickAction) (user, type, inventory) -> {
                    new GamesMenuInventory().open(user.getPlayer());
                 })
        );


        // Battlegrounds button.
        this.setTeleportItem(
                "battlegroundssmp",
                "&c&lBattle Grounds",
                Material.PINK_STAINED_GLASS_PANE,
                List.of("&7Click to teleport to the battle grounds world."),
                List.of(6, 7, 15, 16),
                1
        );

        // Other button.
        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName("&a&lMore")
                .addSlot(8, 17)
                .addAction((ClickAction) (user, type, inventory) -> {
                    new MenuInventoryPage2().open(user.getPlayer());
                })
        );


        ConfigurationSection servers = MineManiaAPI_BukkitPlugin.getInstance()
                .getServers()
                .getSection("main");

        for (String key : servers.getKeys()) {
            ConfigurationSection s = servers.getSection(key);

            String name = s.getString("name");
            String matStr = s.getString("material");
            Material material = Material.matchMaterial(matStr);
            List<String> lore = s.getListString("lore");
            int pos = s.getInteger("position");

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
