package com.mattworzala.canary.server.mixin;

import net.minestom.server.extensions.DiscoveredExtension;
import net.minestom.server.extensions.ExtensionManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(ExtensionManager.class)
public class ExtensionManagerMixin {


    @Inject(method = "discoverExtensions", at = @At("TAIL"), locals = LocalCapture.PRINT)
    private void loadExtensionFromClasspath(CallbackInfoReturnable<@NotNull List<DiscoveredExtension>> cir) {
        System.out.println("DISCOVERING EXTENSIONS");
    }
}
