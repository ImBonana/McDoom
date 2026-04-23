package me.imbanana.mcdoom;

import me.imbanana.mcdoom.doom.DOOM;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class McDoom {
    public static final String MOD_ID = "mcdoom";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static boolean WASD_LAYOUT = true;

    public static void init() {
        DOOM.loadNativeDoom();
    }

    public static Identifier ofId(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static String getDoomWadFile() {
        try (InputStream inputStream = McDoom.class.getResourceAsStream("/doom/doom1.wad")) {
            if (inputStream != null) {
                Path temp = Files.createTempFile("", "doom1.wad");
                temp.toFile().deleteOnExit();
                Files.copy(inputStream, temp, StandardCopyOption.REPLACE_EXISTING);
                return temp.toFile().getAbsolutePath();
            }
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load wad file!", exception);
        }

        throw new RuntimeException("Wad file not found!");
    }
}
