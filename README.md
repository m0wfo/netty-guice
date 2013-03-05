A Guice module that creates and exposes ClientBootstrap, ServerBootstrap, and ConnectionlessBootstrap. All pools and channel factories are shared between created bootstraps; shutdown hooks are registered.

Get It
------

```xml
<repository>
    <snapshots>
        <enabled>false</enabled>
    </snapshots>
    <id>central</id>
    <name>bintray</name>
    <url>http://dl.bintray.com/content/mgodave/robotninjas</url>
</repository>

<dependency>
    <groupId>org.robotninjas.netty</groupId>
    <artifactId>netty-guice</artifactId>
    <version>3.0</version>
</dependency>
```