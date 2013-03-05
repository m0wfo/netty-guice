package org.robotninjas.netty.guice.oio;

import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioDatagramChannelFactory;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.jboss.netty.util.internal.ExecutorUtil;
import org.robotninjas.netty.guice.annotations.ClientChannelFactory;
import org.robotninjas.netty.guice.annotations.DatagramChannelFactory;
import org.robotninjas.netty.guice.annotations.ServerChannelFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NettyOioModule extends PrivateModule {
  @Override protected void configure() {

  }

  protected ExecutorService addShutdownHook(final ExecutorService e) {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override public void run() {
        ExecutorUtil.terminate(e);
      }
    });
    return e;
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

  @Provides
  @Singleton
  @WorkerExecutor
  public Executor getWorkerExecutor() {
    return addShutdownHook(Executors.newCachedThreadPool());
  }

  @Provides
  @Singleton
  @BossExecutor
  public Executor getBossExecutor() {
    return addShutdownHook(Executors.newCachedThreadPool());
  }

  @Provides
  @Singleton
  @DatagramChannelFactory
  public OioDatagramChannelFactory getDatagramChannelFactory(@WorkerExecutor Executor executor) {
    return addShutdownHook(new OioDatagramChannelFactory(executor));
  }

  @Provides
  @Singleton
  @ClientChannelFactory
  public OioServerSocketChannelFactory getServerSocketChannelFactory(@BossExecutor Executor boss, @WorkerExecutor Executor worker) {
    return addShutdownHook(new OioServerSocketChannelFactory(boss, worker));
  }

  @Provides
  @Singleton
  @ServerChannelFactory
  public OioClientSocketChannelFactory getClientSocketChannelFactory(@WorkerExecutor Executor executor) {
    return addShutdownHook(new OioClientSocketChannelFactory(executor));
  }

  @Provides
  public ClientBootstrap getClientBootstrap(@DatagramChannelFactory OioClientSocketChannelFactory factory) {
    return new ClientBootstrap(factory);
  }

  @Provides
  public ServerBootstrap getServerBootstrap(@ClientChannelFactory OioServerSocketChannelFactory factory) {
    return new ServerBootstrap(factory);
  }

  @Provides
  public ConnectionlessBootstrap getConnectionlessBootstrap(@ServerChannelFactory OioDatagramChannelFactory factory) {
    return new ConnectionlessBootstrap(factory);
  }
}
