package me.imbanana.modtemplate.neoforge;

import me.imbanana.modtemplate.Modtemplate;
import net.neoforged.fml.common.Mod;

@Mod(Modtemplate.MOD_ID)
public final class ModtemplateNeoForge {
    public ModtemplateNeoForge() {
        // Run our common setup.
        Modtemplate.init();
    }
}
