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

import com.github.cozyplugins.cozylibrary.inventory.action.action.ConfirmAction;
import com.github.cozyplugins.cozylibrary.inventory.inventory.ConfirmationInventory;
import com.github.minemaniauk.api.MineManiaLocation;
import com.github.minemaniauk.api.database.collection.GameRoomCollection;
import com.github.minemaniauk.api.database.record.GameRoomRecord;
import com.github.minemaniauk.api.game.Arena;
import com.github.minemaniauk.api.user.MineManiaUser;
import com.github.minemaniauk.bukkitapi.MineManiaAPI_BukkitPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents the join game inventory.
 * This is shown when there game room is currently in a game.
 */
public class JoinGameInventory extends ConfirmationInventory {

    /**
     * Used to create a new join game inventory.
     *
     * @param gameRoomIdentifier The game room identifier.
     */
    public JoinGameInventory(@NotNull UUID gameRoomIdentifier) {
        super(new ConfirmAction()
                .setAnvilTitle("&8&lJoin Game")
                .setConfirm(user -> {

                    final GameRoomRecord gameRoomRecord = MineManiaAPI_BukkitPlugin.getInstance().getAPI().getDatabase()
                            .getTable(GameRoomCollection.class)
                            .getGameRoom(gameRoomIdentifier).orElse(null);

                    if (gameRoomRecord == null) {
                        user.sendMessage("&7&l> &7The game room you are in no longer exists.");
                        new MenuInventory().open(user.getPlayer());
                        return;
                    }

                    final Arena arena = MineManiaAPI_BukkitPlugin.getInstance().getAPI()
                            .getGameManager()
                            .getArena(gameRoomIdentifier).orElse(null);

                    if (arena == null) {
                        user.sendMessage("&7&l> &7The game has ended.");
                        new GameRoomInventory(gameRoomIdentifier).open(user.getPlayer());
                        return;
                    }

                    MineManiaLocation location = new MineManiaLocation(
                            arena.getServerName(), "null", 0, 0, 0
                    );

                    MineManiaUser mineManiaUser = new MineManiaUser(user.getUuid(), user.getName());
                    mineManiaUser.getActions().teleport(location);
                })
                .setAbort(user -> {

                    final GameRoomRecord gameRoomRecord = MineManiaAPI_BukkitPlugin.getInstance().getAPI().getDatabase()
                            .getTable(GameRoomCollection.class)
                            .getGameRoom(gameRoomIdentifier).orElse(null);

                    if (gameRoomRecord == null) {
                        new MenuInventory().open(user.getPlayer());
                        return;
                    }

                    // Check if they are the owner.
                    if (gameRoomRecord.getOwner().getUniqueId().equals(user.getUuid())) {

                        // Check if they were the only player in that game room.
                        if (gameRoomRecord.getPlayerUuids().size() == 1) {
                            MineManiaAPI_BukkitPlugin.getInstance().getAPI()
                                    .getDatabase()
                                    .getTable(GameRoomCollection.class)
                                    .removeRecord(gameRoomRecord);
                            new MenuInventory().open(user.getPlayer());
                            return;
                        }

                        gameRoomRecord.removePlayer(user.getUuid());
                        gameRoomRecord.setOwner(gameRoomRecord.getPlayerUuids().get(0));
                        gameRoomRecord.save();
                        new MenuInventory().open(user.getPlayer());
                        return;
                    }

                    // Otherwise, remove them from the game room.
                    gameRoomRecord.removePlayer(user.getUuid());
                    gameRoomRecord.save();
                    new MenuInventory().open(user.getPlayer());
                })
        );
    }
}
