package com.quizweb.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Cấu hình Simple In-Memory Broker:
     * - /topic : kênh broadcast (server → nhiều client đang subscribe).
     * - /app   : prefix để gửi message từ client lên server (nếu cần @MessageMapping).
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Đăng ký STOMP endpoint tại /ws với SockJS fallback.
     * Vite dev-proxy cùng origin → setAllowedOriginPatterns("*") an toàn cho môi trường dev.
     * Production: thay bằng domain cụ thể (ví dụ: "https://yourdomain.com").
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
