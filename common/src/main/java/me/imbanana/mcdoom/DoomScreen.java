package me.imbanana.mcdoom;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import me.imbanana.mcdoom.doom.DOOM;
import me.imbanana.mcdoom.doom.DoomKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import java.util.Optional;

public class DoomScreen extends Screen {
    private final DOOM doom;

    private DynamicTexture texture;
    private Identifier textureId;

    public DoomScreen() {
        super(Component.translatable(McDoom.MOD_ID + ".menu.play"));
        this.doom = new DOOM(McDoom.getDoomWadFile());
    }

    @Override
    protected void init() {
        int width = this.doom.getWidth();
        int height = this.doom.getHeight();

        this.texture = new DynamicTexture(() -> "Doom", width, height, false);
        this.textureId = McDoom.ofId("doom");
        Minecraft.getInstance().getTextureManager().register(
                this.textureId,
                this.texture
        );

        Thread gameThread = new Thread(this.doom::start, "doom-game-loop");
        gameThread.setDaemon(true);
        gameThread.start();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        int doomWidth = this.doom.getWidth();
        int doomHeight = this.doom.getHeight();
        byte[] frame = this.doom.getFrame().clone();

        NativeImage pixels = this.texture.getPixels();

        for (int y = 0; y < doomHeight; y++) {
            for (int x = 0; x < doomWidth; x++) {
                int index = (x + y * doomWidth) * 4;
                int alpha = 0xFF;
                int red = frame[index + 2] & 0xFF;
                int green = frame[index + 1] & 0xFF;
                int blue = frame[index] & 0xFF;

                int argb = (alpha << 24) | (red << 16) | (green << 8) | blue;

                pixels.setPixel(x, y, argb);
            }
        }

        texture.upload();
        graphics.blit(textureId, 0, 0, width, height, 0.0f, 1.0f, 0.0f, 1.0f);
    }

    @Override
    public void onClose() {
        super.onClose();
        this.doom.nativeStop();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape() && event.hasShiftDown() && this.shouldCloseOnEsc()) {
            this.onClose();
            return true;
        }

        Optional<DoomKey> doomKey = DoomKey.fromMcCode(event.key(), McDoom.WASD_LAYOUT);
        if (doomKey.isPresent()) {
            this.doom.pushKey(true, doomKey.get());
            return true;
        }

        return false;
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        Optional<DoomKey> doomKey = DoomKey.fromMcCode(event.key(), McDoom.WASD_LAYOUT);
        if (doomKey.isPresent()) {
            this.doom.pushKey(false, doomKey.get());
            return true;
        }

        return false;
    }

    @Override
    protected void repositionElements() {

    }
}
