package io.minimum.minecraft.alien.natives.util;

import com.google.common.collect.ImmutableList;
import io.minimum.minecraft.alien.natives.compression.JavaVelocityCompressor;
import io.minimum.minecraft.alien.natives.encryption.JavaVelocityCipher;
import io.minimum.minecraft.alien.natives.NativeSetupException;
import io.minimum.minecraft.alien.natives.compression.VelocityCompressorFactory;
import io.minimum.minecraft.alien.natives.encryption.VelocityCipherFactory;
import io.minimum.minecraft.alien.natives.hash.AlienHash;
import io.minimum.minecraft.alien.natives.hash.JavaAlienHash;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Supplier;

public class Natives {

    private Natives() {
        throw new AssertionError();
    }

    private static Runnable copyAndLoadNative(String path) {
        return () -> {
            try {
                Path tempFile = Files.createTempFile("native-", path.substring(path.lastIndexOf('.')));
                InputStream nativeLib = Natives.class.getResourceAsStream(path);
                if (nativeLib == null) {
                    throw new IllegalStateException("Native library " + path + " not found.");
                }

                Files.copy(nativeLib, tempFile, StandardCopyOption.REPLACE_EXISTING);
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        Files.deleteIfExists(tempFile);
                    } catch (IOException ignored) {
                        // Well, it doesn't matter...
                    }
                }));
                System.load(tempFile.toAbsolutePath().toString());
            } catch (IOException e) {
                throw new NativeSetupException("Unable to copy natives", e);
            }
        };
    }

    public static final NativeCodeLoader<VelocityCompressorFactory> compress = new NativeCodeLoader<>(
            ImmutableList.of(
                    new NativeCodeLoader.Variant<>(NativeCodeLoader.ALWAYS, () -> {
                    }, "Java", JavaVelocityCompressor.FACTORY)
            )
    );

    public static final NativeCodeLoader<VelocityCipherFactory> cipher = new NativeCodeLoader<>(
            ImmutableList.of(
                    new NativeCodeLoader.Variant<>(NativeCodeLoader.ALWAYS, () -> {
                    }, "Java", JavaVelocityCipher.FACTORY)
            )
    );

    public static final NativeCodeLoader<Supplier<AlienHash>> hash = new NativeCodeLoader<>(
            ImmutableList.of(
                    new NativeCodeLoader.Variant<>(NativeCodeLoader.ALWAYS, () -> {
                    }, "Java", JavaAlienHash::new)
            )
    );
}
