package controleur;

import ihm.Page_Historique_Stationnements;
import ihm.Page_Utilisateur;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;

import modele.Stationnement;
import modele.dao.StationnementDAO;

public class ControleurHistoriqueStationnements implements ActionListener {
    
    private Page_Historique_Stationnements vue;
    
    public ControleurHistoriqueStationnements(Page_Historique_Stationnements vue) {
        this.vue = vue;
        configurerListeners();
    }
    
    private void configurerListeners() {
        // Configurer le bouton retour
        JButton btnRetour = vue.getBtnRetour();
        if (btnRetour != null) {
            btnRetour.addActionListener(this);
        }
        
        // Configurer la table pour les double-clics
        JTable table = vue.getTable();
        if (table != null) {
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                        afficherDetailsStationnement(table.getSelectedRow());
                    }
                }
            });
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == vue.getBtnRetour()) {
            retourProfil();
        }
    }
    
    private void afficherDetailsStationnement(int rowIndex) {
        // Récupérer les données depuis la base pour être sûr d'avoir les infos complètes
        java.util.List<Stationnement> stationnements = StationnementDAO.getHistoriqueStationnements(vue.getUsager().getIdUsager());
        
        if (rowIndex >= 0 && rowIndex < stationnements.size()) {
            Stationnement stationnement = stationnements.get(rowIndex);
            
            StringBuilder details = new StringBuilder();
            details.append("<html><div style='font-family: Arial;'>");
            details.append("<h3>Détails du stationnement</h3>");
            details.append("<hr>");
            details.append("<table border='0' cellspacing='5' cellpadding='5'>");
            
            details.append("<tr><td><b>Type:</b></td><td>").append(stationnement.getTypeStationnement()).append("</td></tr>");
            details.append("<tr><td><b>Véhicule:</b></td><td>").append(stationnement.getTypeVehicule()).append("</td></tr>");
            details.append("<tr><td><b>Plaque:</b></td><td>").append(stationnement.getPlaqueImmatriculation()).append("</td></tr>");
            details.append("<tr><td><b>Zone/Parking:</b></td><td>").append(stationnement.getZone()).append("</td></tr>");
            
            details.append("<tr><td><b>Date de création:</b></td><td>")
                   .append(stationnement.getDateCreation()
                          .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                   .append("</td></tr>");
            
            details.append("<tr><td><b>Statut:</b></td><td><b>").append(stationnement.getStatut()).append("</b></td></tr>");
            details.append("<tr><td><b>Coût:</b></td><td><b>")
                   .append(String.format("%.2f €", stationnement.getCout()))
                   .append("</b></td></tr>");
            
            if (stationnement.estVoirie()) {
                details.append("<tr><td><b>Durée prévue:</b></td><td>")
                       .append(stationnement.getDureeHeures())
                       .append("h").append(stationnement.getDureeMinutes())
                       .append("min</td></tr>");
            } else {
                if (stationnement.getHeureArrivee() != null) {
                    details.append("<tr><td><b>Heure d'arrivée:</b></td><td>")
                           .append(stationnement.getHeureArrivee()
                                  .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                           .append("</td></tr>");
                }
                if (stationnement.getHeureDepart() != null) {
                    details.append("<tr><td><b>Heure de départ:</b></td><td>")
                           .append(stationnement.getHeureDepart()
                                  .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                           .append("</td></tr>");
                }
            }
            
            details.append("</table>");
            
            if ("ACTIF".equals(stationnement.getStatut())) {
                details.append("<hr><p style='color: red; font-weight: bold;'>")
                       .append("⚠️ Ce stationnement est toujours actif.<br>")
                       .append("Conservez votre ticket de stationnement.</p>");
            }
            
            details.append("</div></html>");
            
            JOptionPane.showMessageDialog(vue, 
                details.toString(), 
                "Détails du stationnement", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void retourProfil() {
        Page_Utilisateur pageUtilisateur = new Page_Utilisateur(vue.getEmailUtilisateur());
        pageUtilisateur.setVisible(true);
        vue.dispose();
    }
}