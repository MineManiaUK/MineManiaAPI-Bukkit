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
import com.github.kerbity.kerb.result.CompletableResultSet;
import com.github.minemaniauk.api.database.collection.GameRoomCollection;
import com.github.minemaniauk.api.database.record.GameRoomRecord;
import com.github.minemaniauk.api.kerb.event.GetOnlinePlayersRequest;
import com.github.minemaniauk.api.user.MineManiaUser;
import com.github.minemaniauk.bukkitapi.MineManiaAPI_BukkitPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Represents the game room invite player inventory.
 * Used to invite players to your game room.
 */
public class GameRoomInvitePlayerInventory extends CozyInventory {

    private final @NotNull UUID gameRoomIdentifier;

    /**
     * Used to create an instance of teh game
     * room invite inventory.
     *
     * @param gameRoomIdentifier The game room identifier.
     */
    public GameRoomInvitePlayerInventory(@NotNull UUID gameRoomIdentifier) {
        super(54, "&8&lInvite Players");

        this.gameRoomIdentifier = gameRoomIdentifier;

        // Start the regenerating task.
        this.startRegeneratingInventory(100);
    }

    @Override
    protected void onGenerate(PlayerUser player) {
        this.resetInventory();

        new Thread(() -> {

            CompletableResultSet<GetOnlinePlayersRequest> resultSet = MineManiaAPI_BukkitPlugin.getInstance()
                    .getAPI()
                    .callEvent(new GetOnlinePlayersRequest());

            for (GetOnlinePlayersRequest result : resultSet.waitForFinalResult()) {
                if (result.getPlayerUuids().isEmpty()) continue;
                this.loadPlayers(result.getPlayerUuids());
            }

        }).start();
    }

    public void loadPlayers(@NotNull List<UUID> uuidList) {

        // Add all database players.
        int slot = -1;
        for (UUID uuid :uuidList) {

            OfflinePlayer offlinePlayer = null;
            try {
                offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            } catch (Exception exception) {
                continue;
            }
            final OfflinePlayer player = offlinePlayer;

            slot++;
            if (slot > 44) continue;

            // Check if the player has already been invited.
            if (MineManiaAPI_BukkitPlugin.getInstance().getAPI().getGameManager()
                    .hasBeenInvited(uuid, this.gameRoomIdentifier)) {

                this.setItem(new InventoryItem()
                        .setMaterial(Material.BLACK_STAINED_GLASS_PANE)
                        .setName("&f&l" + player.getName() + " &a&lHas Been Invited")
                        .addSlot(slot)
                );
                continue;
            }

            // Set the player's item.
            this.setItem(new InventoryItem()
                    .setMaterial(Material.PLAYER_HEAD)
                    .setSkull(uuid)
                    .setName("&6&lInvite &f&l" + player.getName())
                    .setLore("&7Click to send a invite to this player.")
                    .addSlot(slot)
                    .addAction((ClickAction) (user, type, inventory) -> {

                        // Get the game room.
                        GameRoomRecord record = MineManiaAPI_BukkitPlugin.getInstance().getAPI().getDatabase()
                                .getTable(GameRoomCollection.class)
                                .getGameRoom(this.gameRoomIdentifier)
                                .orElse(null);

                        if (record == null) {
                            user.sendMessage("&7&l> &7The game room you are in no longer exists.");
                            new MenuInventory().open(user.getPlayer());
                            return;
                        }

                        MineManiaAPI_BukkitPlugin.getInstance().getAPI().getGameManager()
                                .sendInvite(uuid, record);

                        // Send the sender a message.
                        user.sendMessage("&7&l> &7An invite has been sent to &f" + player.getName());

                        // Send the invited a message.
                        MineManiaUser invitedUser = new MineManiaUser(uuid, player.getName());
                        invitedUser.getActions().sendMessage("&7&l> &7You have been invited to play &f" + record.getGameType().getName() + " &7with &f" + record.getOwner().getName() + "&7. Run the command &e/invites &7to accept.");
                        this.onGenerate(user);
                    })
            );
        }

        // Back button.
        this.setItem(new InventoryItem()
                .setMaterial(Material.LIME_STAINED_GLASS_PANE)
                .setName("&a&lBack To Game Room")
                .setLore("&7Click to go back to the game room.")
                .addSlotRange(45, 53)
                .addAction((ClickAction) (user, type, inventory) -> {
                    new GameRoomInventory(this.gameRoomIdentifier).open(user.getPlayer());
                })
        );
    }
}
