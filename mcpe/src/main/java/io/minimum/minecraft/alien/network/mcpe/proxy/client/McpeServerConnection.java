package io.minimum.minecraft.alien.network.mcpe.proxy.client;

import com.google.common.base.Preconditions;
import com.nimbusds.jose.JOSEException;
import io.minimum.minecraft.alien.network.mcpe.listener.McpeConnection;
import io.minimum.minecraft.alien.network.mcpe.packet.McpeLogin;
import io.minimum.minecraft.alien.network.mcpe.proxy.client.handler.InitialServerConnectionSessionHandler;
import io.minimum.minecraft.alien.network.mcpe.proxy.client.transport.RakNetServerTransport;
import io.minimum.minecraft.alien.network.mcpe.proxy.client.transport.ServerTransport;
import io.minimum.minecraft.alien.network.mcpe.proxy.data.ServerInfo;
import io.minimum.minecraft.alien.network.mcpe.proxy.player.McpePlayer;
import io.minimum.minecraft.alien.network.mcpe.util.EncryptionUtil;
import io.minimum.minecraft.alien.network.util.TransportType;
import io.netty.util.concurrent.Promise;

import javax.annotation.Nullable;
import java.net.URISyntaxException;

public class McpeServerConnection {
    private final ServerInfo target;
    private final McpePlayer player;
    private @Nullable McpeConnection connection;
    private final ServerTransport transport;

    public McpeServerConnection(ServerInfo target, McpePlayer player) {
        this.target = target;
        this.player = player;
        this.transport = new RakNetServerTransport(TransportType.bestType());
    }

    public Promise<Void> connect() {
        Promise<Void> result = player.getConnection().eventLoop().newPromise();
        transport.open(player.getConnection(), target.getDestination())
                .addListener(future -> {
                    if (future.isSuccess()) {
                        this.connection = (McpeConnection) future.getNow();
                        initiateLogin(result);
                    } else {
                        result.setFailure(future.cause());
                    }
                });
        return result;
    }

    private void initiateLogin(Promise<Void> result) {
        Preconditions.checkState(connection != null, "Connection is not active");

        // At this point, we have an open connection to the remote server. Let's send our identity to the remote
        // server.
        McpeLogin login;
        try {
            login = new McpeLogin(354, EncryptionUtil.createFakeChain(player.getProfile()));
        } catch (JOSEException | URISyntaxException e) {
            result.tryFailure(new Exception("Unable to initiate login", e));
            return;
        }

        connection.setPacketHandler(new InitialServerConnectionSessionHandler(connection, player));
        connection.write(login);
    }
}
