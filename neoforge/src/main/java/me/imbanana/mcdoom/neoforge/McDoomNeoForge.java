package me.imbanana.mcdoom.neoforge;

import me.imbanana.mcdoom.McDoom;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = McDoom.MOD_ID, dist = Dist.CLIENT)
public final class McDoomNeoForge {
    public McDoomNeoForge() {
        // Run our common setup.
        McDoom.init();
    }
}
