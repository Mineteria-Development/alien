package io.minimum.minecraft.alien.natives.compression;

import io.minimum.minecraft.alien.natives.Disposable;
import io.minimum.minecraft.alien.natives.Native;
import io.netty.buffer.ByteBuf;

import java.util.zip.DataFormatException;

/**
 * Provides an interface to inflate and deflate {@link ByteBuf}s using zlib.
 */
public interface VelocityCompressor extends Disposable, Native {
    void inflate(ByteBuf source, ByteBuf destination, int max) throws DataFormatException;

    void deflate(ByteBuf source, ByteBuf destination) throws DataFormatException;
}
