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

import com.github.minemaniauk.api.game.GameType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the bukkit material converter.
 * Converts a material identifier to a bukkit material.
 */
public class BukkitMaterialConverter implements GameType.Converter<Material> {

    @Override
    public @NotNull Material convert(@NotNull String materialIdentifier) {
        return Material.valueOf(materialIdentifier.toUpperCase());
    }
}
