package io.minimum.minecraft.alien.network.mcpe.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation forces any packet sent to be sent in clear text. This is primarily used for handshaking.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ForceClearText {
}
