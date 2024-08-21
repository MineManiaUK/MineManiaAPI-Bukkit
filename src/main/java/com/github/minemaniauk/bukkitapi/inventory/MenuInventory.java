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
import com.github.minemaniauk.api.database.collection.GameRoomCollection;
import com.github.minemaniauk.api.database.record.GameRoomRecord;
import com.github.minemaniauk.api.game.Arena;
import com.github.minemaniauk.api.user.MineManiaUser;
import com.github.minemaniauk.bukkitapi.MineManiaAPI_BukkitPlugin;
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

        // SMP button.
        this.setTeleportItem(
                "smp",
                "&a&lSMP",
                List.of("&7Click to teleport to the public smp."),
                List.of(0, 1, 9, 10)
        );

        // World of calm button.
        this.setTeleportItem(
                "worldofcalm",
                "&b&lWorld of Calm",
                List.of("&7Click to teleport to the world of calm."),
                List.of(2, 3, 11, 12)
        );

        // Games button.
        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName("&d&lGames")
                .setLore("&7Click to view the games and the game rooms.")
                .addSlot(4, 5, 13, 14)
                .addAction((ClickAction) (user, type, inventory) -> {

                    // Check if the user is in a game room.
                    final GameRoomRecord record = MineManiaAPI_BukkitPlugin.getInstance().getAPI().getDatabase()
                            .getTable(GameRoomCollection.class)
                            .getGameRoomFromPlayer(user.getUuid())
                            .orElse(null);

                    if (record == null) {
                        // Otherwise, open the game inventory.
                        new GameInventory().open(user.getPlayer());
                        return;
                    }

                    // Check if the game room is in a game.
                    final Arena arena = MineManiaAPI_BukkitPlugin.getInstance().getAPI()
                            .getGameManager()
                            .getArena(record.getUuid())
                            .orElse(null);

                    if (arena == null) {
                        new GameRoomInventory(record.getUuid()).open(user.getPlayer());
                        return;
                    }

                    new JoinGameInventory(record.getUuid()).open(user.getPlayer());
                })
        );

        // Battlegrounds button.
        this.setTeleportItem(
                "battlegroundssmp",
                "&c&lBattle Grounds",
                List.of("&7Click to teleport to the battle grounds world."),
                List.of(6, 7, 15, 16)
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

        // Profile button.
        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName("&d&lProfile")
                .setLore("&7",
                        "&7Paws &f" + MineManiaAPI_BukkitPlugin.getInstance().getPaws(openUser.getUuid()))
                .addSlot(23, 24, 25, 26,
                        32, 33, 34, 35,
                        41, 42, 43, 44,
                        50, 51, 52, 53)
        );
    }

    /**
     * Used to set a teleport item into the inventory.
     *
     * @param serverName The name of the server.
     * @param name       The name of the item.
     * @param lore       The lore of the item.
     * @param slots      The slots to place the item.
     */
    private void setTeleportItem(@NotNull String serverName, @NotNull String name, @NotNull List<String> lore, List<Integer> slots) {
        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName(name)
                .setLore(lore)
                .addSlotList(slots)
                .addAction((ClickAction) (user, type, inventory) -> {
                    MineManiaUser mineManiaUser = MineManiaAPI_BukkitPlugin.getInstance().getUser(user.getUuid());
                    MineManiaLocation location = new MineManiaLocation(serverName, "null", 0, 0, 0);

                    // Teleport the player.
                    mineManiaUser.getActions().teleport(location);
                })
        );
    }
}
