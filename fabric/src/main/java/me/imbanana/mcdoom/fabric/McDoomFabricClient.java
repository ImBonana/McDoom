package me.imbanana.mcdoom.fabric;

import me.imbanana.mcdoom.McDoom;
import net.fabricmc.api.ClientModInitializer;

public final class McDoomFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        McDoom.init();
    }
}
