package io.minimum.minecraft.alien.network.mcpe.util;

import lombok.NonNull;
import lombok.Value;

@Value
public class ResourcePackInfo {
    @NonNull
    private final String packageId;
    @NonNull
    private final String version;
    private final long unknown;
}
