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

public class CartePanel extends JPanel {
    private BufferedImage carteImage;
    private List<Parking> parkings = new ArrayList<>();
    private Parking parkingSelectionne = null;
    private String emailUtilisateur;
    
    // Variables pour le zoom et déplacement (protégées pour l'accès depuis CarteAdminPanel)
    protected double zoom = 1.0;
    protected double translateX = 0;
    protected double translateY = 0;
    private Point dragStart = null;
    
    // Taille originale de l'image
    private int originalImageWidth;
    private int originalImageHeight;
    
    public CartePanel(java.net.URL imageUrl, String emailUtilisateur) throws java.io.IOException {
        this.carteImage = ImageIO.read(imageUrl);
        this.originalImageWidth = carteImage.getWidth();
        this.originalImageHeight = carteImage.getHeight();
        this.emailUtilisateur = emailUtilisateur;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));
        chargerParkings();
        configurerInteractions();
    }
    
    private void chargerParkings() {
        try {
            parkings = ParkingDAO.getAllParkings();
            repaint();
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des parkings: " + e.getMessage());
            parkings = new ArrayList<>();
        }
    }
    
    public void rafraichirParkings() {
        chargerParkings();
        repaint();
    }
    
    // Méthode pour configurer les interactions (peut être surchargée par CarteAdminPanel)
    protected void configurerInteractions() {
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
                    // Pour le déplacement
                    dragStart = e.getPoint();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    dragStart = null;
                    setCursor(Cursor.getDefaultCursor());
                    
                    // Vérifier si on a cliqué sur un parking
                    Point clickPoint = e.getPoint();
                    Parking parkingClique = getParkingAtScreenPoint(clickPoint);
                    if (parkingClique != null) {
                        onParkingClicked(parkingClique); // Gestion du stationnement
                    }
                }
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart != null) {
                    // Calculer le déplacement
                    double dx = e.getX() - dragStart.getX();
                    double dy = e.getY() - dragStart.getY();
                    
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
    
    protected Parking getParkingAtScreenPoint(Point screenPoint) {
        // Convertir le point d'écran en coordonnées image
        Point imagePoint = screenToImage(screenPoint);
        
        for (Parking p : parkings) {
            if (p.getPositionX() != null && p.getPositionY() != null) {
                // Convertir les positions relatives (0.0-1.0) en pixels image
                int parkingX = (int)(p.getPositionX() * originalImageWidth);
                int parkingY = (int)(p.getPositionY() * originalImageHeight);
                
                // Vérifier si le clic est proche du point (tolérance de 10 pixels image)
                if (Math.abs(imagePoint.x - parkingX) < 10 && Math.abs(imagePoint.y - parkingY) < 10) {
                    return p;
                }
            }
        }
        return null;
    }
    
    private Point screenToImage(Point screenPoint) {
        // Convertir les coordonnées d'écran en coordonnées image
        double x = (screenPoint.x / zoom) - translateX;
        double y = (screenPoint.y / zoom) - translateY;
        return new Point((int)x, (int)y);
    }
    
    // Méthode pour convertir écran -> image (public pour CarteAdminPanel)
    public Point screenToImageCoordinates(Point screenPoint) {
        return screenToImage(screenPoint);
    }
    
    // Méthode appelée quand un utilisateur clique sur un parking
    protected void onParkingClicked(Parking parking) {
        parkingSelectionne = parking;
        repaint();
        
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
    
    protected void ouvrirPageStationnement(Parking parking) {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame != null) {
            Page_Garer_Parking pageGarer = new Page_Garer_Parking(emailUtilisateur, parking);
            pageGarer.setVisible(true);
            parentFrame.dispose();
        }
    }
    
    // Méthodes de contrôle du zoom et déplacement
    public void zoomIn() {
        double oldZoom = zoom;
        zoom = Math.min(5.0, zoom * 1.2);
        
        // Centrer le zoom
        Point center = new Point(getWidth() / 2, getHeight() / 2);
        double zoomFactor = zoom / oldZoom;
        translateX = center.x / zoom - (center.x / oldZoom - translateX) * zoomFactor;
        translateY = center.y / zoom - (center.y / oldZoom - translateY) * zoomFactor;
        
        repaint();
    }
    
    public void zoomOut() {
        double oldZoom = zoom;
        zoom = Math.max(0.5, zoom * 0.8);
        
        // Centrer le zoom
        Point center = new Point(getWidth() / 2, getHeight() / 2);
        double zoomFactor = zoom / oldZoom;
        translateX = center.x / zoom - (center.x / oldZoom - translateX) * zoomFactor;
        translateY = center.y / zoom - (center.y / oldZoom - translateY) * zoomFactor;
        
        repaint();
    }
    
    public void resetZoom() {
        zoom = 1.0;
        translateX = 0;
        translateY = 0;
        repaint();
    }
    
    public void panLeft() {
        translateX -= 50 / zoom;
        repaint();
    }
    
    public void panRight() {
        translateX += 50 / zoom;
        repaint();
    }
    
    public void panUp() {
        translateY -= 50 / zoom;
        repaint();
    }
    
    public void panDown() {
        translateY += 50 / zoom;
        repaint();
    }
    
    public void centerOnParking(Parking parking) {
        if (parking != null && parking.getPositionX() != null && parking.getPositionY() != null) {
            // Calculer la position du parking en pixels image
            int parkingX = (int)(parking.getPositionX() * originalImageWidth);
            int parkingY = (int)(parking.getPositionY() * originalImageHeight);
            
            // Centrer la vue sur ce point
            translateX = -parkingX + (getWidth() / (2 * zoom));
            translateY = -parkingY + (getHeight() / (2 * zoom));
            
            repaint();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (carteImage != null) {
            Graphics2D g2d = (Graphics2D) g;
            
            // Activer l'antialiasing pour un rendu lisse
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            
            // Sauvegarder la transformation actuelle
            AffineTransform originalTransform = g2d.getTransform();
            
            // Appliquer le zoom et la translation
            g2d.scale(zoom, zoom);
            g2d.translate(translateX, translateY);
            
            // Dessiner l'image
            g2d.drawImage(carteImage, 0, 0, originalImageWidth, originalImageHeight, this);
            
            // Dessiner les parkings
            for (Parking p : parkings) {
                if (p.getPositionX() != null && p.getPositionY() != null) {
                    dessinerParking(g2d, p);
                }
            }
            
            // Restaurer la transformation pour dessiner les informations
            g2d.setTransform(originalTransform);
            
            // Afficher les informations de zoom et position
            dessinerInfos(g2d);
        }
    }
    
    private void dessinerParking(Graphics2D g2d, Parking p) {
        // Calculer la position en pixels image
        int x = (int)(p.getPositionX() * originalImageWidth);
        int y = (int)(p.getPositionY() * originalImageHeight);
        
        // Choisir la couleur selon la disponibilité
        Color couleur;
        if (p.getPlacesDisponibles() == 0) {
            couleur = new Color(220, 53, 69); // Rouge
        } else if (p.getPlacesDisponibles() > p.getNombrePlaces() / 4) {
            couleur = new Color(40, 167, 69); // Vert
        } else {
            couleur = new Color(255, 193, 7); // Orange
        }
        
        // Taille du point - AUGMENTÉE pour plus de visibilité
        int pointSize = 22; // Augmenté de 15 à 22
        
        // Si sélectionné, halo jaune plus grand
        if (p == parkingSelectionne) {
            g2d.setColor(new Color(255, 255, 0, 100));
            g2d.fillOval(x - pointSize - 8, y - pointSize - 8, (pointSize + 8) * 2, (pointSize + 8) * 2);
        }
        
        // Ombre sous le point
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillOval(x - pointSize/2 + 3, y - pointSize/2 + 3, pointSize, pointSize);
        
        // Point principal - PLUS GROS
        g2d.setColor(couleur);
        g2d.fillOval(x - pointSize/2, y - pointSize/2, pointSize, pointSize);
        
        // Bordure blanche épaisse
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3)); // Épaissi de 2 à 3
        g2d.drawOval(x - pointSize/2, y - pointSize/2, pointSize, pointSize);
        
        // Bordure noire fine
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawOval(x - pointSize/2, y - pointSize/2, pointSize, pointSize);
        
        // Restaurer la transformation pour le texte
        AffineTransform originalTransform = g2d.getTransform();
        g2d.setTransform(new AffineTransform());
        
        // Convertir les coordonnées image en coordonnées écran
        Point screenPoint = imageToScreen(new Point(x, y));
        
        // Toujours afficher le nom au-dessus (même non sélectionné) - NOUVEAU
        String nomCourt = p.getLibelleParking();
        if (nomCourt.length() > 20) {
            nomCourt = nomCourt.substring(0, 17) + "...";
        }
        
        // Police plus grande pour le nom
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        int textWidth = g2d.getFontMetrics().stringWidth(nomCourt);
        
        // Fond pour le nom - toujours visible maintenant
        g2d.setColor(new Color(255, 255, 255, 230)); // Presque opaque
        g2d.fillRoundRect(screenPoint.x - textWidth/2 - 5, screenPoint.y - pointSize - 25, 
                         textWidth + 10, 22, 6, 6);
        
        // Bordure du fond
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(screenPoint.x - textWidth/2 - 5, screenPoint.y - pointSize - 25, 
                         textWidth + 10, 22, 6, 6);
        
        // Texte du nom
        g2d.setColor(Color.BLACK);
        g2d.drawString(nomCourt, screenPoint.x - textWidth/2, screenPoint.y - pointSize - 10);
        
        // Afficher les places disponibles en dessous - NOUVEAU
        String placesText = p.getPlacesDisponibles() + "/" + p.getNombrePlaces();
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        int placesWidth = g2d.getFontMetrics().stringWidth(placesText);
        
        // Fond pour les places
        g2d.setColor(new Color(255, 255, 255, 220));
        g2d.fillRoundRect(screenPoint.x - placesWidth/2 - 4, screenPoint.y + pointSize/2 + 5, 
                         placesWidth + 8, 16, 4, 4);
        
        // Texte des places
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawString(placesText, screenPoint.x - placesWidth/2, screenPoint.y + pointSize/2 + 16);
        
        // Icônes supplémentaires si parking spécial
        if (p.hasMoto()) {
            // Icône moto
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.drawString("Ⓜ", screenPoint.x - 15, screenPoint.y + pointSize/2 + 35);
        }
        
        if (p.isEstRelais()) {
            // Icône relais
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.drawString("Ⓡ", screenPoint.x + 5, screenPoint.y + pointSize/2 + 35);
        }
        
        // Si sélectionné, ajouter un effet spécial
        if (p == parkingSelectionne) {
            // Cadre jaune autour du point
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(screenPoint.x - pointSize/2 - 2, screenPoint.y - pointSize/2 - 2, 
                        pointSize + 4, pointSize + 4);
            
            // Étoile d'indication
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("★", screenPoint.x - 7, screenPoint.y - pointSize - 30);
        }
        
        // Restaurer la transformation
        g2d.setTransform(originalTransform);
    }

    
    private Point imageToScreen(Point imagePoint) {
        // Convertir les coordonnées image en coordonnées écran
        double x = (imagePoint.x + translateX) * zoom;
        double y = (imagePoint.y + translateY) * zoom;
        return new Point((int)x, (int)y);
    }
    
    private void dessinerInfos(Graphics2D g2d) {
        // Afficher les informations de contrôle en bas - AGRANDI
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fillRoundRect(10, getHeight() - 90, 280, 80, 10, 10);
        
        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        g2d.drawString("Contrôles :", 20, getHeight() - 70);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("• Molette : Zoom", 30, getHeight() - 52);
        g2d.drawString("• Clic-glisser : Déplacer", 30, getHeight() - 35);
        
        // Afficher le niveau de zoom
        String zoomText = String.format("Zoom: %.1fx", zoom);
        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        int zoomWidth = g2d.getFontMetrics().stringWidth(zoomText);
        g2d.setColor(new Color(52, 152, 219));
        g2d.fillRoundRect(getWidth() - zoomWidth - 35, 10, zoomWidth + 25, 30, 8, 8);
        g2d.setColor(Color.WHITE);
        g2d.drawString(zoomText, getWidth() - zoomWidth - 25, 32);
        
        // Légende
        String[] legendItems = {
            "Vert = Places disponibles",
            "Orange = Peu de places", 
            "Rouge = Complet",
            "Ⓜ = Parking moto",
            "Ⓡ = Parking relais"
        };
        
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fillRoundRect(getWidth() - 180, getHeight() - 130, 170, 130, 10, 10);
        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        g2d.drawString("Légende :", getWidth() - 170, getHeight() - 105);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        for (int i = 0; i < legendItems.length; i++) {
            g2d.drawString(legendItems[i], getWidth() - 165, getHeight() - 85 + (i * 18));
        }
        
        // Information sur le parking survolé - CORRECTION: vérifier si la souris est sur le composant
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
            // Ignorer les exceptions lors du dessin de l'infobulle de survol
        }
    }
    
    // Getters et setters
    public List<Parking> getParkings() {
        return new ArrayList<>(parkings);
    }
    
    public Parking getParkingSelectionne() {
        return parkingSelectionne;
    }
    
    public void setParkingSelectionne(Parking parkingSelectionne) {
        this.parkingSelectionne = parkingSelectionne;
        repaint();
    }
    
    public double getZoom() {
        return zoom;
    }
    
    public void setZoom(double zoom) {
        this.zoom = zoom;
        repaint();
    }
    
    public double getTranslateX() {
        return translateX;
    }
    
    public double getTranslateY() {
        return translateY;
    }
    
    public void setTranslation(double translateX, double translateY) {
        this.translateX = translateX;
        this.translateY = translateY;
        repaint();
    }
    
    public int getOriginalImageWidth() {
        return originalImageWidth;
    }

    public int getOriginalImageHeight() {
        return originalImageHeight;
    }
    
    // Méthode pour convertir les coordonnées image en coordonnées écran
    public Point imageToScreenCoordinates(Point imagePoint) {
        return imageToScreen(imagePoint);
    }
}