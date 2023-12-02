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

package com.github.minemaniauk.bukkitapi.dependency;

import com.github.minemaniauk.bukkitapi.MineManiaAPI_Bukkit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIDependency {

    /**
     * Used to get the instance of hte placeholder adapter.
     *
     * @return Throws an error if it is not installed.
     */
    public static @NotNull PlaceholderAPIHelper getInstance() {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            MineManiaAPI_Bukkit.getInstance().getLogger().warning("Placeholder API is not installed.");
            return new PlaceholderAPIHelper() {
                @Override
                public @NotNull String parse(@NotNull String message, @Nullable Player player) {
                    return message;
                }
            };
        }
        return new PlaceholderAPIAdapter();
    }
}
