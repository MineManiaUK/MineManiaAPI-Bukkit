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
import com.github.minemaniauk.api.game.Arena;
import com.github.minemaniauk.api.user.MineManiaUser;
import com.github.minemaniauk.bukkitapi.BukkitMaterialConverter;
import com.github.minemaniauk.bukkitapi.MineManiaAPI_Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class GameRoomInventory extends CozyInventory {

    private final @NotNull UUID gameRoomIdentifier;

    public GameRoomInventory(@NotNull UUID gameRoomIdentifier) {
        super(54, "&f₴₴₴₴₴₴₴₴㉿");

        this.gameRoomIdentifier = gameRoomIdentifier;

        // Start the regenerating.
        this.startRegeneratingInventory(100);
    }

    @Override
    protected void onGenerate(PlayerUser player) {
        this.resetInventory();

        // Get the instance of teh game room record.
        final GameRoomRecord record = MineManiaAPI_Bukkit.getInstance().getAPI().getDatabase()
                .getTable(GameRoomCollection.class)
                .getGameRoom(this.gameRoomIdentifier).orElse(null);

        // Check if the record is null.
        if (record == null) {
            this.close();
            return;
        }

        // Set the player items.
        this.setPlayerItems(record);

        // Game type item.
        this.setItem(new InventoryItem()
                .setMaterial(record.getGameType().getMaterial(new BukkitMaterialConverter()))
                .setName("&f&l" + record.getGameType().getTitle())
                .setLore("&7This game room will be playing &f" + record.getGameType().getName() + "&7.")
                .addSlot(53)
        );

        // Back button.
        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName("&a&lBack")
                .setLore("&7Click to go back to the &f/menu&7.")
                .addAction((ClickAction) (user, type, inventory) -> {
                    new MenuInventory().open(user.getPlayer());
                })
                .addSlot(45)
        );

        // Reload button.
        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName("&b&lReload Player List")
                .setLore("&7Click to reload the player list.",
                        "&7You can also click in any blank space to reload the list.")
                .addAction((ClickAction) (user, type, inventory) -> {
                    new GameRoomInventory(this.gameRoomIdentifier).open(user.getPlayer());
                })
                .addSlot(25)
        );

        // Leave button.
        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName("&c&lLeave Game Room")
                .setLore("&7Click to leave this game room.")
                .addSlot(46, 47)
                .addAction((ClickAction) (user, type, inventory) -> {

                    // Check if the player is the owner.
                    if (record.getOwner().getUniqueId().equals(user.getUuid())) {
                        MineManiaAPI_Bukkit.getInstance().getAPI()
                                .getDatabase()
                                .getTable(GameRoomCollection.class)
                                .removeRecord(record);

                        new GameInventory().open(user.getPlayer());
                        return;
                    }

                    // Otherwise, remove player from the game room.
                    record.removePlayer(user.getUuid());
                    record.save();

                    // Open the game inventory.
                    new GameInventory().open(user.getPlayer());
                })
        );

        // Start button.
        this.setStartButton(record, player);

        // Invite button.
        this.setInviteButton(record, player);

        // Visibility button.
        this.setVisibilityButton(record, player);
    }

    /**
     * Used to set the player items.
     *
     * @param record The game room record.
     */
    private void setPlayerItems(@NotNull GameRoomRecord record) {

        // Get the owner.
        MineManiaUser owner = record.getOwner();

        // Add the owner.
        this.setItem(new InventoryItem()
                .setMaterial(Material.PLAYER_HEAD)
                .setSkull(owner.getUniqueId())
                .setName("&6&l" + owner.getName())
                .addSlot(10)
        );

        // Create the slot iterator.
        Iterator<Integer> iterator = List.of(
                11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24
        ).iterator();

        // Loop though all the other users.
        for (MineManiaUser user : record.getPlayers()) {

            // Ensure they are not the owner.
            if (user.getUniqueId().equals(owner.getUniqueId())) continue;

            // Check if there are any more slots.
            if (!iterator.hasNext()) return;

            // Set the player's item.
            this.setItem(new InventoryItem()
                    .setMaterial(Material.PLAYER_HEAD)
                    .setSkull(user.getUniqueId())
                    .setName("&f&l" + user.getName())
                    .addSlot(iterator.next())
            );
        }
    }

    /**
     * Used to set the start button into the inventory.
     *
     * @param record The instance of the game room.
     * @param player The instance of the player that opened the inventory.
     */
    private void setStartButton(@NotNull GameRoomRecord record, @NotNull PlayerUser player) {

        // Check if the player is the owner of the game room.
        if (record.getOwner().getUniqueId().equals(player.getUuid())) {
            InventoryItem item = new InventoryItem()
                    .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                    .setCustomModelData(1)
                    .setName("&a&lStart Game")
                    .setLore("&7Click to start a game.",
                            "&7",
                            "&e&lAvailable Arenas"
                    )
                    .addAction((ClickAction) (user, type, inventory) -> {
                        this.startGame(record, user);
                    })
                    .addSlot(48, 49);

            // Add the arenas as lore.
            MineManiaAPI_Bukkit.getInstance().getAPI()
                    .getGameManager()
                    .getArenaAvailabilityAsLore(record.getGameType())
                    .stream().map(line -> "&7- &f" + line)
                    .forEach(item::addLore);

            this.setItem(item);
        }
    }

    /**
     * Called when the start game button is pressed.
     *
     * @param record The instance of the game record.
     * @param player The instance of the player that clicked.
     */
    private void startGame(@NotNull GameRoomRecord record, @NotNull PlayerUser player) {

        // Message the owner.
        player.sendMessage("&7&l> &7Searching for an arena...");

        // Check if there is an available arena.
        Arena arena = MineManiaAPI_Bukkit.getInstance().getAPI()
                .getGameManager()
                .getFirstAvailableArena(record.getGameType(), record.getPlayerUuids().size())
                .orElse(null);

        // Check if it returned an arena.
        if (arena == null) {
            player.sendMessage("&c&l> &cThere are currently no available arenas.");
            return;
        }

        // Set game room identifier before other rooms take the arena.
        arena.setGameRoomIdentifier(record.getUuid());
        arena.save();

        // Start the game.
        arena.activate();
    }

    /**
     * Used to add the invite button to the inventory.
     *
     * @param record The game room record.
     * @param player The player that opened the inventory.
     */
    private void setInviteButton(@NotNull GameRoomRecord record, @NotNull PlayerUser player) {

        // Check if the player is the owner of the game room.
        if (record.getOwner().getUniqueId().equals(player.getUuid())) {
            this.setItem(new InventoryItem()
                    .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                    .setCustomModelData(1)
                    .setName("&b&lInvite Players")
                    .setLore("&7Invite a player to your game room.")
                    .addSlot(50, 51)
                    .addAction((ClickAction) (user, type, inventory) -> {
                        new GameRoomInvitePlayerInventory(GameRoomInventory.this.gameRoomIdentifier).open(player.getPlayer());
                    })
            );
            return;
        }

        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName("&7&lInvite Players")
                .setLore("&fOnly the owner of the game room can invite players.")
                .addSlot(50, 51)
        );
    }

    /**
     * Used to set the visibility button.
     *
     * @param record The game room record instance.
     * @param player The player instance.
     */
    private void setVisibilityButton(@NotNull GameRoomRecord record, @NotNull PlayerUser player) {

        // Start building the item.
        InventoryItem item = new InventoryItem();

        // Check if the game room is private.
        if (record.isPrivate()) {
            item.setMaterial(Material.ENDER_PEARL);
            item.setName("&f&lThis Game Room is Private");
        }

        // Check if the game room is not private.
        if (!record.isPrivate()) {
            item.setMaterial(Material.ENDER_EYE);
            item.setName("&f&lThis Game Room is Public");
        }

        // Check if the player is the owner of the game room.
        if (record.getOwner().getUniqueId().equals(player.getUuid())) {

            // Check if the game room is private.
            if (record.isPrivate()) {
                item.setMaterial(Material.ENDER_PEARL);
                item.setLore("&7Click to change to &fpublic&7.");
            }

            // Check if the game room is not private.
            if (!record.isPrivate()) {
                item.setMaterial(Material.ENDER_EYE);
                item.setLore("&7Click to change to &fprivate&7.");
            }

            // Add the toggle action.
            item.addAction((ClickAction) (user, type, inventory) -> {
                record.setPrivate(!record.isPrivate());
                record.save();
                new GameRoomInventory(this.gameRoomIdentifier).open(user.getPlayer());
            });
        }

        item.addSlot(52);
        this.setItem(item);
    }
}
