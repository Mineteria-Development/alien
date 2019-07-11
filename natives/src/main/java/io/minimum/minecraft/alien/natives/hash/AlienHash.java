package io.minimum.minecraft.alien.natives.hash;

import io.minimum.minecraft.alien.natives.Disposable;
import io.minimum.minecraft.alien.natives.Native;
import io.netty.buffer.ByteBuf;

public interface AlienHash extends Native, Disposable {
    void update(ByteBuf buf);

    ByteBuf digest();
}
