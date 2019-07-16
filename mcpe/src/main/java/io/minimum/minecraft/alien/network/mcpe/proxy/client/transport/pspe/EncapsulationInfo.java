package io.minimum.minecraft.alien.network.mcpe.proxy.client.transport.pspe;

import java.net.InetSocketAddress;

public class EncapsulationInfo {
    private final boolean compressed;
    private final InetSocketAddress address;

    public EncapsulationInfo(boolean compressed, InetSocketAddress address) {
        this.compressed = compressed;
        this.address = address;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "EncapsulationInfo{" +
                "compressed=" + compressed +
                ", address=" + address +
                '}';
    }
}
