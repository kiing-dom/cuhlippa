package com.cuhlippa.server.config;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket handler for clipboard synchronization
 */
@Component
public class ClipboardSyncHandler extends TextWebSocketHandler {
    
    private final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        sessions.add(session);
        userSessions.computeIfAbsent("default", k -> new CopyOnWriteArraySet<>()).add(session);
        System.out.println("Client connected: " + session.getId() + " (Total: " + sessions.size() + ")");
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        System.out.println("Received message from " + session.getId() + ": " + message.getPayload().substring(0, Math.min(100, message.getPayload().length())) + "...");
        
        // Broadcast to all other sessions in the same user group
        String userGroup = "default"; // In real implementation, get from session
        CopyOnWriteArraySet<WebSocketSession> groupSessions = userSessions.get(userGroup);
        
        if (groupSessions != null) {
            for (WebSocketSession otherSession : groupSessions) {
                if (otherSession.isOpen() && !otherSession.getId().equals(session.getId())) {
                    try {
                        otherSession.sendMessage(message);
                        System.out.println("Forwarded message to: " + otherSession.getId());
                    } catch (Exception e) {
                        System.err.println("Failed to send message to session " + otherSession.getId() + ": " + e.getMessage());
                        // Remove dead session
                        groupSessions.remove(otherSession);
                        sessions.remove(otherSession);
                    }
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("Client disconnected: " + session.getId() + " (Total: " + sessions.size() + ")");
        userSessions.values().forEach(groupSessions -> groupSessions.remove(session));
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) throws Exception {
        System.err.println("Transport error for session " + session.getId() + ": " + exception.getMessage());
        sessions.remove(session);
        userSessions.values().forEach(groupSessions -> groupSessions.remove(session));
    }

    /**
     * Get number of connected clients
     */
    public int getConnectedClientCount() {
        return sessions.size();
    }
}
