package ihm;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import modele.VehiculeUsager;
import modele.dao.VehiculeUsagerDAO;
import modele.dao.UsagerDAO;
import controleur.ControleurGestionVehicules;

public class Page_Gestion_Vehicules extends JFrame {
    public String emailUtilisateur; 
    public JList<VehiculeUsager> listVehicules; 
    public DefaultListModel<VehiculeUsager> listModel;
    public JButton btnAjouter, btnSupprimer, btnDefinirPrincipal;
    private ControleurGestionVehicules controleur;

    public Page_Gestion_Vehicules(String email) {
        this.emailUtilisateur = email;
        initialisePage();
        this.controleur = new ControleurGestionVehicules(this);
    }

    private void initialisePage() {
        setTitle("Mes Véhicules");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Titre
        JLabel lblTitre = new JLabel("Mes Véhicules", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 16));
        contentPanel.add(lblTitre, BorderLayout.NORTH);

        // Liste des véhicules
        listModel = new DefaultListModel<>();
        listVehicules = new JList<>(listModel);
        listVehicules.setCellRenderer(new VehiculeRenderer());
        JScrollPane scrollPane = new JScrollPane(listVehicules);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel boutons
        JPanel panelBoutons = new JPanel(new FlowLayout());
        btnAjouter = new JButton("Ajouter un véhicule");
        btnSupprimer = new JButton("Supprimer");
        btnDefinirPrincipal = new JButton("Définir comme principal");

        // Les ActionListeners seront configurés par le contrôleur
        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnSupprimer);
        panelBoutons.add(btnDefinirPrincipal);

        contentPanel.add(panelBoutons, BorderLayout.SOUTH);

        setContentPane(contentPanel);
    }


    private class VehiculeRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                     int index, boolean isSelected, 
                                                     boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            VehiculeUsager vehicule = (VehiculeUsager) value;
            
            String texte = vehicule.getPlaqueImmatriculation() + " - " + vehicule.getTypeVehicule();
            if (vehicule.getMarque() != null && !vehicule.getMarque().isEmpty()) {
                texte += " " + vehicule.getMarque();
            }
            if (vehicule.getModele() != null && !vehicule.getModele().isEmpty()) {
                texte += " " + vehicule.getModele();
            }
            if (vehicule.isEstPrincipal()) {
                texte += " ★";
                setFont(getFont().deriveFont(Font.BOLD));
            }
            
            setText(texte);
            return this;
        }
    }
}