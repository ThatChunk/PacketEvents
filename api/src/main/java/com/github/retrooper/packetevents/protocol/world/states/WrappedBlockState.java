package com.github.retrooper.packetevents.protocol.world.states;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is designed to take advantage of modern minecraft versions
 * It has also been designed so that legacy versions can use this system
 * <p>
 * Write your code once, and use it everywhere.  Platform and version agnostic.
 * <p>
 * The mappings for legacy versions (1.12) was generated by setting blocks in the world at the pos id * 2, 255, data * 2
 * and then the world was upgraded to 1.18 and the block was read, dumping it all into a text file.
 * <p>
 * Mappings from modern versions are from ViaVersion, who have a similar (but a bit slower) system.
 */
public class WrappedBlockState {

    // TODO: 1.16.0/1.16.1 and 1.13.0/1.13.1 support
    private static String getMappingServerVersion(ServerVersion serverVersion) {
        if (serverVersion.isOlderThan(ServerVersion.V_1_13)) {
            return "legacy_block_mappings.txt";
        } else if (serverVersion.isOlderThan(ServerVersion.V_1_14)) {
            return "13.txt";
        } else if (serverVersion.isOlderThan(ServerVersion.V_1_15)) {
            return "14.txt";
        } else if (serverVersion.isOlderThan(ServerVersion.V_1_16)) {
            return "15.txt";
        } else if (serverVersion.isOlderThan(ServerVersion.V_1_16_2)) {
            return "16.txt";
        } else {
            return "17.txt";
        }
    }

    static {
        String mappingName = "17.txt";//getMappingServerVersion(PacketEvents.getAPI().getServerManager().getVersion());
        InputStream mappings = WrappedBlockState.class.getClassLoader().getResourceAsStream("assets/mappings/block/" + mappingName);
        BufferedReader paletteReader = new BufferedReader(new InputStreamReader(mappings));

        if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_13)) {
            loadLegacy(paletteReader);
        } else {
            loadModern(paletteReader);
        }
    }

    private static void loadLegacy(BufferedReader reader) {

    }

    private static void loadModern(BufferedReader reader) {
        List<String> globalPaletteToBlockData = new ArrayList<>();

        String line;

        try {
            while ((line = reader.readLine()) != null) {
                // Example line:
                // 109 minecraft:oak_wood[axis=x]
                String number = line.substring(0, line.indexOf(" "));

                // This is the integer used when sending chunks
                int globalPaletteID = Integer.parseInt(number);

                String blockString = line.substring(line.indexOf(" ") + 1);

                // Link this global palette ID to the blockdata for the second part of the script
                globalPaletteToBlockData.add(globalPaletteID, blockString);
            }
        } catch (IOException e) {
            PacketEvents.getAPI().getLogManager().debug("Palette reading failed! Unsupported version?");
            e.printStackTrace();
        }
        PacketEvents.getAPI().getLogManager().debug("Loaded " + globalPaletteToBlockData.size() + " mappings with state 21 being " + globalPaletteToBlockData.get(21));

    }
}
