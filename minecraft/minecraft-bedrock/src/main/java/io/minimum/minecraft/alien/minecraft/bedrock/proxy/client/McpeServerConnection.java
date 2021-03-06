package io.minimum.minecraft.alien.minecraft.bedrock.proxy.client;

import com.google.common.base.Preconditions;
import com.nimbusds.jose.JOSEException;
import io.minimum.minecraft.alien.minecraft.bedrock.pipeline.BedrockConnection;
import io.minimum.minecraft.alien.minecraft.bedrock.packet.McpeLogin;
import io.minimum.minecraft.alien.minecraft.bedrock.proxy.client.handler.InitialServerConnectionSessionHandler;
import io.minimum.minecraft.alien.minecraft.bedrock.proxy.client.transport.pspe.PSPEEncapsulatedServerTransport;
import io.minimum.minecraft.alien.minecraft.bedrock.proxy.client.transport.ServerTransport;
import io.minimum.minecraft.alien.minecraft.bedrock.proxy.data.ServerInfo;
import io.minimum.minecraft.alien.minecraft.bedrock.proxy.player.McpePlayer;
import io.minimum.minecraft.alien.minecraft.bedrock.util.EncryptionUtil;
import io.minimum.minecraft.alien.network.util.TransportType;
import io.netty.util.concurrent.Promise;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.URISyntaxException;

public class McpeServerConnection {
    private final ServerInfo target;
    private final McpePlayer player;
    private @Nullable BedrockConnection connection;
    private final ServerTransport transport;

    public McpeServerConnection(ServerInfo target, McpePlayer player) {
        this.target = target;
        this.player = player;
        this.transport = new PSPEEncapsulatedServerTransport(TransportType.bestType());
    }

    public Promise<Void> connect() {
        Preconditions.checkState(connection == null, "Already connected to remote server");

        Promise<Void> result = player.getConnection().eventLoop().newPromise();
        transport.open(player.getConnection(), target.getDestination())
                .addListener(future -> {
                    if (future.isSuccess()) {
                        this.connection = (BedrockConnection) future.getNow();
                        initiateLogin(result);
                    } else {
                        result.setFailure(future.cause());
                    }
                });
        return result;
    }

    public ServerInfo getTarget() {
        return target;
    }

    public McpePlayer getPlayer() {
        return player;
    }

    @Nullable
    public BedrockConnection getConnection() {
        return connection;
    }

    /**
     * Disconnects from the server.
     */
    public void disconnect() {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    /**
     * Ensures that the connection to the remote server is still active.
     * @return the connection
     */
    public BedrockConnection ensureConnection() {
        Preconditions.checkState(connection != null, "Connection is not active");
        return connection;
    }

    private void initiateLogin(Promise<Void> result) {
        Preconditions.checkState(connection != null, "Connection is not active");

        // At this point, we have an open connection to the remote server. Let's send our identity to the remote
        // server.
        McpeLogin login;
        try {
            login = EncryptionUtil.createFakeChain(player);
        } catch (JOSEException | URISyntaxException e) {
            result.tryFailure(new Exception("Unable to initiate login", e));
            return;
        }

        connection.setSessionHandler(new InitialServerConnectionSessionHandler(this, player));
        connection.write(login);
    }
}
