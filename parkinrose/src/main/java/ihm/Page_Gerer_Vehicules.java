package ihm;

import javax.swing.*;
import modele.Vehicule;
import modele.dao.VehiculeDAO;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class Page_Gerer_Vehicules extends JDialog {
    private String emailUtilisateur;
    private int idUsager;
    private DefaultListModel<Vehicule> listModel;
    private JList<Vehicule> listVehicules;
    
    public Page_Gerer_Vehicules(JFrame parent, String email) {
        super(parent, "Gérer mes véhicules", true);
        this.emailUtilisateur = email;
        this.idUsager = modele.dao.UsagerDAO.getUsagerByEmail(email).getIdUsager();
        initialiserDialog();
    }
    
    private void initialiserDialog() {
        setSize(400, 400);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Titre
        JLabel lblTitre = new JLabel("Mes véhicules enregistrés");
        lblTitre.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        // Liste des véhicules
        listModel = new DefaultListModel<>();
        chargerVehicules();
        
        listVehicules = new JList<>(listModel);
        listVehicules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listVehicules.setCellRenderer(new VehiculeRenderer());
        
        JScrollPane scrollPane = new JScrollPane(listVehicules);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel des boutons
        JPanel panelBoutons = new JPanel(new GridLayout(1, 4, 5, 5));
        
        JButton btnAjouter = new JButton("Ajouter");
        btnAjouter.addActionListener(e -> ajouterVehicule());
        
        JButton btnDefinirPrincipal = new JButton("Définir principal");
        btnDefinirPrincipal.addActionListener(e -> {
			try {
				definirPrincipal();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
        
        JButton btnSupprimer = new JButton("Supprimer");
        btnSupprimer.addActionListener(e -> supprimerVehicule());
        
        JButton btnFermer = new JButton("Fermer");
        btnFermer.addActionListener(e -> dispose());
        
        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnDefinirPrincipal);
        panelBoutons.add(btnSupprimer);
        panelBoutons.add(btnFermer);
        
        mainPanel.add(panelBoutons, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private void chargerVehicules() {
        listModel.clear();
        List<Vehicule> vehicules = VehiculeDAO.getVehiculesByUsager(idUsager);
        for (Vehicule v : vehicules) {
            listModel.addElement(v);
        }
    }
    
    private void ajouterVehicule() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        
        JTextField txtPlaque = new JTextField();
        JTextField txtAlias = new JTextField();
        
        panel.add(new JLabel("Plaque d'immatriculation:"));
        panel.add(txtPlaque);
        panel.add(new JLabel("Alias (optionnel):"));
        panel.add(txtAlias);
        panel.add(new JLabel("Exemple: 'Voiture principale', 'Moto'"));
        panel.add(new JLabel());
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Ajouter un véhicule", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            String plaque = txtPlaque.getText().trim().toUpperCase().replaceAll("\\s", "");
            String alias = txtAlias.getText().trim();
            
            if (plaque.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "La plaque d'immatriculation est obligatoire.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            boolean success = VehiculeDAO.ajouterVehiculeUsager(idUsager, plaque, alias);
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Véhicule ajouté avec succès!",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
                chargerVehicules();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'ajout du véhicule.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void definirPrincipal() throws SQLException {
        Vehicule selected = listVehicules.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un véhicule.",
                "Avertissement", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        boolean success = VehiculeDAO.definirVehiculePrincipal(idUsager, selected.getIdVehiculeUsager());
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Véhicule défini comme principal avec succès!",
                "Succès", JOptionPane.INFORMATION_MESSAGE);
            chargerVehicules();
        }
    }
    
    private void supprimerVehicule() {
        Vehicule selected = listVehicules.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un véhicule.",
                "Avertissement", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir supprimer ce véhicule?\n" +
            selected.toString(),
            "Confirmation de suppression", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = VehiculeDAO.supprimerVehicule(selected.getIdVehiculeUsager());
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Véhicule supprimé avec succès!",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
                chargerVehicules();
            }
        }
    }
    
    // Renderer personnalisé pour afficher les véhicules
    private class VehiculeRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Vehicule) {
                Vehicule v = (Vehicule) value;
                String text = v.toString();
                if (v.isEstPrincipal()) {
                    text = "⭐ " + text + " (Principal)";
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                setText(text);
            }
            
            return this;
        }
    }
}