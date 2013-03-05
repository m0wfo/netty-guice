package org.robotninjas.netty.guice;

import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.jboss.netty.bootstrap.Bootstrap;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.*;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.Timer;

import java.util.concurrent.Executor;

/**
 * A Guice module that creates and exposes Timer, ClientBootstrap, ServerBootstrap, and ConnectionlessBootstrap
 */
public class NettyModule extends PrivateModule {

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
  public NettyModule(int numWorkers, int numClientBosses, int numServerBosses) {
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
  public NettyModule(int numWorkers, int numClientBosses) {
    this(numWorkers, numClientBosses, DEFAULT_SERVER_BOSSES);
  }

  /**
   * Creates a Netty module setting all thread pools to their default levels
   */
  public NettyModule() {
    this(DEFAULT_WORKERS, DEFAULT_CLIENT_BOSSES, DEFAULT_SERVER_BOSSES);
  }

  @Override
  protected void configure() {
    bind(Timer.class).toInstance(new HashedWheelTimer());
    expose(Timer.class);
    bind(ThreadNameDeterminer.class).toInstance(ThreadNameDeterminer.PROPOSED);
  }

  protected void addShutdownHook(final ChannelFactory factory) {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override public void run() {
        factory.releaseExternalResources();
        factory.shutdown();
      }
    });
  }

  protected void addShutdownHook(final AbstractNioWorkerPool<?> pool) {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override public void run() {
        pool.releaseExternalResources();
        pool.shutdown();
      }
    });
  }

  protected void addShutdownHook(final AbstractNioBossPool<?> pool) {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override public void run() {
        pool.releaseExternalResources();
        pool.shutdown();
      }
    });
  }

  protected void addShutdownHook(final Bootstrap bootstrap) {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override public void run() {
        bootstrap.releaseExternalResources();
        bootstrap.shutdown();
      }
    });
  }

  @Provides
  @Singleton
  @WorkerPool
  public NioWorkerPool getNioWorkerPool(@PoolExecutor Executor executor, ThreadNameDeterminer nameDeterminer) {
    final NioWorkerPool pool = new NioWorkerPool(executor, numWorkers, nameDeterminer);
    addShutdownHook(pool);
    return pool;
  }

  @Provides
  @Singleton
  @ClientBossPool
  public NioClientBossPool getNioClientBossPool(@PoolExecutor Executor executor, Timer timer, ThreadNameDeterminer nameDeterminer) {
    final NioClientBossPool pool = new NioClientBossPool(executor, numClientBosses, timer, nameDeterminer);
    addShutdownHook(pool);
    return pool;
  }

  @Provides
  @Singleton
  @ServerBossPool
  public NioServerBossPool getNioServerBossPool(@PoolExecutor Executor executor, ThreadNameDeterminer nameDeterminer) {
    final NioServerBossPool pool = new NioServerBossPool(executor, numServerBosses, nameDeterminer);
    addShutdownHook(pool);
    return pool;
  }

  @Provides
  @Singleton
  @DatagramWorkerPool
  public NioDatagramWorkerPool getNioDatagramWorkerPool(@PoolExecutor Executor executor) {
    final NioDatagramWorkerPool pool = new NioDatagramWorkerPool(executor, numWorkers);
    addShutdownHook(pool);
    return pool;
  }

  @Provides
  public NioServerSocketChannelFactory getNioServerSocketChannelFactory(@ServerBossPool NioServerBossPool bossPool,
                                                                        @WorkerPool NioWorkerPool workerPool) {
    final NioServerSocketChannelFactory factory = new NioServerSocketChannelFactory(bossPool, workerPool);
    addShutdownHook(factory);
    return factory;
  }

  @Provides
  public NioClientSocketChannelFactory getNioClientSocketChannelFactory(@ClientBossPool NioClientBossPool bossPool,
                                                                        @WorkerPool NioWorkerPool workerPool) {
    final NioClientSocketChannelFactory factory = new NioClientSocketChannelFactory(bossPool, workerPool);
    addShutdownHook(factory);
    return factory;
  }

  @Provides
  public NioDatagramChannelFactory getNioDatagramChannelFactory(NioDatagramWorkerPool workerPool) {
    final NioDatagramChannelFactory factory = new NioDatagramChannelFactory(workerPool);
    addShutdownHook(factory);
    return factory;
  }

  @Provides
  @Exposed
  public ClientBootstrap getClientBootstrap(NioClientSocketChannelFactory channelFactory) {
    final ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
    addShutdownHook(bootstrap);
    return bootstrap;
  }

  @Provides
  @Exposed
  public ServerBootstrap getServerBootstrap(NioServerSocketChannelFactory channelFactory) {
    final ServerBootstrap bootstrap = new ServerBootstrap(channelFactory);
    addShutdownHook(bootstrap);
    return bootstrap;
  }

  @Provides
  @Exposed
  public ConnectionlessBootstrap getConnectionlessBootstrap(NioDatagramChannelFactory channelFactory) {
    final ConnectionlessBootstrap bootstrap = new ConnectionlessBootstrap(channelFactory);
    addShutdownHook(bootstrap);
    return bootstrap;
  }

}

