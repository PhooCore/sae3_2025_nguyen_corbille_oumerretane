package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import modele.Parking;
import modele.dao.ParkingDAO;
import javax.imageio.ImageIO;

/**
 * PANEL D'AFFICHAGE DE LA CARTE INTERACTIVE
 * =========================================
 * Ce composant affiche une carte avec les parkings, permet le zoom/déplacement,
 * et gère l'affichage des zones colorées (aplats PNG).
 * 
 * CONCEPTION :
 * - Héritage prévu pour CarteAdminPanel (protected methods)
 * - Gestion complète du rendu et des interactions
 * - Support des zones semi-transparentes
 */
public class CartePanel extends JPanel {
    // IMAGES ET DONNÉES
    private BufferedImage carteImage;              // Carte de base
    private List<Parking> parkings = new ArrayList<>(); // Liste des parkings à afficher
    private Parking parkingSelectionne = null;     // Parking actuellement sélectionné
    private String emailUtilisateur;               // Identifiant utilisateur pour les actions
    
    // GESTION DES ZONES (APLATS COLORÉS)
    private BufferedImage zoneImage = null;        // Image PNG de la zone (aplat de couleur)
    private float zoneOpacity = 0.4f;              // Opacité de la zone (40% par défaut)
    private boolean zoneVisible = false;           // Visibilité de la zone
    private String nomZoneActuelle = null;         // Nom de la zone affichée
    private String codeZoneActuelle = null;        // Code couleur de la zone (ZJ, ZR, etc.)
    
    // ZOOM ET DÉPLACEMENT (variables protégées pour héritage)
    protected double zoom = 1.0;                   // Niveau de zoom actuel
    protected double translateX = 0;               // Translation horizontale
    protected double translateY = 0;               // Translation verticale
    private Point dragStart = null;                // Point de départ du drag
    
    // DIMENSIONS ORIGINALES
    private int originalImageWidth;                // Largeur originale de la carte
    private int originalImageHeight;               // Hauteur originale de la carte
    
    /**
     * CONSTRUCTEUR PRINCIPAL
     * @param imageUrl URL de l'image de la carte
     * @param emailUtilisateur Email de l'utilisateur (pour les actions)
     */
    public CartePanel(java.net.URL imageUrl, String emailUtilisateur) throws java.io.IOException {
        // CHARGEMENT DE LA CARTE
        this.carteImage = ImageIO.read(imageUrl);
        this.originalImageWidth = carteImage.getWidth();
        this.originalImageHeight = carteImage.getHeight();
        this.emailUtilisateur = emailUtilisateur;
        
        // CONFIGURATION DU PANEL
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));
        
        // CHARGEMENT DES DONNÉES ET CONFIGURATION
        chargerParkings();        // Récupère les parkings depuis la BDD
        configurerInteractions(); // Configure les listeners souris
    }
    
    /**
     * CHARGE LES PARKINGS DEPUIS LA BASE DE DONNÉES
     * Méthode appelée au démarrage et pour rafraîchir les données
     */
    private void chargerParkings() {
        try {
            // Utiliser le Singleton pour récupérer les parkings
            parkings = ParkingDAO.getInstance().getAllParkings();
            repaint();
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des parkings: " + e.getMessage());
            parkings = new ArrayList<>();
        }
    }
    
    /**
     * RAFRAÎCHIT L'AFFICHAGE DES PARKINGS
     * Appelé lors des mises à jour de disponibilité
     */
    public void rafraichirParkings() {
        chargerParkings();
        repaint();
    }
    
    /**
     * CONFIGURE LES INTERACTIONS SOURIS
     * Cette méthode est protégée pour permettre la surcharge dans CarteAdminPanel
     * Elle gère :
     * - Clic-gaucher pour sélectionner un parking
     * - Drag pour déplacer la carte
     * - Molette pour zoomer
     * - Survol pour les infobulles
     */
    protected void configurerInteractions() {
        // NETTOYAGE DES ANCIENS LISTENERS (évite les doublons)
        for (MouseListener listener : getMouseListeners()) removeMouseListener(listener);
        for (MouseMotionListener listener : getMouseMotionListeners()) removeMouseMotionListener(listener);
        for (MouseWheelListener listener : getMouseWheelListeners()) removeMouseWheelListener(listener);
        
        // GESTION DES CLICS SOURIS
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    dragStart = e.getPoint(); // Mémorise le point de départ
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    dragStart = null;
                    setCursor(Cursor.getDefaultCursor());
                    
                    // Vérifie si on a cliqué sur un parking
                    Point clickPoint = e.getPoint();
                    Parking parkingClique = getParkingAtScreenPoint(clickPoint);
                    if (parkingClique != null) {
                        onParkingClicked(parkingClique); // Lance la procédure de stationnement
                    }
                }
            }
        });
        
        // GESTION DU DÉPLACEMENT (DRAG)
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart != null) {
                    // Calcule le déplacement en coordonnées image (pas écran)
                    double dx = e.getX() - dragStart.getX();
                    double dy = e.getY() - dragStart.getY();
                    
                    // Met à jour la translation (compense le zoom)
                    translateX += dx / zoom;
                    translateY += dy / zoom;
                    
                    dragStart = e.getPoint();
                    repaint();
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                // Change le curseur et affiche l'infobulle au survol
                Parking parkingSurvol = getParkingAtScreenPoint(e.getPoint());
                if (parkingSurvol != null) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    String info = parkingSurvol.getLibelleParking() + 
                                 " - " + parkingSurvol.getPlacesDisponibles() + "/" + 
                                 parkingSurvol.getNombrePlaces() + " places";
                    setToolTipText(info);
                } else {
                    setCursor(Cursor.getDefaultCursor());
                    setToolTipText(null);
                }
            }
        });
        
        // GESTION DU ZOOM AVEC MOLETTE
        addMouseWheelListener(e -> {
            Point mousePoint = e.getPoint();
            double oldZoom = zoom;
            
            // CALCUL DU NOUVEAU ZOOM
            if (e.getPreciseWheelRotation() < 0) {
                zoom = Math.min(5.0, zoom * 1.2); // Zoom in (max 5x)
            } else {
                zoom = Math.max(0.5, zoom * 0.8); // Zoom out (min 0.5x)
            }
            
            // AJUSTEMENT DE LA TRANSLATION POUR ZOOMER VERS LA SOURIS
            // Cette formule permet de garder le point sous la souris au même endroit
            double zoomFactor = zoom / oldZoom;
            translateX = mousePoint.x / zoom - (mousePoint.x / oldZoom - translateX) * zoomFactor;
            translateY = mousePoint.y / zoom - (mousePoint.y / oldZoom - translateY) * zoomFactor;
            
            repaint();
        });
    }
    
    /**
     * TROUVE UN PARKING SOUS UN POINT ÉCRAN
     * @param screenPoint Point en coordonnées écran
     * @return Le parking trouvé ou null
     */
    protected Parking getParkingAtScreenPoint(Point screenPoint) {
        // Convertit les coordonnées écran -> image
        Point imagePoint = screenToImage(screenPoint);
        
        // Parcourt tous les parkings
        for (Parking p : parkings) {
            if (p.getPositionX() != null && p.getPositionY() != null) {
                // Convertit les coordonnées relatives (0.0-1.0) en pixels
                int parkingX = (int)(p.getPositionX() * originalImageWidth);
                int parkingY = (int)(p.getPositionY() * originalImageHeight);
                
                // Vérifie la proximité (tolérance de 10 pixels)
                if (Math.abs(imagePoint.x - parkingX) < 10 && Math.abs(imagePoint.y - parkingY) < 10) {
                    return p;
                }
            }
        }
        return null;
    }
    
    /**
     * CONVERTIT UN POINT ÉCRAN EN COORDONNÉES IMAGE
     * Utilisé pour les interactions et le rendu
     */
    private Point screenToImage(Point screenPoint) {
        double x = (screenPoint.x / zoom) - translateX;
        double y = (screenPoint.y / zoom) - translateY;
        return new Point((int)x, (int)y);
    }
    
    /**
     * VERSION PUBLIQUE DE screenToImage
     * Permet aux sous-classes d'utiliser cette conversion
     */
    public Point screenToImageCoordinates(Point screenPoint) {
        return screenToImage(screenPoint);
    }
    
    /**
     * GÈRE LE CLIC SUR UN PARKING
     * Affiche une boîte de dialogue et lance le processus de stationnement
     */
    protected void onParkingClicked(Parking parking) {
        parkingSelectionne = parking;
        repaint();
        
        // BOÎTE DE DIALOGUE DE CONFIRMATION
        int option = JOptionPane.showConfirmDialog(
            this,
            "Voulez-vous vous garer dans ce parking?\n\n" +
            "Nom: " + parking.getLibelleParking() + "\n" +
            "Adresse: " + parking.getAdresseParking() + "\n" +
            "Places disponibles: " + parking.getPlacesDisponibles() + "/" + parking.getNombrePlaces() +
            (parking.hasMoto() ? "\nPlaces moto: " + parking.getPlacesMotoDisponibles() + "/" + parking.getPlacesMoto() : ""),
            "Stationnement - " + parking.getLibelleParking(),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            ouvrirPageStationnement(parking);
        }
        
        parkingSelectionne = null;
        repaint();
    }
    
    /**
     * OUVRE LA PAGE DE STATIONNEMENT
     * Ferme la fenêtre courante et ouvre la nouvelle page
     */
    protected void ouvrirPageStationnement(Parking parking) {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame != null) {
            Page_Garer_Parking pageGarer = new Page_Garer_Parking(emailUtilisateur, parking);
            pageGarer.setVisible(true);
            parentFrame.dispose();
        }
    }
    
    // ========== MÉTHODES DE CONTRÔLE DU ZOOM/DÉPLACEMENT ==========
    
    /**
     * ZOOM IN (agrandissement)
     * Zoom de 20% vers le centre de la vue
     */
    public void zoomIn() {
        double oldZoom = zoom;
        zoom = Math.min(5.0, zoom * 1.2);
        
        // Centre le zoom
        Point center = new Point(getWidth() / 2, getHeight() / 2);
        double zoomFactor = zoom / oldZoom;
        translateX = center.x / zoom - (center.x / oldZoom - translateX) * zoomFactor;
        translateY = center.y / zoom - (center.y / oldZoom - translateY) * zoomFactor;
        
        repaint();
    }
    
    /**
     * ZOOM OUT (réduction)
     * Zoom de 20% depuis le centre de la vue
     */
    public void zoomOut() {
        double oldZoom = zoom;
        zoom = Math.max(0.5, zoom * 0.8);
        
        // Centre le zoom
        Point center = new Point(getWidth() / 2, getHeight() / 2);
        double zoomFactor = zoom / oldZoom;
        translateX = center.x / zoom - (center.x / oldZoom - translateX) * zoomFactor;
        translateY = center.y / zoom - (center.y / oldZoom - translateY) * zoomFactor;
        
        repaint();
    }
    
    /**
     * RÉINITIALISE LE ZOOM ET LA POSITION
     * Retourne à la vue initiale (zoom 1.0, centré)
     */
    public void resetZoom() {
        zoom = 1.0;
        translateX = 0;
        translateY = 0;
        repaint();
    }
    
    /**
     * DÉPLACE LA VUE VERS LA GAUCHE
     */
    public void panLeft() {
        translateX -= 50 / zoom; // Déplacement relatif au zoom
        repaint();
    }
    
    /**
     * DÉPLACE LA VUE VERS LA DROITE
     */
    public void panRight() {
        translateX += 50 / zoom;
        repaint();
    }
    
    /**
     * DÉPLACE LA VUE VERS LE HAUT
     */
    public void panUp() {
        translateY -= 50 / zoom;
        repaint();
    }
    
    /**
     * DÉPLACE LA VUE VERS LE BAS
     */
    public void panDown() {
        translateY += 50 / zoom;
        repaint();
    }
    
    /**
     * CENTRE LA VUE SUR UN PARKING SPÉCIFIQUE
     * Utile pour la navigation rapide
     */
    public void centerOnParking(Parking parking) {
        if (parking != null && parking.getPositionX() != null && parking.getPositionY() != null) {
            // Convertit les coordonnées relatives en pixels
            int parkingX = (int)(parking.getPositionX() * originalImageWidth);
            int parkingY = (int)(parking.getPositionY() * originalImageHeight);
            
            // Centre la vue sur ce point
            translateX = -parkingX + (getWidth() / (2 * zoom));
            translateY = -parkingY + (getHeight() / (2 * zoom));
            
            repaint();
        }
    }
    
    /**
     * MÉTHODE DE DESSIN PRINCIPALE
     * Gère tout le rendu de la carte, zones, parkings et interfaces
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (carteImage != null) {
            Graphics2D g2d = (Graphics2D) g;
            
            // OPTIMISATIONS DE RENDU
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            
            // SAUVEGARDE DE LA TRANSFORMATION ORIGINALE
            AffineTransform originalTransform = g2d.getTransform();
            
            // APPLICATION DU ZOOM ET TRANSLATION
            g2d.scale(zoom, zoom);
            g2d.translate(translateX, translateY);
            
            // DESSIN DE LA CARTE DE BASE
            g2d.drawImage(carteImage, 0, 0, originalImageWidth, originalImageHeight, this);
            
            // ========== DESSIN DE LA ZONE (APLAT PNG) ==========
            if (zoneVisible && zoneImage != null) {
                Composite originalComposite = g2d.getComposite();
                
                // APPLIQUE L'OPACITÉ POUR L'APLAT
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, zoneOpacity));
                
                // DESSINE L'APLAT DE COULEUR
                g2d.drawImage(zoneImage, 0, 0, originalImageWidth, originalImageHeight, this);
                
                g2d.setComposite(originalComposite);
            }
            // ===================================================
            
            // DESSINE LES PARKINGS (PAR-DESSUS LA ZONE)
            for (Parking p : parkings) {
                if (p.getPositionX() != null && p.getPositionY() != null) {
                    dessinerParking(g2d, p);
                }
            }
            
            // RESTAURE LA TRANSFORMATION POUR LES INFOS SUPERFICIELLES
            g2d.setTransform(originalTransform);
            
            // AFFICHE LES INFORMATIONS D'INTERFACE
            dessinerInfos(g2d);
            
            // AFFICHE L'INDICATEUR DE ZONE SI NÉCESSAIRE
            if (zoneVisible && nomZoneActuelle != null) {
                dessinerIndicateurZone(g2d);
            }
        }
    }
    
    /**
     * DESSINE UN PARKING SUR LA CARTE
     * Rendu complexe avec :
     * - Point coloré selon disponibilité
     * - Ombre pour effet 3D
     * - Nom et places disponibles
     * - Icônes spéciales (moto, relais)
     */
    private void dessinerParking(Graphics2D g2d, Parking p) {
        // CONVERSION COORDONNÉES RELATIVES -> PIXELS
        int x = (int)(p.getPositionX() * originalImageWidth);
        int y = (int)(p.getPositionY() * originalImageHeight);
        
        // CHOIX DE LA COULEUR SELON DISPONIBILITÉ
        Color couleur;
        if (p.getPlacesDisponibles() == 0) {
            couleur = new Color(220, 53, 69); // ROUGE = Complet
        } else if (p.getPlacesDisponibles() > p.getNombrePlaces() / 4) {
            couleur = new Color(40, 167, 69); // VERT = Disponible
        } else {
            couleur = new Color(255, 193, 7); // ORANGE = Peu de places
        }
        
        // TAILLE DU POINT (22 pixels)
        int pointSize = 22;
        
        // HALO JAUNE POUR LE PARKING SÉLECTIONNÉ
        if (p == parkingSelectionne) {
            g2d.setColor(new Color(255, 255, 0, 100));
            g2d.fillOval(x - pointSize - 8, y - pointSize - 8, (pointSize + 8) * 2, (pointSize + 8) * 2);
        }
        
        // OMBRE SOUS LE POINT (effet 3D)
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillOval(x - pointSize/2 + 3, y - pointSize/2 + 3, pointSize, pointSize);
        
        // POINT PRINCIPAL
        g2d.setColor(couleur);
        g2d.fillOval(x - pointSize/2, y - pointSize/2, pointSize, pointSize);
        
        // BORDURES (blanche épaisse + noire fine)
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawOval(x - pointSize/2, y - pointSize/2, pointSize, pointSize);
        
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawOval(x - pointSize/2, y - pointSize/2, pointSize, pointSize);
        
        // SAUVEGARDE TRANSFORMATION POUR TEXTE (coordonnées écran)
        AffineTransform originalTransform = g2d.getTransform();
        g2d.setTransform(new AffineTransform());
        
        // CONVERSION COORDONNÉES IMAGE -> ÉCRAN
        Point screenPoint = imageToScreen(new Point(x, y));
        
        // AFFICHAGE DU NOM (toujours visible)
        String nomCourt = p.getLibelleParking();
        if (nomCourt.length() > 20) {
            nomCourt = nomCourt.substring(0, 17) + "...";
        }
        
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        int textWidth = g2d.getFontMetrics().stringWidth(nomCourt);
        
        // FOND POUR LE NOM
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fillRoundRect(screenPoint.x - textWidth/2 - 5, screenPoint.y - pointSize - 25, 
                         textWidth + 10, 22, 6, 6);
        
        // BORDURE DU FOND
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(screenPoint.x - textWidth/2 - 5, screenPoint.y - pointSize - 25, 
                         textWidth + 10, 22, 6, 6);
        
        // TEXTE DU NOM
        g2d.setColor(Color.BLACK);
        g2d.drawString(nomCourt, screenPoint.x - textWidth/2, screenPoint.y - pointSize - 10);
        
        // AFFICHAGE DES PLACES DISPONIBLES
        String placesText = p.getPlacesDisponibles() + "/" + p.getNombrePlaces();
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        int placesWidth = g2d.getFontMetrics().stringWidth(placesText);
        
        g2d.setColor(new Color(255, 255, 255, 220));
        g2d.fillRoundRect(screenPoint.x - placesWidth/2 - 4, screenPoint.y + pointSize/2 + 5, 
                         placesWidth + 8, 16, 4, 4);
        
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawString(placesText, screenPoint.x - placesWidth/2, screenPoint.y + pointSize/2 + 16);
        
        // ICÔNES SPÉCIALES
        if (p.hasMoto()) {
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.drawString("M", screenPoint.x - 15, screenPoint.y + pointSize/2 + 35);
        }
        
        if (p.isEstRelais()) {
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.drawString("R", screenPoint.x + 5, screenPoint.y + pointSize/2 + 35);
        }
        
        // EFFETS SPÉCIAUX POUR PARKING SÉLECTIONNÉ
        if (p == parkingSelectionne) {
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(screenPoint.x - pointSize/2 - 2, screenPoint.y - pointSize/2 - 2, 
                        pointSize + 4, pointSize + 4);
            
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("", screenPoint.x - 7, screenPoint.y - pointSize - 30);
        }
        
        // RESTAURATION TRANSFORMATION
        g2d.setTransform(originalTransform);
    }
    
    /**
     * CONVERTIT UN POINT IMAGE EN COORDONNÉES ÉCRAN
     */
    private Point imageToScreen(Point imagePoint) {
        double x = (imagePoint.x + translateX) * zoom;
        double y = (imagePoint.y + translateY) * zoom;
        return new Point((int)x, (int)y);
    }
    
    /**
     * DESSINE LES INFORMATIONS D'INTERFACE
     * - Légende des parkings
     * - Contrôles
     * - Niveau de zoom
     * - Infobulle de survol
     */
    private void dessinerInfos(Graphics2D g2d) {
        // PANEL DE CONTRÔLES (bas gauche)
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fillRoundRect(10, getHeight() - 90, 280, 80, 10, 10);
        
        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        g2d.drawString("Contrôles :", 20, getHeight() - 70);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("• Molette : Zoom", 30, getHeight() - 52);
        g2d.drawString("• Clic-glisser : Déplacer", 30, getHeight() - 35);
        
        // INDICATEUR DE ZOOM (haut droit)
        String zoomText = String.format("Zoom: %.1fx", zoom);
        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        int zoomWidth = g2d.getFontMetrics().stringWidth(zoomText);
        g2d.setColor(new Color(52, 152, 219));
        g2d.fillRoundRect(getWidth() - zoomWidth - 35, 10, zoomWidth + 25, 30, 8, 8);
        g2d.setColor(Color.WHITE);
        g2d.drawString(zoomText, getWidth() - zoomWidth - 25, 32);
        
        // LÉGENDE DES PARKINGS (bas droit)
        String[] legendItems = {
            "Vert = Places disponibles",
            "Orange = Peu de places", 
            "Rouge = Complet",
            "M = Parking moto",
            "R = Parking relais"
        };
        
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fillRoundRect(getWidth() - 180, getHeight() - 130, 170, 130, 10, 10);
        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        g2d.drawString("Légende parkings :", getWidth() - 170, getHeight() - 105);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        for (int i = 0; i < legendItems.length; i++) {
            g2d.drawString(legendItems[i], getWidth() - 165, getHeight() - 85 + (i * 18));
        }
        
        // INFOBULLE DE SURVOL DE PARKING
        try {
            Point mousePoint = getMousePosition();
            if (mousePoint != null) {
                Parking parkingSurvole = getParkingAtScreenPoint(mousePoint);
                if (parkingSurvole != null) {
                    g2d.setColor(new Color(255, 255, 255, 230));
                    g2d.fillRoundRect(getWidth() - 250, 50, 240, 80, 10, 10);
                    
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.setFont(new Font("Arial", Font.BOLD, 12));
                    g2d.drawString("Parking survolé:", getWidth() - 240, 70);
                    
                    g2d.setFont(new Font("Arial", Font.PLAIN, 11));
                    String nom = parkingSurvole.getLibelleParking();
                    if (nom.length() > 25) {
                        nom = nom.substring(0, 22) + "...";
                    }
                    g2d.drawString(nom, getWidth() - 240, 90);
                    g2d.drawString("Places: " + parkingSurvole.getPlacesDisponibles() + "/" + 
                                  parkingSurvole.getNombrePlaces(), getWidth() - 240, 110);
                }
            }
        } catch (Exception e) {
            // Ignorer les exceptions lors du dessin de l'infobulle
        }
    }
    
    // ========== GETTERS ET SETTERS ==========
    
    public List<Parking> getParkings() {
        return new ArrayList<>(parkings); // Copie pour éviter les modifications externes
    }
    
    public Parking getParkingSelectionne() {
        return parkingSelectionne;
    }
    
    public void setParkingSelectionne(Parking parkingSelectionne) {
        this.parkingSelectionne = parkingSelectionne;
        repaint();
    }
    
    public double getZoom() { return zoom; }
    public void setZoom(double zoom) { this.zoom = zoom; repaint(); }
    
    public double getTranslateX() { return translateX; }
    public double getTranslateY() { return translateY; }
    
    public void setTranslation(double translateX, double translateY) {
        this.translateX = translateX;
        this.translateY = translateY;
        repaint();
    }
    
    public int getOriginalImageWidth() { return originalImageWidth; }
    public int getOriginalImageHeight() { return originalImageHeight; }
    
    public Point imageToScreenCoordinates(Point imagePoint) {
        return imageToScreen(imagePoint);
    }
    
    // ========== MÉTHODES POUR LES ZONES (APLATS PNG) ==========
    
    /**
     * AFFICHE UNE ZONE COLORÉE
     * @param nomZone Nom affiché de la zone
     * @param codeZone Code couleur (ZJ, ZR, etc.)
     * @param cheminImage Chemin vers le PNG dans /images/
     * @return true si chargement réussi
     */
    public boolean afficherZone(String nomZone, String codeZone, String cheminImage) {
        try {
            java.net.URL imageUrl = getClass().getResource(cheminImage);
            if (imageUrl != null) {
                zoneImage = ImageIO.read(imageUrl);
                zoneVisible = true;
                nomZoneActuelle = nomZone;
                codeZoneActuelle = codeZone;
                repaint();
                return true;
            } else {
                System.err.println("Fichier PNG non trouvé: " + cheminImage);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la zone " + nomZone + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * AFFICHE UNE ZONE AVEC OPACITÉ PERSONNALISÉE
     */
    public boolean afficherZone(String nomZone, String codeZone, String cheminImage, float opacite) {
        boolean success = afficherZone(nomZone, codeZone, cheminImage);
        if (success) {
            setZoneOpacity(opacite);
        }
        return success;
    }
    
    /**
     * CACHE LA ZONE ACTUELLE
     */
    public void cacherZone() {
        zoneVisible = false;
        nomZoneActuelle = null;
        codeZoneActuelle = null;
        repaint();
    }
    
    /**
     * CHANGE L'OPACITÉ DE LA ZONE
     * @param opacity Opacité entre 0.1 et 0.8 (10% à 80%)
     */
    public void setZoneOpacity(float opacity) {
        zoneOpacity = Math.max(0.1f, Math.min(0.8f, opacity));
        repaint();
    }
    
    /**
     * VÉRIFIE SI UNE ZONE EST AFFICHÉE
     */
    public boolean isZoneVisible() { return zoneVisible; }
    public String getZoneActuelle() { return nomZoneActuelle; }
    public String getCodeZoneActuelle() { return codeZoneActuelle; }
    public float getZoneOpacity() { return zoneOpacity; }
    
    /**
     * RENVOIE LA COULEUR ASSOCIÉE À UNE ZONE
     * Utilisé pour l'indicateur et la cohérence visuelle
     */
    public Color getCouleurZoneActuelle() {
        if (codeZoneActuelle == null) return null;
        
        switch (codeZoneActuelle) {
            case "ZJ": return new Color(255, 193, 7);    // Jaune
            case "ZR": return new Color(220, 53, 69);    // Rouge
            case "ZO": return new Color(255, 165, 0);    // Orange
            case "ZB": return new Color(52, 152, 219);   // Bleu
            case "ZV": return new Color(60, 179, 113);   // Vert
            default: return new Color(155, 89, 182);     // Violet (par défaut)
        }
    }
    
    /**
     * DESSINE L'INDICATEUR DE ZONE
     * Panneau d'information sur la zone actuellement affichée
     */
    private void dessinerIndicateurZone(Graphics2D g2d) {
        Color couleurZone = getCouleurZoneActuelle();
        if (couleurZone == null) return;
        
        int width = 220;
        int height = 90;
        int x = getWidth() - width - 20;
        int y = 60;
        
        // OMBRE
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.fillRoundRect(x + 2, y + 2, width, height, 15, 15);
        
        // FOND PRINCIPAL
        g2d.setColor(new Color(255, 255, 255, 240));
        g2d.fillRoundRect(x, y, width, height, 15, 15);
        
        // BORDURE COLORÉE
        g2d.setColor(couleurZone);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(x, y, width, height, 15, 15);
        
        // CARRÉ DE COULEUR (icône)
        g2d.setColor(couleurZone);
        g2d.fillRoundRect(x + 15, y + 15, 40, 40, 8, 8);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString(codeZoneActuelle, x + 25, y + 45);
        
        // INFORMATIONS TEXTE
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Zone affichée:", x + 70, y + 30);
        
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(couleurZone);
        g2d.drawString(nomZoneActuelle, x + 70, y + 55);
    }
    
    
}