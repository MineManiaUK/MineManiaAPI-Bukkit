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
import com.github.cozyplugins.cozylibrary.inventory.action.action.ConfirmAction;
import com.github.cozyplugins.cozylibrary.inventory.inventory.ConfirmationInventory;
import com.github.cozyplugins.cozylibrary.user.PlayerUser;
import com.github.minemaniauk.api.database.collection.GameRoomCollection;
import com.github.minemaniauk.api.database.record.GameRoomInviteRecord;
import com.github.minemaniauk.api.database.record.GameRoomRecord;
import com.github.minemaniauk.bukkitapi.MineManiaAPI_Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Represents the invite list inventory.
 */
public class InviteListInventory extends CozyInventory {

    /**
     * Used to create an invitation list inventory instance.
     */
    public InviteListInventory() {
        super(54, "&8&lGame Room Invites");

        // Start regenerating.
        this.startRegeneratingInventory(100);
    }

    @Override
    protected void onGenerate(PlayerUser player) {

        // Get the list of invites for this player.
        List<GameRoomInviteRecord> recordList = MineManiaAPI_Bukkit.getInstance().getAPI()
                .getGameManager()
                .getInviteList(player.getUuid());

        // Loop though each record.
        int slot = -1;
        for (GameRoomInviteRecord record : recordList) {
            slot++;

            // Check if the invite is still valid.
            if (!record.isValid()) {
                record.remove();
                continue;
            }

            // Check if the slot is out of range.
            if (slot > 53) return;

            // Get the instance of the game room.
            final GameRoomRecord gameRoomRecord = MineManiaAPI_Bukkit.getInstance().getAPI()
                    .getDatabase()
                    .getTable(GameRoomCollection.class)
                    .getGameRoom(UUID.fromString(record.gameRoomUuid))
                    .orElseThrow();

            if (MineManiaAPI_Bukkit.getInstance().getAPI()
                    .getGameManager()
                    .getArena(gameRoomRecord.getUuid())
                    .isPresent()) {

                this.setItem(new InventoryItem()
                        .setMaterial(Material.PLAYER_HEAD)
                        .setSkull(gameRoomRecord.getOwner().getUniqueId())
                        .setName("&f&lInvite From &6" + gameRoomRecord.getOwner().getName())
                        .setLore("&eThis player is currently in a game,",
                                "&eplease check back when the game has finished.")
                );
                return;
            }

            // Set the item.
            this.setItem(new InventoryItem()
                    .setMaterial(Material.PLAYER_HEAD)
                    .setSkull(gameRoomRecord.getOwner().getUniqueId())
                    .setName("&f&lInvite From &6" + gameRoomRecord.getOwner().getName())
                    .setLore("&7Click to open the &fconfirm/decline &7menu.",
                            "&7",
                            "&fGame Type &a" + gameRoomRecord.getGameType().getName(),
                            "&fPlayers &a" + gameRoomRecord.getPlayers().size())
                    .addAction((ClickAction) (user, type, inventory) -> {
                        new ConfirmationInventory(new ConfirmAction()
                                .setAnvilTitle("&8&lAccept Invite")
                                .setConfirm(confirmUser -> {
                                    record.remove();

                                    // Check if they are already in a game room.
                                    final GameRoomRecord gameRoomIn = MineManiaAPI_Bukkit.getInstance().getAPI()
                                            .getDatabase()
                                            .getTable(GameRoomCollection.class)
                                            .getGameRoomFromPlayer(confirmUser.getUuid())
                                            .orElse(null);

                                    if (gameRoomIn != null) {
                                        if (gameRoomIn.getOwner().getUniqueId().equals(confirmUser.getUuid())) {
                                            confirmUser.sendMessage("&7&l> &7Previous game room has been deleted.");
                                            MineManiaAPI_Bukkit.getInstance().getAPI()
                                                    .getDatabase()
                                                    .getTable(GameRoomCollection.class)
                                                    .removeRecord(gameRoomIn);
                                        } else {
                                            confirmUser.sendMessage("&7&l> &7You have left your previous game room.");
                                            gameRoomIn.removePlayer(confirmUser.getUuid());
                                            gameRoomIn.save();
                                        }
                                    }

                                    final GameRoomRecord gameRoomRecordNew = MineManiaAPI_Bukkit.getInstance().getAPI()
                                            .getDatabase()
                                            .getTable(GameRoomCollection.class)
                                            .getGameRoom(UUID.fromString(record.gameRoomUuid))
                                            .orElse(null);

                                    if (gameRoomRecordNew == null) {
                                        user.sendMessage("&7&l> &7This game room no longer exists.");
                                        return;
                                    }

                                    // Add the player to the game room.
                                    gameRoomRecordNew.addPlayer(confirmUser.getUuid());
                                    gameRoomRecordNew.save();

                                    user.sendMessage("&7&l> &7You have been added to the game room.");
                                    new GameRoomInventory(gameRoomRecordNew.getUuid()).open(confirmUser.getPlayer());
                                })
                                .setAbort(abortUser -> {
                                    new InviteListInventory().open(abortUser.getPlayer());
                                })
                        ).open(user.getPlayer());
                    })
            );
        }
    }
}
