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
import com.github.minemaniauk.api.database.collection.GameRoomCollection;
import com.github.minemaniauk.api.database.record.GameRoomRecord;
import com.github.minemaniauk.api.game.GameType;
import com.github.minemaniauk.api.user.MineManiaUser;
import com.github.minemaniauk.bukkitapi.BukkitMaterialConverter;
import com.github.minemaniauk.bukkitapi.MineManiaAPI_Bukkit;
import com.github.smuddgge.squishydatabase.Query;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents the game inventory.
 * Shows all the games a player can play and the rooms.
 */
public class GameInventory extends CozyInventory {

    /**
     * Used to create a game inventory.
     */
    public GameInventory() {
        super(54, "&f₴₴₴₴₴₴₴₴⏅");

        // Regenerating inventory.
        this.startRegeneratingInventory(100);
    }

    @Override
    protected void onGenerate(PlayerUser player) {
        this.resetInventory();

        // Tnt run button.
        this.setGameItem(GameType.TNT_RUN, List.of(0, 1, 9, 10));

        // Hide and seek button.
        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName("&7&lHide and Seek")
                .setLore("&eComing soon...")
                .addSlot(2, 3, 11, 12)
        );

        // Tnt run button.
        this.setGameItem(GameType.BED_WARS, List.of(4, 5, 13, 14));

        // Back button.
        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName("&a&lBack")
                .setLore("&7Click to go back to the main menu.")
                .addAction((ClickAction) (user, type, inventory) -> {
                    new MenuInventory().open(user.getPlayer());
                })
                .addSlot(45)
        );

        // Reload button.
        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName("&b&lReload Game Room List")
                .setLore("&7Click to reload the game room list.",
                        "&7You can also click in any blank space to reload the list.")
                .addAction((ClickAction) (user, type, inventory) -> {
                    new GameInventory().open(user.getPlayer());
                })
                .addSlot(46)
        );

        // More Rooms button.
        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName("&7&lMore Rooms")
                .setLore("&eComing soon...")
                .addSlot(47, 48, 49, 50)
        );

        // Profile button.
        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName("&d&lProfile")
                .setLore("&7",
                        "&7Paws &f" + MineManiaAPI_Bukkit.getInstance().getPaws(player.getUuid()))
                .addSlot(51, 52, 53)
        );

        // Add the public game rooms.
        this.setGameRooms(player);
    }

    /**
     * Used to set a game item into the inventory.
     *
     * @param gameType The game type.
     * @param slots The slots to set.
     */
    private void setGameItem(@NotNull GameType gameType, @NotNull List<Integer> slots) {
        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName("&f&l" + gameType.getTitle())
                .setLore("&7Click to create a new game room for &f" + gameType.getName() + "&7.")
                .addSlotList(slots)
                .addAction((ClickAction) (user, type, inventory) -> {

                    if (gameType.equals(GameType.TNT_RUN) && !user.hasPermission("game.tntrun")) {
                        user.sendMessage("&e&l> &eThis game is in development.");
                        return;
                    }

                    if (gameType.equals(GameType.BED_WARS) && !user.hasPermission("game.bedwars")) {
                        user.sendMessage("&e&l> &eThis game is in development.");
                        return;
                    }

                    // Create a new game room record.
                    GameRoomRecord record = new GameRoomRecord(user.getUuid(), gameType);
                    record.setPrivate(true);
                    record.save();

                    // Open the game room that has been created.
                    new GameRoomInventory(record.getUuid()).open(user.getPlayer());
                })
        );
    }

    /**
     * Used to set the game rooms.
     *
     * @param player The player that opened the inventory.
     */
    private void setGameRooms(@NotNull PlayerUser player) {

        // Get public rooms not in an arena.
        List<GameRoomRecord> roomRecordList = MineManiaAPI_Bukkit.getInstance()
                .getAPI().getDatabase()
                .getTable(GameRoomCollection.class)
                .getRecordList(new Query().match("is_private", false))
                .stream().filter(gameRoom -> MineManiaAPI_Bukkit.getInstance().getAPI()
                        .getGameManager()
                        .getArena(gameRoom.getUuid())
                        .isEmpty()
                )
                .toList();

        // First room.
        if (roomRecordList.isEmpty()) return;
        final GameRoomRecord firstRecord = roomRecordList.get(0);
        this.setRoomLine(
                player,
                firstRecord,
                27
        );

        // Second room.
        if (roomRecordList.size() < 2) return;
        final GameRoomRecord secondRecord = roomRecordList.get(1);
        this.setRoomLine(
                player,
                secondRecord,
                36
        );
    }

    public void setRoomLine(@NotNull PlayerUser player, @NotNull GameRoomRecord record, int startSlot) {

        // Add the users.
        int slot = startSlot - 1;
        for (MineManiaUser user : record.getPlayers()) {
            slot++;
            if (slot > startSlot + 5) continue;

            // Set the player item.
            this.setItem(new InventoryItem()
                    .setMaterial(Material.PLAYER_HEAD)
                    .setSkull(user.getUniqueId())
                    .setName("&f&l" + user.getName())
                    .addSlot(slot)
            );
        }

        // Add the game type.
        this.setItem(new InventoryItem()
                .setMaterial(record.getGameType().getMaterial(new BukkitMaterialConverter()))
                .setName("&f&l" + record.getGameType().getTitle())
                .setLore("&7This game room will be paying &f" + record.getGameType().getName() + "&7.")
                .addSlot(startSlot + 6)
        );

        // Add join item.
        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName("&a&lJoin Game Room")
                .setLore("&7Click to join this game room.",
                        "&7",
                        "&fGame Type &a" + record.getGameType().getName())
                .addSlot(startSlot + 7, startSlot + 8)
                .addAction((ClickAction) (user, type, inventory) -> {

                    // Add the player to the game room.
                    record.addPlayer(user.getUuid());
                    record.save();

                    // Open the game room inventory.
                    new GameRoomInventory(record.getUuid()).open(user.getPlayer());
                })
        );
    }
}
