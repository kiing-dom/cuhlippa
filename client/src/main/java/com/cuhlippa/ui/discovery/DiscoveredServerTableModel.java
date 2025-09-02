package com.cuhlippa.ui.discovery;

import com.cuhlippa.client.discovery.DiscoveredServer;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class DiscoveredServerTableModel extends AbstractTableModel {
    
    private static final String[] COLUMN_NAMES = {
        "Server Name", "IP Address", "Port", "Devices", "Status"
    };
    
    private static final Class<?>[] COLUMN_CLASSES = {
        String.class, String.class, Integer.class, Integer.class, String.class
    };
    
    private final List<DiscoveredServer> servers;
    
    public DiscoveredServerTableModel() {
        this.servers = new ArrayList<>();
    }
    
    @Override
    public int getRowCount() {
        return servers.size();
    }
    
    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return COLUMN_CLASSES[columnIndex];
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // Read-only table
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= servers.size()) {
            return null;
        }
        
        DiscoveredServer server = servers.get(rowIndex);
        
        switch (columnIndex) {
            case 0: // Server Name
                return server.getServerName();
            case 1: // IP Address
                return server.getServerIP();
            case 2: // Port
                return server.getServerPort();
            case 3: // Devices
                return server.getConnectedDevices();
            case 4: // Status
                return server.isOnline() ? "Online" : "Offline";
            default:
                return null;
        }
    }
    
    // Table management methods
    public void addServer(DiscoveredServer server) {
        // Check if server already exists (by server ID)
        for (int i = 0; i < servers.size(); i++) {
            if (servers.get(i).getServerId().equals(server.getServerId())) {
                // Update existing server
                servers.set(i, server);
                fireTableRowsUpdated(i, i);
                return;
            }
        }
        
        // Add new server
        servers.add(server);
        int newRow = servers.size() - 1;
        fireTableRowsInserted(newRow, newRow);
    }
    
    public void removeServer(DiscoveredServer server) {
        for (int i = 0; i < servers.size(); i++) {
            if (servers.get(i).getServerId().equals(server.getServerId())) {
                servers.remove(i);
                fireTableRowsDeleted(i, i);
                return;
            }
        }
    }
    
    public void updateServer(DiscoveredServer server) {
        for (int i = 0; i < servers.size(); i++) {
            if (servers.get(i).getServerId().equals(server.getServerId())) {
                servers.set(i, server);
                fireTableRowsUpdated(i, i);
                return;
            }
        }
    }
    
    public void clearServers() {
        int size = servers.size();
        if (size > 0) {
            servers.clear();
            fireTableRowsDeleted(0, size - 1);
        }
    }
    
    public DiscoveredServer getServerAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= servers.size()) {
            return null;
        }
        return servers.get(rowIndex);
    }
    
    public List<DiscoveredServer> getServers() {
        return new ArrayList<>(servers);
    }
    
    public boolean isEmpty() {
        return servers.isEmpty();
    }
    
    // Helper methods for UI updates
    public void refreshServerStatus() {
        // Mark servers as offline if they haven't been seen recently
        long currentTime = System.currentTimeMillis();
        boolean hasChanges = false;
        
        for (DiscoveredServer server : servers) {
            long timeSinceLastSeen = currentTime - server.getLastSeen();
            boolean shouldBeOnline = timeSinceLastSeen < 15000; // 15 seconds timeout
            
            if (server.isOnline() != shouldBeOnline) {
                server.setOnline(shouldBeOnline);
                hasChanges = true;
            }
        }
        
        if (hasChanges) {
            fireTableDataChanged();
        }
    }
    
    public int getOnlineServerCount() {
        return (int) servers.stream().mapToInt(s -> s.isOnline() ? 1 : 0).sum();
    }
    
    public int getTotalServerCount() {
        return servers.size();
    }
}
