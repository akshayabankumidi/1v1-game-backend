package com.example._v1.mcq.game.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String username = request.getURI().getQuery().split("username=")[1].split("&")[0];
        log.info("username: "+username);
        // If username is null or empty, generate a unique ID
        if (username == null || username.isEmpty()) {
            username = "user-" + UUID.randomUUID().toString();
        }

        return new StompPrincipal(username);
    }
}

class StompPrincipal implements Principal {
    private final String name;

    public StompPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}