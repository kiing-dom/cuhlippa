package com.cuhlippa.server.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ClipboardSyncHandler clipboardSyncHandler;

    public WebSocketConfig(ClipboardSyncHandler clipboardSyncHandler) {
        this.clipboardSyncHandler = clipboardSyncHandler;
    }

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(clipboardSyncHandler, "/sync")
                .setAllowedOrigins("*"); // In production, restrict this
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();

        // Set message size limits to 20MB to handle large images
        container.setMaxTextMessageBufferSize(20 * 1024 * 1024); // 20MB
        container.setMaxBinaryMessageBufferSize(20 * 1024 * 1024); // 20MB
        container.setMaxSessionIdleTimeout(300000L); // 5 minutes

        System.out.println("✅ WebSocket container configured with 20MB message buffer limits");

        return container;
    }
}
