package com.cuhlippa.ui.discovery;

import com.cuhlippa.client.discovery.DiscoveredServer;
import com.cuhlippa.client.discovery.DiscoveryListener;
import com.cuhlippa.client.discovery.NetworkDiscoveryService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class DeviceDiscoveryDialog extends JDialog implements DiscoveryListener {
    
    private final transient NetworkDiscoveryService discoveryService;
    private final JTable serverTable;
    private final DiscoveredServerTableModel tableModel;
    private final JButton discoverButton;
    private final JButton connectButton;
    private final JButton cancelButton;
    private final JButton refreshButton;
    private final JLabel statusLabel;
    private final JProgressBar progressBar;
    
    private transient DiscoveredServer selectedServer;
    private boolean dialogResult = false;
    
    public DeviceDiscoveryDialog(Frame parent) {
        super(parent, "Find Other Computers", true);
        
        this.discoveryService = new NetworkDiscoveryService();
        this.tableModel = new DiscoveredServerTableModel();
        
        // Initialize components
        this.serverTable = createServerTable();
        this.discoverButton = new JButton("🔍 Search for Computers");
        this.connectButton = new JButton("✓ Connect to Selected Computer");
        this.cancelButton = new JButton("✕ Cancel");
        this.refreshButton = new JButton("🔄 Refresh");
        this.statusLabel = new JLabel("Click 'Search for Computers' to find available computers");
        this.progressBar = new JProgressBar();
        
        setupUI();
        setupEventHandlers();
        
        // Register for discovery events
        discoveryService.addDiscoveryListener(this);
        
        // Set dialog properties
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(parent);
    }
    
    private JTable createServerTable() {
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(200); // Server Name
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // IP Address
        table.getColumnModel().getColumn(2).setPreferredWidth(80);  // Port
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Devices
        table.getColumnModel().getColumn(4).setPreferredWidth(80);  // Status
        
        // Custom renderer for status column
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());
        
        return table;
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        JLabel titleLabel = new JLabel("🌐 Find Computers to Share Clipboard With");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        // Server list panel
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Available Computers"));
        
        JScrollPane scrollPane = new JScrollPane(serverTable);
        listPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(listPanel, BorderLayout.CENTER);
        
        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        statusPanel.add(progressBar, BorderLayout.EAST);
        
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        // Update button states
        connectButton.setEnabled(false);
        refreshButton.setEnabled(false);
        
        buttonPanel.add(discoverButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(connectButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // Discover button
        discoverButton.addActionListener(e -> {
            if (discoveryService.isDiscovering()) {
                stopDiscovery();
            } else {
                startDiscovery();
            }
        });
        
        // Refresh button
        refreshButton.addActionListener(e -> {
            if (discoveryService.isDiscovering()) {
                discoveryService.refreshDiscovery();
                updateStatus("Refreshing search...");
            }
        });
        
        // Connect button
        connectButton.addActionListener(e -> {
            int selectedRow = serverTable.getSelectedRow();
            if (selectedRow >= 0) {
                selectedServer = tableModel.getServerAt(selectedRow);
                dialogResult = true;
                dispose();
            }
        });
        
        // Cancel button
        cancelButton.addActionListener(e -> {
            dialogResult = false;
            dispose();
        });
        
        // Table selection
        serverTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = serverTable.getSelectedRow();
                connectButton.setEnabled(selectedRow >= 0);
            }
        });
        
        // Double-click to connect
        serverTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = serverTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        serverTable.setRowSelectionInterval(row, row);
                        selectedServer = tableModel.getServerAt(row);
                        dialogResult = true;
                        dispose();
                    }
                }
            }
        });
        
        // Window closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cleanup();
            }
        });
    }
    
    private void startDiscovery() {
        try {
            tableModel.clearServers();
            discoveryService.startDiscovery();
            
            discoverButton.setText("⏹ Stop Discovery");
            refreshButton.setEnabled(true);
            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);
            updateStatus("Scanning network for other computers...");
            
        } catch (Exception e) {
            updateStatus("Failed to start discovery: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Failed to start network discovery:\n" + e.getMessage(),
                "Discovery Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void stopDiscovery() {
        discoveryService.stopDiscovery();
        
        discoverButton.setText("🔍 Search for Computers");
        refreshButton.setEnabled(false);
        progressBar.setVisible(false);
        
        List<DiscoveredServer> servers = tableModel.getServers();
        if (servers.isEmpty()) {
            updateStatus("No computers found. Try manual entry if needed.");
        } else {
            updateStatus("Found " + servers.size() + " computer(s). Select one to connect.");
        }
    }
    
    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
        });
    }
    
    private void cleanup() {
        if (discoveryService.isDiscovering()) {
            discoveryService.stopDiscovery();
        }
        discoveryService.shutdown();
    }
    
    // DiscoveryListener implementation
    @Override
    public void onServerDiscovered(DiscoveredServer server) {
        SwingUtilities.invokeLater(() -> {
            tableModel.addServer(server);
            updateStatus("Found " + tableModel.getRowCount() + " computer(s). Select one to connect.");
        });
    }
    
    @Override
    public void onServerLost(DiscoveredServer server) {
        SwingUtilities.invokeLater(() -> {
            tableModel.removeServer(server);
            updateStatus("Found " + tableModel.getRowCount() + " computer(s). Select one to connect.");
        });
    }
    
    @Override
    public void onDiscoveryStatusChanged(boolean active) {
        SwingUtilities.invokeLater(() -> {
            if (!active && discoveryService != null) {
                // Discovery stopped
                progressBar.setVisible(false);
                discoverButton.setText("🔍 Search for Computers");
                refreshButton.setEnabled(false);
            }
        });
    }
    
    // Public API
    public boolean showDialog() {
        setVisible(true);
        return dialogResult;
    }
    
    public DiscoveredServer getSelectedServer() {
        return selectedServer;
    }
    
    @Override
    public void dispose() {
        cleanup();
        super.dispose();
    }
    
    // Custom cell renderer for status column
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof String) {
                String status = (String) value;
                if ("Online".equals(status)) {
                    setForeground(isSelected ? Color.WHITE : new Color(0, 128, 0));
                    setText("🟢 Online");
                } else {
                    setForeground(isSelected ? Color.WHITE : Color.GRAY);
                    setText("🔴 " + status);
                }
            }
            
            return this;
        }
    }
}
