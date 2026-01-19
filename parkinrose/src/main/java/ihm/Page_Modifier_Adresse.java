package ihm;

import javax.swing.*;
import java.awt.*;
import controleur.ControleurModifierAdresse;
import modele.Usager;
import modele.dao.UsagerDAO;

public class Page_Modifier_Adresse extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;
    private Usager usager;
    private JTextField txtAdresse;
    private JTextField txtCodePostal;
    private JTextField txtVille;
    private JLabel lblZoneAttribuee;
    private JButton btnValider;
    private JButton btnAnnuler;
    
    public Page_Modifier_Adresse(String email) {
        this.emailUtilisateur = email;
        this.usager = UsagerDAO.getUsagerByEmail(email);
        initialisePage();
        chargerAdresseExistante();
        new ControleurModifierAdresse(this);
    }
    
    private void initialisePage() {
        this.setTitle("Modifier mon adresse");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(500, 400);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Titre
        JLabel lblTitre = new JLabel("Mon adresse", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        // Formulaire
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(5, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createTitledBorder("Informations d'adresse"));
        formPanel.setBackground(Color.WHITE);
        
        formPanel.add(new JLabel("Adresse*:"));
        txtAdresse = new JTextField();
        txtAdresse.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(txtAdresse);
        
        formPanel.add(new JLabel("Code postal*:"));
        txtCodePostal = new JTextField();
        txtCodePostal.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(txtCodePostal);
        
        formPanel.add(new JLabel("Ville*:"));
        txtVille = new JTextField();
        txtVille.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(txtVille);
        
        formPanel.add(new JLabel("Zone attribuée:"));
        lblZoneAttribuee = new JLabel("-");
        lblZoneAttribuee.setFont(new Font("Arial", Font.BOLD, 14));
        lblZoneAttribuee.setForeground(new Color(0, 100, 0));
        formPanel.add(lblZoneAttribuee);
        
        // Information
        JLabel lblInfo = new JLabel("* Champs obligatoires pour les abonnements résidentiels");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfo.setForeground(Color.GRAY);
        formPanel.add(lblInfo);
        formPanel.add(new JLabel(""));
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Boutons
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBoutons.setBackground(Color.WHITE);
        
        btnAnnuler = new JButton("Annuler");
        btnValider = new JButton("Enregistrer");
        btnValider.setBackground(new Color(0, 120, 215));
        btnValider.setForeground(Color.WHITE);
        btnValider.setFont(new Font("Arial", Font.BOLD, 14));
        
        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnValider);
        
        mainPanel.add(panelBoutons, BorderLayout.SOUTH);
        
        this.setContentPane(mainPanel);
    }
    
    private void chargerAdresseExistante() {
        if (usager != null) {
           
            String adresseComplete = usager.getNumeroCarteTisseo();
            
            if (adresseComplete != null && !adresseComplete.isEmpty() && !adresseComplete.contains("CARTE")) {

                String[] parties = adresseComplete.split("\\|");
                if (parties.length >= 3) {
                    txtAdresse.setText(parties[0]);
                    txtCodePostal.setText(parties[1]);
                    txtVille.setText(parties[2]);
                }
            }
            
        }
    }
    
    // === GETTERS POUR LE CONTROLEUR ===
    
    public String getEmailUtilisateur() { 
    	return emailUtilisateur;
    }
    public Usager getUsager() { 
    	return usager; 
    }
    public JTextField getTxtAdresse() { 
    	return txtAdresse; 
    }
    public JTextField getTxtCodePostal() { 
    	return txtCodePostal; 
    }
    public JTextField getTxtVille() { 
    	return txtVille; 
    }
    public JLabel getLblZoneAttribuee() { 
    	return lblZoneAttribuee; 
    }
    public JButton getBtnValider() { 
    	return btnValider; 
    }
    public JButton getBtnAnnuler() { 
    	return btnAnnuler; 
    }
}