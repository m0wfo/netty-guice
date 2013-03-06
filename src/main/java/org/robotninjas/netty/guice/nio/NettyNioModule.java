package org.robotninjas.netty.guice.nio;

import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.*;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.Timer;
import org.robotninjas.netty.guice.annotations.ClientChannelFactory;
import org.robotninjas.netty.guice.annotations.DatagramChannelFactory;
import org.robotninjas.netty.guice.annotations.ServerChannelFactory;

import java.util.concurrent.Executor;

/**
 * A Guice module that creates and exposes ClientBootstrap, ServerBootstrap, and ConnectionlessBootstrap
 */
public class NettyNioModule extends PrivateModule {

  private static final int DEFAULT_WORKERS = Runtime.getRuntime().availableProcessors();
  private static final int DEFAULT_CLIENT_BOSSES = 1;
  private static final int DEFAULT_SERVER_BOSSES = 1;
  private final int numWorkers;
  private final int numClientBosses;
  private final int numServerBosses;

  /**
   * Usefuls when you are creating clients and/or servers
   *
   * @param numWorkers      number of worker threads
   * @param numClientBosses number of client boss threads
   * @param numServerBosses number of server boss threads
   */
  public NettyNioModule(int numWorkers, int numClientBosses, int numServerBosses) {
    this.numWorkers = numWorkers;
    this.numClientBosses = numClientBosses;
    this.numServerBosses = numServerBosses;
  }

  /**
   * Useful when you will only be creating clients.
   *
   * @param numWorkers      number of worker threads
   * @param numClientBosses number of client boss threads
   */
  public NettyNioModule(int numWorkers, int numClientBosses) {
    this(numWorkers, numClientBosses, DEFAULT_SERVER_BOSSES);
  }

  /**
   * Creates a Netty module setting all thread pools to their default levels
   */
  public NettyNioModule() {
    this(DEFAULT_WORKERS, DEFAULT_CLIENT_BOSSES, DEFAULT_SERVER_BOSSES);
  }

  @Override
  protected void configure() {
    bind(Timer.class).toInstance(new HashedWheelTimer());
    bind(ThreadNameDeterminer.class).toInstance(ThreadNameDeterminer.PROPOSED);
  }

  protected <T extends ChannelFactory> T addShutdownHook(final T factory) {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override public void run() {
        factory.releaseExternalResources();
        factory.shutdown();
      }
    });
    return factory;
  }

  protected <T extends AbstractNioWorkerPool> T addShutdownHook(final T pool) {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override public void run() {
        pool.releaseExternalResources();
        pool.shutdown();
      }
    });
    return pool;
  }

  protected <T extends AbstractNioBossPool> T addShutdownHook(final T pool) {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override public void run() {
        pool.releaseExternalResources();
        pool.shutdown();
      }
    });
    return pool;
  }

  @Provides
  @Singleton
  @WorkerPool
  public NioWorkerPool getNioWorkerPool(@PoolExecutor Executor executor, ThreadNameDeterminer nameDeterminer) {
    return addShutdownHook(new NioWorkerPool(executor, numWorkers, nameDeterminer));
  }

  @Provides
  @Singleton
  @ClientBossPool
  public NioClientBossPool getNioClientBossPool(@PoolExecutor Executor executor, Timer timer, ThreadNameDeterminer nameDeterminer) {
    return addShutdownHook(new NioClientBossPool(executor, numClientBosses, timer, nameDeterminer));
  }

  @Provides
  @Singleton
  @ServerBossPool
  public NioServerBossPool getNioServerBossPool(@PoolExecutor Executor executor, ThreadNameDeterminer nameDeterminer) {
    return (new NioServerBossPool(executor, numServerBosses, nameDeterminer));
  }

  @Provides
  @Singleton
  @DatagramWorkerPool
  public NioDatagramWorkerPool getNioDatagramWorkerPool(@PoolExecutor Executor executor) {
    return addShutdownHook(new NioDatagramWorkerPool(executor, numWorkers));
  }

  @Provides
  @Singleton
  @ServerChannelFactory
  public NioServerSocketChannelFactory getNioServerSocketChannelFactory(@ServerBossPool NioServerBossPool bossPool, @WorkerPool NioWorkerPool workerPool) {
    return addShutdownHook(new NioServerSocketChannelFactory(bossPool, workerPool));
  }

  @Provides
  @Singleton
  @ClientChannelFactory
  public NioClientSocketChannelFactory getNioClientSocketChannelFactory(@ClientBossPool NioClientBossPool bossPool, @WorkerPool NioWorkerPool workerPool) {
    return addShutdownHook(new NioClientSocketChannelFactory(bossPool, workerPool));
  }

  @Provides
  @Singleton
  public NioDatagramChannelFactory getNioDatagramChannelFactory(NioDatagramWorkerPool workerPool) {
    return addShutdownHook(new NioDatagramChannelFactory(workerPool));
  }

  @Provides
  @Exposed
  public ClientBootstrap getClientBootstrap(@ClientChannelFactory NioClientSocketChannelFactory channelFactory) {
    return new ClientBootstrap(channelFactory);
  }

  @Provides
  @Exposed
  public ServerBootstrap getServerBootstrap(@ServerChannelFactory NioServerSocketChannelFactory channelFactory) {
    return new ServerBootstrap(channelFactory);
  }

  @Provides
  @Exposed
  public ConnectionlessBootstrap getConnectionlessBootstrap(@DatagramChannelFactory NioDatagramChannelFactory channelFactory) {
    return new ConnectionlessBootstrap(channelFactory);
  }

}

