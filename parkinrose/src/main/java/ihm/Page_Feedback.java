package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Page_Feedback extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;
    
    public Page_Feedback(String email) {
        this.emailUtilisateur = email;
        initialiserPage();
    }
    
    private void initialiserPage() {
        setTitle("Feedback - ParkinRose");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(mainPanel);
        
        // Titre
        JLabel lblTitle = new JLabel("Donnez votre avis", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(lblTitle, BorderLayout.NORTH);
        
        // Panel central
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        
        // Type de feedback
        JPanel panelType = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelType.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblType = new JLabel("Type : ");
        JComboBox<String> comboType = new JComboBox<>(new String[]{
            "Suggestion", "Bug", "Question", "Autre"
        });
        panelType.add(lblType);
        panelType.add(comboType);
        centerPanel.add(panelType);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Note
        JPanel panelNote = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblNote = new JLabel("Note : ");
        JComboBox<String> comboNote = new JComboBox<>(new String[]{
            "★★★★★ Excellent", 
            "★★★★☆ Très bon", 
            "★★★☆☆ Correct", 
            "★★☆☆☆ Passable", 
            "★☆☆☆☆ Médiocre"
        });
        panelNote.add(lblNote);
        panelNote.add(comboNote);
        centerPanel.add(panelNote);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Message
        JPanel panelMessage = new JPanel(new BorderLayout());
        panelMessage.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelMessage.setMaximumSize(new Dimension(400, 150));
        
        JLabel lblMessage = new JLabel("Votre message :");
        lblMessage.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        panelMessage.add(lblMessage, BorderLayout.NORTH);
        
        JTextArea txtMessage = new JTextArea(5, 30);
        txtMessage.setLineWrap(true);
        txtMessage.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(txtMessage);
        panelMessage.add(scrollPane, BorderLayout.CENTER);
        
        centerPanel.add(panelMessage);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Boutons
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        
        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.addActionListener(e -> dispose());
        
        JButton btnEnvoyer = new JButton("Envoyer");
        btnEnvoyer.setBackground(new Color(70, 130, 180));
        btnEnvoyer.setForeground(Color.WHITE);
        
        btnEnvoyer.addActionListener(e -> {
            String type = (String) comboType.getSelectedItem();
            String note = (String) comboNote.getSelectedItem();
            String message = txtMessage.getText().trim();
            
            if (message.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Veuillez saisir un message.", 
                    "Message vide", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            //simulation
            JOptionPane.showMessageDialog(this, 
                "Merci pour votre feedback !\nVotre message a été envoyé.", 
                "Succès", 
                JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
        });
        
        panelButtons.add(btnAnnuler);
        panelButtons.add(btnEnvoyer);
        mainPanel.add(panelButtons, BorderLayout.SOUTH);
    }
    
    // Pour tester
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Page_Feedback frame = new Page_Feedback("test@example.com");
            frame.setVisible(true);
        });
    }
}