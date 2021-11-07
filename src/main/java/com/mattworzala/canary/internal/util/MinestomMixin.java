package com.mattworzala.canary.internal.util;

import com.mattworzala.canary.internal.util.safety.EnvType;
import com.mattworzala.canary.internal.util.safety.Env;
import net.minestom.server.extensions.ExtensionManager;
import net.minestom.server.extras.selfmodification.mixins.MixinCodeModifier;
import net.minestom.server.extras.selfmodification.mixins.MixinServiceMinestom;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.launch.platform.CommandLineOptions;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.service.ServiceNotAvailableError;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Taken from {@link net.minestom.server.Bootstrap}.
 * <p>
 * Very similar to the Minestom bootstrap, except it doesn't execute a main class.
 */
@Env(EnvType.PLATFORM)
public class MinestomMixin {

    public static void inject(String... args) {
        try {
            ClassLoader classLoader = ClassLoaders.MINESTOM;
            startMixin(args);
            try {
                ClassLoaders.MINESTOM.addCodeModifier(new MixinCodeModifier());
            } catch (RuntimeException e) {
                e.printStackTrace();
                System.err.println("Failed to add MixinCodeModifier, mixins will not be injected. Check the log entries above to debug.");
            }

            ExtensionManager.loadCodeModifiersEarly();

            MixinServiceMinestom.gotoPreinitPhase();
            // ensure extensions are loaded when starting the server
            Class<?> serverClass = classLoader.loadClass("net.minestom.server.MinecraftServer");
            Method init = serverClass.getMethod("init");
            init.invoke(null);
            MixinServiceMinestom.gotoInitPhase();

            MixinServiceMinestom.gotoDefaultPhase();
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void startMixin(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // hacks required to pass custom arguments
        Method start = MixinBootstrap.class.getDeclaredMethod("start");
        start.setAccessible(true);
        try {
            if (!((boolean) start.invoke(null))) {
                return;
            }
        } catch (ServiceNotAvailableError e) {
            e.printStackTrace();
            System.err.println("Failed to load Mixin, see error above.");
            System.err.println("It is possible you simply have two files with identical names inside your server jar. " +
                    "Check your META-INF/services directory inside your Minestom implementation and merge files with identical names inside META-INF/services.");

            return;
        }

        Method doInit = MixinBootstrap.class.getDeclaredMethod("doInit", CommandLineOptions.class);
        doInit.setAccessible(true);
        doInit.invoke(null, CommandLineOptions.ofArgs(Arrays.asList(args)));

        MixinBootstrap.getPlatform().inject();
        Mixins.getConfigs().forEach(c -> ClassLoaders.MINESTOM.protectedPackages.add(c.getConfig().getMixinPackage()));
    }
}
