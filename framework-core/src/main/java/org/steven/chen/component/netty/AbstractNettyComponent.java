/**
 * Copyright 2019 ChenHao96
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.steven.chen.component.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.springframework.beans.factory.annotation.Autowired;
import org.steven.chen.component.ComponentService;
import org.steven.chen.model.ConfigProperty;

public abstract class AbstractNettyComponent implements ComponentService {

    private boolean start;
    private boolean initialize;
    protected SslContext sslCtx;
    private EventLoopGroup bossGroup;
    private ServerBootstrap bootstrap;
    private EventLoopGroup workerGroup;

    @Autowired(required = false)
    private ConfigProperty configProperty;

    @Override
    public boolean initialized() {
        return this.initialize;
    }

    @Override
    public boolean started() {
        return this.start;
    }

    @Override
    public void initialize() throws Exception {
        if (this.initialize) return;
        this.initialize = false;
        if (this.configProperty == null) {
            this.configProperty = ConfigProperty.getInstance();
        }
        if (this.configProperty.getSocketSsl()) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            this.sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        }
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
        this.bootstrap = new ServerBootstrap();
        this.bootstrap.group(bossGroup, workerGroup);
        this.bootstrap.channel(NioServerSocketChannel.class);
        this.channelInitializer(bootstrap);
        this.initialize = true;
    }

    protected abstract void channelInitializer(ServerBootstrap bootstrap);

    @Override
    public void start() throws Exception {
        if (!this.start) return;
        if (!this.initialize) return;
        this.start = false;
        int port = this.configProperty.getSocketPort();
        this.bootstrap.bind(port).sync();
        this.start = true;
    }

    @Override
    public void stop() throws Exception {
        if (!this.start) return;
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
    }
}
