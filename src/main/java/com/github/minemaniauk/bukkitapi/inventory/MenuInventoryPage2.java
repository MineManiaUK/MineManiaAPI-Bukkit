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
import com.github.minemaniauk.api.MineManiaLocation;
import com.github.minemaniauk.api.user.MineManiaUser;
import com.github.minemaniauk.bukkitapi.MineManiaAPI_BukkitPlugin;
import com.github.squishylib.configuration.Configuration;
import com.github.squishylib.configuration.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MenuInventoryPage2 extends CozyInventory {

    /**
     * Used to create a new instance of the menu.
     */
    public MenuInventoryPage2() {
        super(54, "&f₴₴₴₴₴₴₴₴✣");
    }
    Configuration serversYaml = MineManiaAPI_BukkitPlugin.getInstance().getServers();

    @Override
    protected void onGenerate(PlayerUser openUser) {

        // Creative.
        this.setTeleportItem(
                "creative",
                "&a&lCreative",
                Material.PINK_STAINED_GLASS_PANE,
                List.of("&7Click to teleport to the creative world."),
                List.of(1, 2, 10, 11),
                1
        );

        // Sky block.
        this.setTeleportItem(
                "skyblock",
                "&b&lSky Block",
                Material.PINK_STAINED_GLASS_PANE,
                List.of("&7Click to teleport to the sky block server."),
                List.of(3, 4, 12, 13),
                1
        );

        // Battlegrounds button.
        this.setTeleportItem(
                "craftyland",
                "&d&lCrafty Land",
                Material.PINK_STAINED_GLASS_PANE,
                List.of("&7Click to teleport to crafty land."),
                List.of(5, 6, 14, 15),
                1
        );

        // Other button.
        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName("&a&lBack")
                .addSlot(0, 9)
                .addAction((ClickAction) (user, type, inventory) -> {
                    new MenuInventory().open(user.getPlayer());
                })
        );

        // Battlegrounds button.
        this.setTeleportItem(
                "dungeons",
                "&c&lDungeons",
                Material.PINK_STAINED_GLASS_PANE,
                List.of("&7Click to teleport to the dungeons."),
                List.of(7, 8, 16, 17),
                1
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