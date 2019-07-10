package io.minimum.minecraft.alien.network.util;

import io.netty.util.concurrent.FastThreadLocalThread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkNotNull;

public class AlienNettyThreadFactory implements ThreadFactory {

  private final AtomicInteger threadNumber = new AtomicInteger();
  private final String nameFormat;

  public AlienNettyThreadFactory(String nameFormat) {
    this.nameFormat = checkNotNull(nameFormat, "nameFormat");
  }

  @Override
  public Thread newThread(Runnable r) {
    String name = String.format(nameFormat, threadNumber.incrementAndGet());
    return new FastThreadLocalThread(r, name);
  }
}
