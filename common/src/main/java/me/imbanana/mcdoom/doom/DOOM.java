package me.imbanana.mcdoom.doom;

import me.imbanana.mcdoom.McDoom;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DOOM {
    public static final int MAX_KEY_QUEUE_SIZE = 64;
    private final ConcurrentLinkedQueue<int[]> keyQueue = new ConcurrentLinkedQueue<>();
    private final String wadFile;

    public DOOM(String wadFile) {
        this.wadFile = wadFile;
    }

    public void start() {
        this.nativeStart(this.wadFile);
    }

    private native void nativeStart(String wadFile);
    public native void nativeStop();
    public native int getWidth();
    public native int getHeight();
    public native byte[] getFrame();

    public int[] pollKey() {
        return keyQueue.poll();
    }

    public void pushKey(boolean pressed, DoomKey doomKey) {
        if (keyQueue.size() >= MAX_KEY_QUEUE_SIZE) keyQueue.poll();
        keyQueue.add(new int[]{pressed ? 1 : 0, doomKey.getDoomCode()});
    }

    public static void loadNativeDoom() {
        String os = System.getProperty("os.name").toLowerCase();

        String folder = "linux";
        String ext = "so";
        if (os.contains("win")) {
            folder = "windows";
            ext = "dll";
        } else if (os.contains("mac") || os.contains("darwin")) {
            folder = "macos";
            ext = "dylib";
        }

        String doomFileName = "doom." + ext;
        String doomPath = "/natives/" + folder + "/" + doomFileName;

        try (InputStream inputStream = DOOM.class.getResourceAsStream(doomPath)) {
            if (inputStream == null) {
                throw new RuntimeException("Native doom not found (%s)".formatted(doomPath));
            }

            Path temp = Files.createTempFile("libdoom_" + folder + "_", doomFileName);
            temp.toFile().deleteOnExit();
            Files.copy(inputStream, temp, StandardCopyOption.REPLACE_EXISTING);
            System.load(temp.toAbsolutePath().toString());
            McDoom.LOGGER.info("Native doom loaded!");
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load native doom", exception);
        }
    }
}
