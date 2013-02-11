package org.robotninjas.netty.guice;

import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.jboss.netty.channel.socket.nio.*;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.Timer;

import java.util.concurrent.Executor;

public class NettyModule extends PrivateModule {

  private final int numWorkers;
  private final int numClientBoss;
  private final int numServerBoss;

  public NettyModule(int numWorkers, int numClientBoss, int numServerBoss) {
    this.numWorkers = numWorkers;
    this.numClientBoss = numClientBoss;
    this.numServerBoss = numServerBoss;
  }

  @Override
  protected void configure() {
    bind(Timer.class).toInstance(new HashedWheelTimer());
    bind(ThreadNameDeterminer.class).toInstance(ThreadNameDeterminer.PROPOSED);
  }

  @Provides
  @Singleton
  @NettyWorkerPool
  public NioWorkerPool getNioWorkerPool(@NettyPoolExecutor Executor executor, ThreadNameDeterminer nameDeterminer) {
    return new NioWorkerPool(executor, numWorkers, nameDeterminer);
  }

  @Provides
  @Singleton
  @NettyClientBossPool
  public NioClientBossPool getNioClientBossPool(@NettyPoolExecutor Executor executor, Timer timer, ThreadNameDeterminer nameDeterminer) {
    return new NioClientBossPool(executor, numClientBoss, timer, nameDeterminer);
  }

  @Provides
  @Singleton
  @NettyServerBossPool
  public NioServerBossPool getNioServerBossPool(@NettyPoolExecutor Executor executor, ThreadNameDeterminer nameDeterminer) {
    return new NioServerBossPool(executor, numServerBoss, nameDeterminer);
  }

  @Provides
  @Singleton
  @NettyDgramWorkerPool
  public NioDatagramWorkerPool getNioDatagramWorkerPool(@NettyPoolExecutor Executor executor) {
    return new NioDatagramWorkerPool(executor, numWorkers);
  }

  @Provides
  @Exposed
  public NioServerSocketChannelFactory getNioServerSocketChannelFactory(@NettyServerBossPool NioServerBossPool bossPool,
                                                                        @NettyWorkerPool NioWorkerPool workerPool) {
    return new NioServerSocketChannelFactory(bossPool, workerPool);
  }

  @Provides
  @Exposed
  public NioClientSocketChannelFactory getNioClientSocketChannelFactory(@NettyClientBossPool NioClientBossPool bossPool,
                                                                        @NettyWorkerPool NioWorkerPool workerPool) {
    return new NioClientSocketChannelFactory(bossPool, workerPool);
  }

  @Provides
  @Exposed
  public NioDatagramChannelFactory getNioDatagramChannelFactory(NioDatagramWorkerPool workerPool) {
    return new NioDatagramChannelFactory(workerPool);
  }

}

