package io.minimum.minecraft.alien.network.mcpe.pipeline.codec;

import io.minimum.minecraft.alien.network.mcpe.packet.McpePacket;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class McpePacketRegistry {
    private final Int2ObjectMap<Supplier<? extends McpePacket>> packetSuppliersById;
    private final Object2IntMap<Class<? extends McpePacket>> packetIdsByClass;

    public McpePacketRegistry() {
        this.packetSuppliersById = new Int2ObjectOpenHashMap<>();
        this.packetIdsByClass = new Object2IntOpenHashMap<>();
        this.packetIdsByClass.defaultReturnValue(Integer.MIN_VALUE);
    }

    public <P extends McpePacket> McpePacketRegistry register(int id, Class<P> packetClass, @Nullable Supplier<P> supplier) {
        checkNotNull(packetClass, "packetClass");
        checkArgument(!packetIdsByClass.containsKey(packetClass), "Packet class already registered");
        checkArgument(!packetSuppliersById.containsKey(id), "Packet ID already registered");

        if (supplier != null) {
            this.packetSuppliersById.put(id, supplier);
        }
        this.packetIdsByClass.put(packetClass, id);
        return this;
    }

    public int getId(Class<? extends McpePacket> packetClass) {
        return this.packetIdsByClass.getInt(packetClass);
    }

    public @Nullable McpePacket supply(int id) {
        Supplier<? extends McpePacket> supplier = packetSuppliersById.get(id);
        if (supplier == null) {
            return null;
        }
        return supplier.get();
    }
}
