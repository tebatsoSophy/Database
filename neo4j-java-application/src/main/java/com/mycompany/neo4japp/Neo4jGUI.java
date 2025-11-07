/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */



package com.mycompany.neo4japp;

import org.neo4j.driver.*;
import javax.swing.*;
import java.awt.*;


/**
 *
 * @author Tebatso Mahlathini
 */
public class Neo4jGUI {
    private Driver driver;
    private JTextArea outputArea;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Neo4jGUI().createGUI());
    }

    private void createGUI() {
        JFrame frame = new JFrame("COS326 Practical 10 - Neo4j Database Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);

        // Main panel with nice background
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(240, 245, 250));

        // Output area with better styling - larger for better visibility
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        outputArea.setBackground(new Color(255, 255, 255));
        outputArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setPreferredSize(new Dimension(800, 500));
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150), 1),
            "Query Results",
            0, 0,
            new Font("Arial", Font.BOLD, 12),
            new Color(70, 70, 70)
        ));

        // Buttons with compact styling
        JButton connectBtn = createStyledButton("1. Test Connection", new Color(70, 130, 180));
        JButton countBtn = createStyledButton("2. Count Person Nodes", new Color(60, 179, 113));
        JButton followsBtn = createStyledButton("3. Count Follows", new Color(218, 165, 32));
        JButton chainsBtn = createStyledButton("4. Find FOLLOWS Chains", new Color(186, 85, 211));

        // Button actions
        connectBtn.addActionListener(e -> testConnection());
        countBtn.addActionListener(e -> countPersonNodes());
        followsBtn.addActionListener(e -> countFollowsPerPerson());
        chainsBtn.addActionListener(e -> findFollowsChains());

        // Button panel - compact and centered
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 15, 5));
        buttonPanel.setBackground(new Color(240, 245, 250));
        buttonPanel.add(connectBtn);
        buttonPanel.add(countBtn);
        buttonPanel.add(followsBtn);
        buttonPanel.add(chainsBtn);

        // Header label
        JLabel headerLabel = new JLabel("Neo4j Database Management System - COS326 Practical 10", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerLabel.setForeground(new Color(50, 50, 120));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

        // Layout
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);

        // Initialize connection
        connectToDatabase();
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }

    private void connectToDatabase() {
        try {
            driver = GraphDatabase.driver("bolt://localhost:7687", 
                                         AuthTokens.basic("neo4j", "12345678"));
            outputArea.setText("🚀 Driver initialized successfully!\n\n" +
                             "Click 'Test Connection' to verify database connection.\n" +
                             "Ready to execute queries...\n");
        } catch (Exception e) {
            outputArea.setText("❌ Failed to initialize driver: " + e.getMessage() + "\n");
        }
    }

    private void testConnection() {
        try (Session session = driver.session()) {
            String result = session.run("RETURN 'Successfully connected to Neo4j database!' AS message")
                    .single().get("message").asString();
            outputArea.setText("✅ " + result + "\n\n" +
                             "Database: Pract10Neo4jB\n" +
                             "Status: Connected and Ready\n");
        } catch (Exception e) {
            outputArea.setText("❌ Connection failed: " + e.getMessage() + "\n");
        }
    }

    private void countPersonNodes() {
        try (Session session = driver.session()) {
            long count = session.run("MATCH (p:Person) RETURN count(p) AS count")
                    .single().get("count").asLong();
            outputArea.setText("👥 PERSON NODE COUNT\n" +
                             "===================\n\n" +
                             "Total person nodes in the network: " + count + "\n\n" +
                             "All Person nodes have been successfully loaded from the database.");
        } catch (Exception e) {
            outputArea.setText("❌ Error counting nodes: " + e.getMessage() + "\n");
        }
    }

    private void countFollowsPerPerson() {
        String personName = JOptionPane.showInputDialog(
            null, 
            "Enter a person's name to see who they follow:\n(Leave blank to see Top 100 by follow count)",
            "Count Follows - Search Options", 
            JOptionPane.QUESTION_MESSAGE
        );

        try (Session session = driver.session()) {
            StringBuilder sb = new StringBuilder();
            
            if (personName != null && !personName.trim().isEmpty()) {
                // Search for specific person
                Result result = session.run(
                    "MATCH (p:Person {name: $name})-[:FOLLOWS]->(f:Person) " +
                    "RETURN p.name AS person, count(f) AS followCount, " +
                    "collect(f.name) AS followedPersons",
                    Values.parameters("name", personName.trim())
                );

                if (result.hasNext()) {
                    var record = result.next();
                    String name = record.get("person").asString();
                    int count = record.get("followCount").asInt();
                    java.util.List<String> followed = record.get("followedPersons").asList(Values.ofString());
                    
                    sb.append("🔍 PERSON FOLLOWS ANALYSIS\n");
                    sb.append("=========================\n\n");
                    sb.append("Person: ").append(name).append("\n");
                    sb.append("Total follows: ").append(count).append("\n\n");
                    sb.append("People followed by ").append(name).append(":\n");
                    sb.append("----------------------------------------\n");
                    
                    for (String followedPerson : followed) {
                        sb.append("  • ").append(followedPerson).append("\n");
                    }
                } else {
                    sb.append("❌ No person found with name: '").append(personName).append("'\n");
                }
            } else {
                // Show top 100 by follow count
                Result result = session.run(
                    "MATCH (p:Person)-[:FOLLOWS]->(f:Person) " +
                    "RETURN p.name AS person, count(f) AS followsCount " +
                    "ORDER BY followsCount DESC " +
                    "LIMIT 100"
                );

                sb.append("🏆 TOP 100 PERSONS BY FOLLOW COUNT\n");
                sb.append("==================================\n\n");
                
                int rank = 1;
                while (result.hasNext()) {
                    var record = result.next();
                    sb.append(String.format("%3d. %-40s → %4d follows\n", 
                        rank++,
                        record.get("person").asString(),
                        record.get("followsCount").asInt()));
                }
            }
            outputArea.setText(sb.toString());
        } catch (Exception e) {
            outputArea.setText("❌ Error counting follows: " + e.getMessage() + "\n");
        }
    }

    private void findFollowsChains() {
        try (Session session = driver.session()) {
            Result result = session.run(
                "MATCH path = (p1:Person)-[:FOLLOWS]->(p2:Person)-[:FOLLOWS]->(p3:Person) " +
                "RETURN p1.name AS person1, p2.name AS person2, p3.name AS person3 " +
                "ORDER BY person1, person2, person3 " +
                "LIMIT 100"
            );

            StringBuilder sb = new StringBuilder();
            sb.append("🔄 FOLLOWS CHAINS: (person1)-[:FOLLOWS]->(person2)-[:FOLLOWS]->(person3)\n");
            sb.append("=========================================================================\n\n");
            
            int count = 0;
            while (result.hasNext()) {
                var record = result.next();
                count++;
                String person1 = record.get("person1").asString();
                String person2 = record.get("person2").asString();
                String person3 = record.get("person3").asString();
                
                sb.append(String.format("Chain %2d:\n", count));
                sb.append(String.format("  (%s)-[:FOLLOWS]->(%s)-[:FOLLOWS]->(%s)\n", 
                    person1, person2, person3));
                sb.append(String.format("  %s → %s → %s\n\n", 
                    person1, person2, person3));
            }
            
            sb.append("=".repeat(70)).append("\n");
            sb.append("📊 Total chains found: ").append(count).append("\n");
            outputArea.setText(sb.toString());
        } catch (Exception e) {
            outputArea.setText("❌ Error finding chains: " + e.getMessage() + "\n");
        }
    }
}