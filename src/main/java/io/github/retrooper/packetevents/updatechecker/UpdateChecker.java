/*
 * MIT License
 *
 * Copyright (c) 2020 retrooper
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.retrooper.packetevents.updatechecker;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.utils.version.PEVersion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * PacketEvents update checker.
 *
 * @author retrooper
 * @since 1.6.9
 */
public class UpdateChecker {
    /**
     * Check for an update and log in the console.
     */
    public void handleUpdate() {
        PEVersion localVersion = PacketEvents.get().getVersion();
        inform("[packetevents] Checking for an update, please wait...");
        String line;
        try {
            line = readLatestVersion();
        } catch (IOException exception) {
            report("[packetevents] We failed to find the latest released version of PacketEvents. Your build: (" + localVersion.toString() + ")");
            return;
        }
        PEVersion newVersion = new PEVersion(line);
        if (localVersion.isOlderThan(newVersion)) {
            inform("[packetevents] There is an update available for the PacketEvents API! Your build: (" + localVersion.toString() + ") | Latest released build: (" + newVersion.toString() + ")");
        } else if (localVersion.isNewerThan(newVersion)) {
            inform("[packetevents] You are on a dev or pre released build of PacketEvents. Your build: (" + localVersion.toString() + ") | Latest released build: (" + newVersion.toString() + ")");
        } else if (localVersion.equals(newVersion)) {
            inform("[packetevents] You are on the latest released version of PacketEvents. (" + newVersion.toString() + ")");
        } else {
            report("[packetevents] Something went wrong while checking for an update. Your build: (" + localVersion.toString() + ") | Latest released build: (" + newVersion.toString() + ")");
        }
    }

    /**
     * Log a positive message in the console.
     *
     * @param message Message
     */
    private void inform(String message) {
        Bukkit.getLogger().info(message);
    }

    /**
     * Log a negative message in the console.
     *
     * @param message Message
     */
    private void report(String message) {
        Bukkit.getLogger().info(ChatColor.DARK_RED + message);
    }

    /**
     * Find the latest PacketEvents version as a string.
     *
     * @return Version as a String(like "1.8")
     * @throws IOException Exception if something fails.
     */
    private String readLatestVersion() throws IOException {
        URLConnection connection = new URL("https://api.spigotmc.org/legacy/update.php?resource=80279").openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla/4.0");
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line = reader.readLine();
        reader.close();
        return line;
    }
}