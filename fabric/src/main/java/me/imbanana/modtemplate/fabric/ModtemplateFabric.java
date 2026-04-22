package me.imbanana.modtemplate.fabric;

import me.imbanana.modtemplate.Modtemplate;
import net.fabricmc.api.ModInitializer;

public final class ModtemplateFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Modtemplate.init();
    }
}
