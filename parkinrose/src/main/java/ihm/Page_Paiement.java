@ -31,16 +31,10 @@ public class Page_Paiement extends JFrame {
    private JTextField txtDateExpiration;
    private JTextField txtCVV;
    
    // Constructeur pour la voirie
    public Page_Paiement(double montant, String emailUtilisateur, String typeVehicule, 
                        String plaqueImmatriculation, String idZone, String nomZone, 
                        int dureeHeures, int dureeMinutes) {
        this.montant = montant;
        this.emailUtilisateur = emailUtilisateur;
        this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
@ -50,8 +44,26 @@ public class Page_Paiement extends JFrame {
        this.nomZone = nomZone;
        this.dureeHeures = dureeHeures;
        this.dureeMinutes = dureeMinutes;
        this.idStationnement = null;
        this.heureDepart = null;
        this.controleur = new PaiementControleur(emailUtilisateur);
        initialisePage();
    }
    
    // Constructeur pour les parkings
    public Page_Paiement(double montant, String emailUtilisateur, Integer idStationnement, 
                        LocalDateTime heureDepart) {
        this.montant = montant;
        this.emailUtilisateur = emailUtilisateur;
        this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        this.idStationnement = idStationnement;
        this.heureDepart = heureDepart;
        this.typeVehicule = "";
        this.plaqueImmatriculation = "";
        this.idZone = "";
        this.nomZone = "";
        this.dureeHeures = 0;
        this.dureeMinutes = 0;
        this.controleur = new PaiementControleur(emailUtilisateur);
        initialisePage();
    }
@ -59,91 +71,131 @@ public class Page_Paiement extends JFrame {
    private void initialisePage() {
        this.setTitle("Paiement du stationnement");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(600, 700);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // ========== TITRE ==========
        JLabel lblTitre = new JLabel("Paiement du stationnement", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitre.setForeground(new Color(0, 102, 204));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        // ========== PANEL CENTRAL ==========
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        
        // ========== SECTION RÉCAPITULATIF ==========
        JPanel panelRecap = new JPanel();
        panelRecap.setBackground(Color.WHITE);
        panelRecap.setLayout(new GridLayout(0, 1, 5, 5));
        panelRecap.setBorder(BorderFactory.createTitledBorder("Récapitulatif"));
        
        JLabel lblMontant = new JLabel("Montant à payer: " + String.format("%.2f", montant) + " €");
        lblMontant.setFont(new Font("Arial", Font.BOLD, 16));
        lblMontant.setForeground(new Color(0, 100, 0));
        panelRecap.add(lblMontant);
        
        panelRecap.add(new JLabel(" ")); // Espacement
        
        String typeStationnement = (idStationnement == null) ? "Voirie" : "Parking";
        JLabel lblType = new JLabel("Type: " + typeStationnement);
        panelRecap.add(lblType);
        
        if (typeVehicule != null && !typeVehicule.isEmpty() && 
            plaqueImmatriculation != null && !plaqueImmatriculation.isEmpty()) {
            JLabel lblVehicule = new JLabel("Véhicule: " + typeVehicule + " - " + plaqueImmatriculation);
            panelRecap.add(lblVehicule);
        }
        
        if (nomZone != null && !nomZone.isEmpty()) {
            JLabel lblZone = new JLabel("Zone: " + nomZone);
            panelRecap.add(lblZone);
        }
        
        if (idStationnement == null) {
            JLabel lblDuree = new JLabel("Durée: " + dureeHeures + "h" + dureeMinutes + "min");
            panelRecap.add(lblDuree);
        } else if (heureDepart != null) {
            JLabel lblDepart = new JLabel("Heure de départ: " + 
                heureDepart.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            panelRecap.add(lblDepart);
        }
        
        centerPanel.add(panelRecap);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // ========== SECTION FORMULAIRE PAIEMENT ==========
        JPanel panelForm = new JPanel();
        panelForm.setBackground(Color.WHITE);
        panelForm.setLayout(new GridLayout(0, 2, 10, 10));
        panelForm.setBorder(BorderFactory.createTitledBorder("Informations de paiement"));
        
        panelForm.add(new JLabel("Nom sur la carte:"));
        txtNomCarte = new JTextField();
        panelForm.add(txtNomCarte);
        
        panelForm.add(new JLabel("Numéro de carte:"));
        txtNumeroCarte = new JTextField();
        panelForm.add(txtNumeroCarte);
        
        panelForm.add(new JLabel("Date expiration (MM/AA):"));
        txtDateExpiration = new JTextField();
        panelForm.add(txtDateExpiration);
        
        panelForm.add(new JLabel("Cryptogramme (CVV):"));
        txtCVV = new JTextField();
        panelForm.add(txtCVV);
        
        centerPanel.add(panelForm);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // ========== SECTION INFORMATIONS ==========
        JPanel panelInfo = new JPanel();
        panelInfo.setBackground(new Color(240, 248, 255));
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setBorder(BorderFactory.createTitledBorder("Informations importantes"));
        
        JLabel info1 = new JLabel("• Votre paiement est sécurisé par cryptage SSL");
        JLabel info2 = new JLabel("• Les données de carte ne sont pas stockées");
        JLabel info3 = new JLabel("• Le stationnement sera activé après paiement");
        
        info1.setFont(new Font("Arial", Font.PLAIN, 12));
        info2.setFont(new Font("Arial", Font.PLAIN, 12));
        info3.setFont(new Font("Arial", Font.PLAIN, 12));
        
        panelInfo.add(info1);
        panelInfo.add(info2);
        panelInfo.add(info3);
        
        centerPanel.add(panelInfo);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // ========== SECTION BOUTONS ==========
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBoutons.setBackground(Color.WHITE);
        
        JButton btnAnnuler = new JButton("Annuler");
        JButton btnPayer = new JButton("Payer " + String.format("%.2f €", montant));
        
        // Style des boutons
        btnAnnuler.setBackground(new Color(220, 53, 69));
        btnAnnuler.setForeground(Color.WHITE);
        btnAnnuler.setFocusPainted(false);
        
        btnPayer.setBackground(new Color(40, 167, 69));
        btnPayer.setForeground(Color.WHITE);
        btnPayer.setFont(new Font("Arial", Font.BOLD, 14));
        btnPayer.setFocusPainted(false);
        
        // Actions des boutons
        btnAnnuler.addActionListener(e -> annuler());
        btnPayer.addActionListener(e -> traiterPaiement());
        
@ -157,9 +209,11 @@ public class Page_Paiement extends JFrame {
    
    private void annuler() {
        int confirmation = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir annuler le paiement ?\n" +
            "Le stationnement ne sera pas activé.",
            "Confirmation d'annulation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (confirmation == JOptionPane.YES_OPTION) {
            this.dispose();
@ -167,8 +221,6 @@ public class Page_Paiement extends JFrame {
    }
    
    private boolean validerFormulaire() {
        return controleur.validerFormulairePaiementComplet(
            txtNomCarte.getText().trim(),
            txtNumeroCarte.getText().trim(),
@ -186,6 +238,7 @@ public class Page_Paiement extends JFrame {
        boolean succes = false;
        
        if (idStationnement == null) {
            // Paiement pour la voirie
            succes = controleur.traiterPaiementVoirie(
                txtNomCarte.getText().trim(),
                txtNumeroCarte.getText().trim(),
@ -200,6 +253,7 @@ public class Page_Paiement extends JFrame {
                this
            );
        } else {
            // Paiement pour parking
            succes = controleur.traiterPaiementParking(
                txtNomCarte.getText().trim(),
                txtNumeroCarte.getText().trim(),
@ -207,21 +261,57 @@ public class Page_Paiement extends JFrame {
                txtCVV.getText().trim(),
                montant,
                idStationnement,
                heureDepart != null ? heureDepart : LocalDateTime.now(),
                this
            );
        }
        
        if (succes) {
            afficherConfirmation();
        }
    }
    
    private void afficherConfirmation() {
        String message = "<html><div style='text-align: center;'>" +
                        "<h3 style='color: #006400;'>Paiement réussi !</h3>" +
                        "<p><b>Montant:</b> " + String.format("%.2f €", montant) + "</p>";
        
        if (idStationnement == null) {
            message += "<p><b>Type:</b> Stationnement voirie</p>" +
                      "<p><b>Véhicule:</b> " + plaqueImmatriculation + "</p>" +
                      "<p><b>Zone:</b> " + nomZone + "</p>" +
                      "<p><b>Durée:</b> " + dureeHeures + "h" + dureeMinutes + "min</p>" +
                      "<p>Votre stationnement est maintenant actif.</p>";
        } else {
            message += "<p><b>Type:</b> Stationnement parking</p>" +
                      "<p>Votre stationnement a été terminé avec succès.</p>";
        }
        
        message += "</div></html>";
        
        JOptionPane.showMessageDialog(this,
            message,
            "Paiement confirmé",
            JOptionPane.INFORMATION_MESSAGE);
            
        this.dispose();
    }
    
    // Méthode main pour tester
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Test avec la voirie
            Page_Paiement page = new Page_Paiement(
                12.50, 
                "test@example.com", 
                "Voiture", 
                "AB-123-CD", 
                "Z001", 
                "Zone Centrale", 
                2, 
                30
            );
            page.setVisible(true);
        });
    }
}