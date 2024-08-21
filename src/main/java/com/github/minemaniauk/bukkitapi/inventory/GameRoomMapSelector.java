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
import com.github.cozyplugins.cozylibrary.item.CozyItem;
import com.github.cozyplugins.cozylibrary.user.PlayerUser;
import com.github.minemaniauk.api.database.collection.ArenaCollection;
import com.github.minemaniauk.api.database.collection.GameRoomCollection;
import com.github.minemaniauk.api.database.record.ArenaRecord;
import com.github.minemaniauk.api.database.record.GameRoomRecord;
import com.github.minemaniauk.api.game.Arena;
import com.github.minemaniauk.bukkitapi.BukkitMaterialConverter;
import com.github.minemaniauk.bukkitapi.MineManiaAPI_BukkitPlugin;
import com.github.smuddgge.squishydatabase.Query;
import com.github.squishylib.configuration.ConfigurationSection;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Represents a game room map selector.
 */
public class GameRoomMapSelector extends CozyInventory {

    private final @NotNull UUID gameRoomIdentifier;
    private int pageIndex;

    /**
     * Used to create a game selector inventory.
     */
    public GameRoomMapSelector(@NotNull UUID gameRoomIdentifier) {
        super(54, "&f₴₴₴₴₴₴₴₴\uf234");

        this.gameRoomIdentifier = gameRoomIdentifier;
        this.pageIndex = 0;

        // Start regenerating.
        this.startRegeneratingInventory(20);
    }

    @Override
    protected void onGenerate(PlayerUser player) {
        this.resetInventory();

        // Get the instance of teh game room record.
        final GameRoomRecord record = MineManiaAPI_BukkitPlugin.getInstance().getAPI().getDatabase()
                .getTable(GameRoomCollection.class)
                .getGameRoom(this.gameRoomIdentifier).orElse(null);

        if (record == null) {
            new MenuInventory().open(player.getPlayer());
            player.sendMessage("&7&l> &7Your game room no longer exists.");
            return;
        }

        // Back button.
        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName("&a&lBack")
                .setLore("&7Click to go back to the &fgame room&7.")
                .addAction((ClickAction) (user, type, inventory) -> {
                    new GameRoomInventory(this.gameRoomIdentifier).open(user.getPlayer());
                })
                .addSlot(45)
        );

        // Back page button.
        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName("&e&lBack Page")
                .setLore("&7Click to go back a page.")
                .addAction((ClickAction) (user, type, inventory) -> {
                    if (this.pageIndex == 0) return;
                    this.pageIndex--;
                    this.onGenerate(user);
                })
                .addSlot(48)
        );

        // Next page button.
        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName("&e&lNext Page")
                .setLore("&7Click to go forward a page.")
                .addAction((ClickAction) (user, type, inventory) -> {
                    this.pageIndex++;
                    this.onGenerate(user);
                })
                .addSlot(50)
        );

        // Help button.
        this.setItem(new InventoryItem()
                .setMaterial(Material.PINK_STAINED_GLASS_PANE)
                .setCustomModelData(1)
                .setName("&b&lHelp")
                .setLore("&7- &fThis inventory displays the list of",
                        "&7 &7 &favailable arenas you can play &e" + record.getGameType().getName() + " &fin.",
                        "&7- &fClick an arena to start a game.")
                .addSlot(49)
        );

        // Add arenas.
        final List<Integer> slots = List.of(
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        );
        final Iterator<Integer> slotIterator = slots.iterator();
        final int arenasToSkip = this.pageIndex * slots.size();
        int arenaIndex = -1;

        // Loop though arenas.
        for (Arena arena : MineManiaAPI_BukkitPlugin.getInstance().getAPI()
                .getGameManager()
                .getAvailableArenas(record.getGameType())) {

            arenaIndex++;

            if (arenaIndex < arenasToSkip) continue;
            if (!slotIterator.hasNext()) return;

            // Get the item section.
            final ConfigurationSection itemSection = arena.getDisplayItemSection();

            // Check if the item section is null.
            if (itemSection == null) {

                // Get the map name.
                String mapName = "&f&lDefault";
                if (arena.getMapName() != null) mapName = "&f7l" + arena.getMapName();

                this.setArena(
                        new CozyItem()
                                .setMaterial(record.getGameType().getMaterial(new BukkitMaterialConverter()))
                                .setName(mapName)
                                .setLore("&7",
                                        "&fMin Players &a" + arena.getMinPlayers(),
                                        "&fMax Players &a" + arena.getMaxPlayers(),
                                        "&7",
                                        "&fMap Name &e" + mapName,
                                        "&7",
                                        "&f&l" + arena.getGameType().getTitle()
                                ),
                        arena.getIdentifier(),
                        slotIterator.next(),
                        record
                );
                continue;
            }

            this.setArena(new CozyItem().convert(arena.getDisplayItemSection()), arena.getIdentifier(), slotIterator.next(), record);
        }
    }

    /**
     * Used to set an arena in the map selector.
     *
     * @param item The instance of the arena item.
     * @param arenaIdentifier The instance of the identifier.
     * @param slot The slot to put the arena in.
     */
    public void setArena(@NotNull CozyItem item, @NotNull UUID arenaIdentifier, int slot, @NotNull GameRoomRecord gameRoom) {
        this.setItem(new InventoryItem(item.create())
                .addAction((ClickAction) (user, type, inventory) -> {

                    // Check if the arena is still available.
                    final ArenaRecord arenaRecord = MineManiaAPI_BukkitPlugin.getInstance().getAPI()
                            .getDatabase()
                            .getTable(ArenaCollection.class)
                            .getFirstRecord(new Query().match("identifier", arenaIdentifier.toString()));

                    // Check if the arena no longer exists.
                    if (arenaRecord == null) {
                        user.sendMessage("&c&l> &cThis arena has recently been deactivated.");
                        this.onGenerate(user);
                        return;
                    }

                    // Get the instance of the arena.
                    final Arena arena = arenaRecord.asArena();

                    // Check if it returned an arena.
                    if (arena.isActivated()) {
                        user.sendMessage("&c&l> &cThis arena has recently been activated by another game room.");
                        this.onGenerate(user);
                        return;
                    }

                    // Check if the room has to little players.
                    if (gameRoom.getPlayerUuids().size() < arena.getMinPlayers()) {
                        user.sendMessage("&7&l> &7You need more players to play in this arena.");
                        this.onGenerate(user);
                        return;
                    }

                    // Check if the room has to many players.
                    if (gameRoom.getPlayerUuids().size() > arena.getMaxPlayers()) {
                        user.sendMessage("&7&l> &7There are too many players in your game room to play in this arena.");
                        this.onGenerate(user);
                        return;
                    }

                    user.sendMessage("&7&l> &7Arena has been confirmed.");
                    user.getPlayer().closeInventory();

                    // Set game room identifier before other rooms take the arena.
                    arena.setGameRoomIdentifier(this.gameRoomIdentifier);
                    arena.save();

                    // Start the game.
                    arena.activate();
                })
                .addSlot(slot)
        );
    }
}
