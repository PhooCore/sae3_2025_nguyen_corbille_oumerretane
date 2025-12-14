package ihm;

import javax.swing.*;
import modele.Parking;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

public class CarteAdminPanel extends CartePanel {
    public enum ModeCarte {
        NAVIGATION,
        AJOUT,
        MODIFICATION
    }
    
    private ModeCarte mode = ModeCarte.NAVIGATION;
    private JLabel compteurLabel;
    private Point dragStart = null; // Pour le déplacement
    
    public CarteAdminPanel(java.net.URL imageUrl, String emailAdmin) throws java.io.IOException {
        super(imageUrl, emailAdmin);
        configurerInteractionsAdmin();
    }
    
    private void configurerInteractionsAdmin() {
        // Supprimer les listeners existants
        for (MouseListener listener : getMouseListeners()) {
            removeMouseListener(listener);
        }
        for (MouseMotionListener listener : getMouseMotionListeners()) {
            removeMouseMotionListener(listener);
        }
        for (MouseWheelListener listener : getMouseWheelListeners()) {
            removeMouseWheelListener(listener);
        }
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    // Démarrer le déplacement
                    dragStart = e.getPoint();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    
                    // Vérifier si on a cliqué sur un parking
                    Parking parkingClique = getParkingAtScreenPoint(e.getPoint());
                    
                    if (parkingClique != null) {
                        // Clic sur un parking existant
                        if (mode == ModeCarte.MODIFICATION) {
                            modifierParking(parkingClique);
                        } else {
                            selectionnerParking(parkingClique);
                        }
                    } else if (mode == ModeCarte.AJOUT) {
                        // Clic sur la carte pour ajouter
                        ajouterParking(e.getX(), e.getY());
                    }
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    dragStart = null;
                    // Restaurer le curseur selon le mode
                    if (mode == ModeCarte.AJOUT) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    } else if (mode == ModeCarte.MODIFICATION) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart != null) {
                    // Calculer le déplacement
                    int dx = e.getX() - dragStart.x;
                    int dy = e.getY() - dragStart.y;
                    
                    // Mettre à jour la translation
                    translateX += dx / zoom;
                    translateY += dy / zoom;
                    
                    dragStart = e.getPoint();
                    repaint();
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                Parking parkingSurvol = getParkingAtScreenPoint(e.getPoint());
                if (parkingSurvol != null) {
                    if (mode == ModeCarte.MODIFICATION) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                    String info = parkingSurvol.getLibelleParking() + 
                                 " - " + parkingSurvol.getPlacesDisponibles() + "/" + 
                                 parkingSurvol.getNombrePlaces() + " places";
                    setToolTipText(info);
                } else if (mode == ModeCarte.AJOUT) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                    setToolTipText(null);
                }
            }
        });
        
        // Zoom avec molette
        addMouseWheelListener(e -> {
            Point mousePoint = e.getPoint();
            double oldZoom = zoom;
            
            if (e.getPreciseWheelRotation() < 0) {
                // Zoom in
                zoom = Math.min(5.0, zoom * 1.2);
            } else {
                // Zoom out
                zoom = Math.max(0.5, zoom * 0.8);
            }
            
            // Ajuster la translation pour zoomer vers le point de la souris
            double zoomFactor = zoom / oldZoom;
            translateX = mousePoint.x / zoom - (mousePoint.x / oldZoom - translateX) * zoomFactor;
            translateY = mousePoint.y / zoom - (mousePoint.y / oldZoom - translateY) * zoomFactor;
            
            repaint();
        });
    }
    
    // Surcharger pour ne PAS permettre le stationnement
    @Override
    protected void onParkingClicked(Parking parking) {
        // Ne rien faire - l'admin ne peut pas stationner
        selectionnerParking(parking);
    }
    
    // Surcharger pour ne PAS ouvrir la page de stationnement
    @Override
    protected void ouvrirPageStationnement(Parking parking) {
        // Ne rien faire - l'admin ne peut pas stationner
    }
    
    public void setCompteurLabel(JLabel label) {
        this.compteurLabel = label;
        updateCompteur();
    }
    
    private void updateCompteur() {
        if (compteurLabel != null) {
            compteurLabel.setText("Parkings chargés: " + getParkings().size());
        }
    }
    
    @Override
    public void rafraichirParkings() {
        super.rafraichirParkings();
        updateCompteur();
        repaint();
    }
    
    private void ajouterParking(int screenX, int screenY) {
        // Convertir les coordonnées écran en coordonnées image
        Point imagePoint = screenToImageCoordinates(new Point(screenX, screenY));
        
        // Calculer la position relative par rapport à l'image (0.0 à 1.0)
        float relativeX = (float)imagePoint.x / getOriginalImageWidth();
        float relativeY = (float)imagePoint.y / getOriginalImageHeight();
        
        // S'assurer que le clic est dans l'image
        if (relativeX < 0 || relativeX > 1 || relativeY < 0 || relativeY > 1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez cliquer sur la carte",
                "Position invalide",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Formulaire d'ajout
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        
        JTextField txtLibelle = new JTextField();
        JTextField txtAdresse = new JTextField();
        JTextField txtPlaces = new JTextField("100");
        JTextField txtHauteur = new JTextField("2.00");
        JCheckBox chkTarifSoiree = new JCheckBox();
        JCheckBox chkMoto = new JCheckBox();
        JTextField txtPlacesMoto = new JTextField("0");
        JCheckBox chkRelais = new JCheckBox();
        
        // Champ d'ID qui sera calculé automatiquement
        JTextField txtId = new JTextField();
        txtId.setEditable(true); // L'admin peut le modifier si besoin
        txtId.setBackground(new Color(240, 240, 240));
        
        // Quand le libellé change, suggérer un ID automatique
        txtLibelle.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateIdFromLibelle();
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateIdFromLibelle();
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateIdFromLibelle();
            }
            
            private void updateIdFromLibelle() {
                String libelle = txtLibelle.getText().trim();
                if (!libelle.isEmpty()) {
                    // Convertir le libellé en format ID : MAJUSCULE, remplace espaces par _
                    String suggestedId = libelle.toUpperCase()
                        .replaceAll(" ", "_")
                        .replaceAll("[^A-Z0-9_]", "") // Garder seulement lettres, chiffres et underscores
                        .replaceAll("__+", "_"); // Remplacer les underscores multiples par un seul
                    
                    // Ajouter le préfixe PARK_ si ce n'est pas déjà le cas
                    if (!suggestedId.startsWith("PARK_")) {
                        suggestedId = "PARK_" + suggestedId;
                    }
                    
                    txtId.setText(suggestedId);
                }
            }
        });
        
        formPanel.add(new JLabel("Nom/Libellé:"));
        formPanel.add(txtLibelle);
        formPanel.add(new JLabel("ID Parking (auto-généré):"));
        formPanel.add(txtId);
        formPanel.add(new JLabel("Adresse:"));
        formPanel.add(txtAdresse);
        formPanel.add(new JLabel("Nombre de places:"));
        formPanel.add(txtPlaces);
        formPanel.add(new JLabel("Hauteur maximale (m):"));
        formPanel.add(txtHauteur);
        formPanel.add(new JLabel("Tarif soirée:"));
        formPanel.add(chkTarifSoiree);
        formPanel.add(new JLabel("Parking moto:"));
        formPanel.add(chkMoto);
        formPanel.add(new JLabel("Places moto:"));
        formPanel.add(txtPlacesMoto);
        formPanel.add(new JLabel("Est un relais:"));
        formPanel.add(chkRelais);
        
        int result = JOptionPane.showConfirmDialog(this, formPanel, 
            "Ajouter un nouveau parking à la position sélectionnée", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String libelle = txtLibelle.getText().trim();
                String id = txtId.getText().trim();
                String adresse = txtAdresse.getText().trim();
                int places = Integer.parseInt(txtPlaces.getText());
                double hauteur = Double.parseDouble(txtHauteur.getText());
                boolean tarifSoiree = chkTarifSoiree.isSelected();
                boolean hasMoto = chkMoto.isSelected();
                int placesMoto = hasMoto ? Integer.parseInt(txtPlacesMoto.getText()) : 0;
                boolean estRelais = chkRelais.isSelected();
                
                // Vérifier que l'ID n'est pas vide
                if (id.isEmpty()) {
                    // Générer l'ID à partir du libellé si vide
                    id = generateParkingIdFromName(libelle);
                }
                
                // Vérifier si l'ID existe
                if (modele.dao.ParkingDAO.idParkingExiste(id)) {
                    JOptionPane.showMessageDialog(this,
                        "L'ID " + id + " existe déjà. Veuillez en choisir un autre.\n" +
                        "Suggestion: " + generateUniqueParkingId(id),
                        "ID dupliqué", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Créer le parking avec la position
                modele.Parking nouveauParking = new modele.Parking(id, libelle, adresse, 
                    places, places, hauteur, tarifSoiree, hasMoto, placesMoto, placesMoto,
                    estRelais, relativeX, relativeY);
                
                if (modele.dao.ParkingDAO.ajouterParking(nouveauParking)) {
                    rafraichirParkings();
                    
                    JOptionPane.showMessageDialog(this, 
                        "Parking ajouté avec succès!\n" +
                        "Nom: " + libelle + "\n" +
                        "ID: " + id, "Succès", 
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    mode = ModeCarte.NAVIGATION;
                    setCursor(Cursor.getDefaultCursor());
                }
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Veuillez entrer des valeurs numériques valides", 
                    "Erreur de format", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            mode = ModeCarte.NAVIGATION;
            setCursor(Cursor.getDefaultCursor());
        }
    }

    // Méthode pour générer un ID de parking à partir du nom
    private String generateParkingIdFromName(String libelle) {
        if (libelle == null || libelle.isEmpty()) {
            // Retourner un ID générique si pas de nom
            return modele.dao.ParkingDAO.genererNouvelIdParking();
        }
        
        // Convertir en MAJUSCULE et remplacer les espaces par des underscores
        String id = libelle.toUpperCase()
            .replaceAll(" ", "_")
            .replaceAll("[^A-Z0-9_]", "") // Garder seulement lettres, chiffres et underscores
            .replaceAll("__+", "_") // Remplacer les underscores multiples par un seul
            .trim();
        
        // Limiter la longueur à 50 caractères maximum
        if (id.length() > 50) {
            id = id.substring(0, 50);
        }
        
        // Ajouter le préfixe PARK_ si ce n'est pas déjà le cas
        if (!id.startsWith("PARK_")) {
            id = "PARK_" + id;
        }
        
        return id;
    }

    // Méthode pour générer un ID unique en cas de duplication
    private String generateUniqueParkingId(String baseId) {
        for (int i = 1; i <= 100; i++) {
            String candidate = baseId + "_" + i;
            if (!modele.dao.ParkingDAO.idParkingExiste(candidate)) {
                return candidate;
            }
        }
        return modele.dao.ParkingDAO.genererNouvelIdParking();
    }
    
    private void selectionnerParking(Parking parking) {
        setParkingSelectionne(parking);
        repaint();
        
        DecimalFormat df = new DecimalFormat("#.##");
        String info = String.format(
            "<html><div style='width:300px;'><h3>%s</h3>" +
            "<b>ID:</b> %s<br><b>Adresse:</b> %s<br>" +
            "<b>Capacité:</b> %d places (%d disponibles)<br>" +
            "<b>Hauteur max:</b> %s m<br><b>Tarif soirée:</b> %s<br>" +
            "<b>Parking moto:</b> %s<br><b>Est un relais:</b> %s",
            parking.getLibelleParking(),
            parking.getIdParking(),
            parking.getAdresseParking(),
            parking.getNombrePlaces(),
            parking.getPlacesDisponibles(),
            df.format(parking.getHauteurParking()),
            parking.hasTarifSoiree() ? "Oui" : "Non",
            parking.hasMoto() ? "Oui" : "Non",
            parking.isEstRelais() ? "Oui" : "Non"
        );
        
        if (parking.hasMoto()) {
            info += String.format("<br><b>Places moto:</b> %d (%d disponibles)",
                parking.getPlacesMoto(),
                parking.getPlacesMotoDisponibles());
        }
        
        if (parking.getPositionX() != null) {
            info += String.format("<br><b>Position:</b> (%.2f, %.2f)",
                parking.getPositionX(), parking.getPositionY());
        }
        
        info += "</div></html>";
        
        JOptionPane.showMessageDialog(this, info, "Information Parking", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void modifierParking(Parking parking) {
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        
        JTextField txtLibelle = new JTextField(parking.getLibelleParking());
        JTextField txtAdresse = new JTextField(parking.getAdresseParking());
        JTextField txtPlaces = new JTextField(String.valueOf(parking.getNombrePlaces()));
        JTextField txtPlacesDispo = new JTextField(String.valueOf(parking.getPlacesDisponibles()));
        JTextField txtHauteur = new JTextField(String.valueOf(parking.getHauteurParking()));
        JCheckBox chkTarifSoiree = new JCheckBox("", parking.hasTarifSoiree());
        JCheckBox chkMoto = new JCheckBox("", parking.hasMoto());
        JTextField txtPlacesMoto = new JTextField(String.valueOf(parking.getPlacesMoto()));
        JTextField txtPlacesMotoDispo = new JTextField(String.valueOf(parking.getPlacesMotoDisponibles()));
        JCheckBox chkRelais = new JCheckBox("", parking.isEstRelais());
        
        formPanel.add(new JLabel("Nom/Libellé:"));
        formPanel.add(txtLibelle);
        formPanel.add(new JLabel("Adresse:"));
        formPanel.add(txtAdresse);
        formPanel.add(new JLabel("Nombre de places:"));
        formPanel.add(txtPlaces);
        formPanel.add(new JLabel("Places disponibles:"));
        formPanel.add(txtPlacesDispo);
        formPanel.add(new JLabel("Hauteur maximale (m):"));
        formPanel.add(txtHauteur);
        formPanel.add(new JLabel("Tarif soirée:"));
        formPanel.add(chkTarifSoiree);
        formPanel.add(new JLabel("Parking moto:"));
        formPanel.add(chkMoto);
        formPanel.add(new JLabel("Places moto:"));
        formPanel.add(txtPlacesMoto);
        formPanel.add(new JLabel("Places moto dispo:"));
        formPanel.add(txtPlacesMotoDispo);
        formPanel.add(new JLabel("Est un relais:"));
        formPanel.add(chkRelais);
        
        int result = JOptionPane.showConfirmDialog(this, formPanel, 
            "Modifier le parking: " + parking.getLibelleParking(), 
            JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                parking.setLibelleParking(txtLibelle.getText().trim());
                parking.setAdresseParking(txtAdresse.getText().trim());
                parking.setNombrePlaces(Integer.parseInt(txtPlaces.getText()));
                parking.setPlacesDisponibles(Integer.parseInt(txtPlacesDispo.getText()));
                parking.setHauteurParking(Double.parseDouble(txtHauteur.getText()));
                parking.setTarifSoiree(chkTarifSoiree.isSelected());
                parking.setHasMoto(chkMoto.isSelected());
                parking.setEstRelais(chkRelais.isSelected());
                
                if (parking.hasMoto()) {
                    parking.setPlacesMoto(Integer.parseInt(txtPlacesMoto.getText()));
                    parking.setPlacesMotoDisponibles(Integer.parseInt(txtPlacesMotoDispo.getText()));
                }
                
                if (modele.dao.ParkingDAO.mettreAJourParking(parking)) {
                    rafraichirParkings();
                    JOptionPane.showMessageDialog(this, 
                        "Parking modifié avec succès!", "Succès", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Veuillez entrer des valeurs numériques valides", 
                    "Erreur de format", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        mode = ModeCarte.NAVIGATION;
        setCursor(Cursor.getDefaultCursor());
    }
    
    public void supprimerParkingSelectionne() {
        Parking parkingSelectionne = getParkingSelectionne();
        if (parkingSelectionne != null) {
            int confirmation = JOptionPane.showConfirmDialog(this,
                "<html><b>Confirmation de suppression</b><br><br>" +
                "Voulez-vous vraiment supprimer le parking :<br>" +
                "<b>" + parkingSelectionne.getLibelleParking() + "</b><br>" +
                "ID: " + parkingSelectionne.getIdParking() + "<br>" +
                "Adresse: " + parkingSelectionne.getAdresseParking() + "<br><br>" +
                "<font color='red'><b>Cette action est irréversible !</b></font></html>",
                "Suppression de parking",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirmation == JOptionPane.YES_OPTION) {
                try {
                    if (modele.dao.StationnementDAO.hasStationnementEnCours(parkingSelectionne.getIdParking())) {
                        JOptionPane.showMessageDialog(this,
                            "<html><b>Impossible de supprimer ce parking</b><br><br>" +
                            "Il y a encore des stationnements en cours dans ce parking.<br>" +
                            "Veuillez attendre que tous les stationnements soient terminés.</html>",
                            "Suppression impossible",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    if (modele.dao.ParkingDAO.supprimerParking(parkingSelectionne.getIdParking())) {
                        rafraichirParkings();
                        setParkingSelectionne(null);
                        
                        JOptionPane.showMessageDialog(this,
                            "<html><b>Parking supprimé avec succès !</b><br><br>" +
                            "Le parking a été supprimé de la base de données.</html>",
                            "Suppression réussie",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                        "<html><b>Erreur lors de la suppression :</b><br>" + e.getMessage() + "</html>",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "<html><b>Aucun parking sélectionné</b><br><br>" +
                "Veuillez d'abord sélectionner un parking en cliquant dessus.</html>",
                "Sélection requise",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public void modeAjout() {
        mode = ModeCarte.AJOUT;
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        JOptionPane.showMessageDialog(this,
            "Mode Ajout activé. Cliquez sur la carte pour ajouter un nouveau parking.\n" +
            "La position du clic servira à positionner le marqueur.",
            "Mode Ajout", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void modeModification() {
        mode = ModeCarte.MODIFICATION;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JOptionPane.showMessageDialog(this,
            "Mode Modification activé. Cliquez sur un parking pour le modifier.",
            "Mode Modification", JOptionPane.INFORMATION_MESSAGE);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        
        // Afficher le mode actuel
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillRoundRect(5, 5, 220, 50, 10, 10);
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawRoundRect(5, 5, 220, 50, 10, 10);
        
        String modeText = "Mode: " + mode.toString();
        Color modeColor;
        switch (mode) {
            case AJOUT:
                modeColor = new Color(40, 167, 69); // Vert
                break;
            case MODIFICATION:
                modeColor = new Color(255, 193, 7); // Orange
                break;
            default:
                modeColor = new Color(0, 123, 255); // Bleu
        }
        
        g2d.setColor(modeColor);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(modeText, 10, 25);
        
        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString("Parkings: " + getParkings().size(), 10, 40);
        
        // Instructions selon le mode
        String instruction = "";
        switch (mode) {
            case AJOUT:
                instruction = "Cliquez pour ajouter";
                break;
            case MODIFICATION:
                instruction = "Cliquez sur un parking";
                break;
            default:
                instruction = "Clic-glisser: déplacer";
                break;
        }
        g2d.drawString(instruction, 10, 55);
    }
}