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

package com.github.minemaniauk.bukkitapi;

import com.github.minemaniauk.api.MineManiaLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the bukkit location converter.
 */
public class BukkitLocationConverter implements MineManiaLocation.LocationConverter<Location> {

    @Override
    public @NotNull MineManiaLocation getMineManiaLocation(@NotNull Location location) {
        return new MineManiaLocation(
                MineManiaAPI_Bukkit.getInstance().getAPI().getServerName(),
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ()
        );
    }

    @Override
    public @NotNull Location getLocationType(@NotNull MineManiaLocation location) {
        World world = Bukkit.getWorld(location.getWorldName());
        if (world == null) {
            throw new RuntimeException("Tried to get world " + location.getWorldName() + " but it doesnt exist.");
        }

        return new Location(
                world,
                location.getX(),
                location.getY(),
                location.getZ()
        );
    }
}