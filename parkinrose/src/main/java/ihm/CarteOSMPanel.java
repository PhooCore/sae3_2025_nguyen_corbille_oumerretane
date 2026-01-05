package ihm;

import javax.swing.*;
import java.awt.*;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import modele.Parking;
import modele.dao.ParkingDAO;
import java.sql.SQLException;
import java.util.List;

public class CarteOSMPanel extends JPanel {
    
    private JFXPanel fxPanel;
    private WebEngine webEngine;
    private String emailUtilisateur;
    private boolean initialisationEnCours = false;
    private ParkingDAO parkingDAO;
    
    public CarteOSMPanel(String emailUtilisateur) {
        this.emailUtilisateur = emailUtilisateur;
        this.parkingDAO = ParkingDAO.getInstance();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Afficher un message de chargement
        JLabel lblChargement = new JLabel("Chargement de la carte...", SwingConstants.CENTER);
        lblChargement.setFont(new Font("Arial", Font.PLAIN, 14));
        lblChargement.setForeground(Color.GRAY);
        add(lblChargement, BorderLayout.CENTER);
        
        // Initialiser JavaFX en arrière-plan
        SwingUtilities.invokeLater(() -> {
            initialiserCarte();
        });
    }
    
    private void initialiserCarte() {
        if (initialisationEnCours) {
            return;
        }
        
        initialisationEnCours = true;
        
        try {
            // Initialiser JavaFX
            JavaFXInitializer.initializeJavaFX();
            
            Platform.runLater(() -> {
                try {
                    creerFXPanel();
                } catch (Exception e) {
                    e.printStackTrace();
                    afficherErreur("Erreur JavaFX: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur d'initialisation: " + e.getMessage());
            initialisationEnCours = false;
        }
    }
    
    private void creerFXPanel() {
        try {
            fxPanel = new JFXPanel();
            
            Platform.runLater(() -> {
                try {
                    WebView webView = new WebView();
                    webEngine = webView.getEngine();
                    
                    // Configurer l'alerte pour le bouton "Stationner ici"
                    webEngine.setOnAlert(event -> {
                        String message = event.getData();
                        if (message.contains("stationnerParking:")) {
                            String[] parts = message.split(":");
                            if (parts.length >= 3) {
                                String idParking = parts[1];
                                String nomParking = parts[2];
                                ouvrirStationnement(idParking, nomParking);
                            }
                        }
                    });
                    
                    // Charger le contenu HTML
                    String html = genererHTML();
                    webEngine.loadContent(html);
                    
                    // Ajouter à la scène
                    fxPanel.setScene(new Scene(webView));
                    
                    // Ajouter au panel Swing
                    SwingUtilities.invokeLater(() -> {
                        try {
                            removeAll();
                            add(fxPanel, BorderLayout.CENTER);
                            revalidate();
                            repaint();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        initialisationEnCours = false;
                    });
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        afficherErreur("Erreur WebView: " + e.getMessage());
                        initialisationEnCours = false;
                    });
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                afficherErreur("Erreur JFXPanel: " + e.getMessage());
                initialisationEnCours = false;
            });
        }
    }
    
    private String genererHTML() {
        List<Parking> parkings = getParkingsFromDatabase();
      
        
        StringBuilder markersJS = new StringBuilder();
        int parkingsAvecCoords = 0;
        double minLat = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double minLng = Double.MAX_VALUE;
        double maxLng = Double.MIN_VALUE;
        
        for (Parking parking : parkings) {
            Float posX = parking.getPositionX();
            Float posY = parking.getPositionY();

            if (posX != null && posY != null && posX != 0.0f && posY != 0.0f) {
                String name = parking.getLibelleParking();
                String id = parking.getIdParking();
                double lat = posY;
                double lng = posX;
                
                // Mettre à jour les bornes pour le zoom
                minLat = Math.min(minLat, lat);
                maxLat = Math.max(maxLat, lat);
                minLng = Math.min(minLng, lng);
                maxLng = Math.max(maxLng, lng);
                
                // Préparer le contenu du popup
                StringBuilder popupContent = new StringBuilder();
                popupContent.append("<b>").append(escapeHtml(name)).append("</b><br>");
                
                String adresse = parking.getAdresseParking();
                if (adresse != null && !adresse.isEmpty()) {
                    popupContent.append("Adresse: ").append(escapeHtml(adresse)).append("<br>");
                }
                
                popupContent.append("Places: ").append(parking.getPlacesDisponibles())
                           .append("/").append(parking.getNombrePlaces());
                
                if (parking.hasMoto()) {
                    int placesMoto = parking.getPlacesMoto();
                    int placesMotoDisponibles = parking.getPlacesMotoDisponibles();
                    popupContent.append("<br>Places moto: ").append(placesMotoDisponibles)
                               .append("/").append(placesMoto);
                }
                
                if (parking.isEstRelais()) {
                    popupContent.append("<br><i>Parking Relais</i>");
                }
                
                // Bouton pour stationner
                popupContent.append("<br><br><button onclick=\"window.alert('stationnerParking:")
                            .append(id).append(":")
                            .append(escapeHtml(name)).append("');\" ")
                            .append("style=\"background:#4CAF50;color:white;padding:8px 16px;")
                            .append("border:none;border-radius:4px;cursor:pointer;font-weight:bold;\">")
                            .append("Stationner ici</button>");
                
                // Couleur du marqueur
                String markerColor = parking.isEstRelais() ? "#ff3333" : "#3388ff";
                
                // Ajouter le marqueur
                markersJS.append("L.circleMarker([").append(lat).append(", ").append(lng).append("], {")
                        .append("radius: 8,")
                        .append("fillColor: '").append(markerColor).append("',")
                        .append("color: '#000',")
                        .append("weight: 1,")
                        .append("opacity: 1,")
                        .append("fillOpacity: 0.8")
                        .append("})")
                        .append(".addTo(map)")
                        .append(".bindPopup('").append(popupContent.toString().replace("'", "\\'")).append("');\n");
                
                parkingsAvecCoords++;
            }
        }
        
        // Calculer les bornes pour le zoom
        boolean hasValidBounds = (minLat < maxLat && minLng < maxLng);
        String boundsJS = "";
        if (hasValidBounds && parkingsAvecCoords > 0) {
            boundsJS = "        var bounds = L.latLngBounds([\n" +
                      "            [" + (minLat - 0.01) + ", " + (minLng - 0.01) + "],\n" +
                      "            [" + (maxLat + 0.01) + ", " + (maxLng + 0.01) + "]\n" +
                      "        ]);\n" +
                      "        map.fitBounds(bounds, {padding: [50, 50]});\n";
        } else {
            // Coordonnées par défaut pour Toulouse
            boundsJS = "        map.setView([43.604652, 1.444209], 13);\n";
        }
        
        // Générer le HTML complet avec géolocalisation
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.css\" />\n" +
            "    <script src=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.js\"></script>\n" +
            "    <style>\n" +
            "        body { margin: 0; padding: 0; }\n" +
            "        #map { height: 100vh; width: 100vw; }\n" +
            "        .info-box {\n" +
            "            position: absolute;\n" +
            "            top: 10px;\n" +
            "            right: 10px;\n" +
            "            background: white;\n" +
            "            padding: 15px;\n" +
            "            border-radius: 8px;\n" +
            "            box-shadow: 0 0 15px rgba(0,0,0,0.3);\n" +
            "            z-index: 1000;\n" +
            "            font-family: Arial, sans-serif;\n" +
            "            max-width: 320px;\n" +
            "            font-size: 14px;\n" +
            "        }\n" +
            "        .legend {\n" +
            "            margin-top: 10px;\n" +
            "            padding-top: 10px;\n" +
            "            border-top: 1px solid #ddd;\n" +
            "        }\n" +
            "        .legend-item {\n" +
            "            display: flex;\n" +
            "            align-items: center;\n" +
            "            margin-bottom: 5px;\n" +
            "        }\n" +
            "        .legend-color {\n" +
            "            width: 20px;\n" +
            "            height: 20px;\n" +
            "            margin-right: 8px;\n" +
            "            border-radius: 50%;\n" +
            "        }\n" +
            "        .geoloc-btn {\n" +
            "            position: absolute;\n" +
            "            top: 50px;\n" +
            "            left: 10px;\n" +
            "            background: white;\n" +
            "            border: 2px solid rgba(0,0,0,0.2);\n" +
            "            border-radius: 5px;\n" +
            "            padding: 6px;\n" +
            "            z-index: 1000;\n" +
            "            cursor: pointer;\n" +
            "            box-shadow: 0 0 10px rgba(0,0,0,0.1);\n" +
            "        }\n" +
            "        .geoloc-btn:hover {\n" +
            "            background: #f8f9fa;\n" +
            "        }\n" +
            "        .geoloc-btn img {\n" +
            "            width: 20px;\n" +
            "            height: 20px;\n" +
            "        }\n" +
            "        .user-marker {\n" +
            "            background-color: #FFD700;\n" +
            "            border-radius: 50%;\n" +
            "            border: 3px solid white;\n" +
            "            box-shadow: 0 0 10px rgba(0,0,0,0.3);\n" +
            "        }\n" +
            "        .user-marker::after {\n" +
            "            content: '';\n" +
            "            position: absolute;\n" +
            "            top: 50%;\n" +
            "            left: 50%;\n" +
            "            width: 8px;\n" +
            "            height: 8px;\n" +
            "            background-color: #FF8C00;\n" +
            "            border-radius: 50%;\n" +
            "            transform: translate(-50%, -50%);\n" +
            "        }\n" +
            "        h3 { margin: 0 0 10px 0; color: #333; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div id=\"map\"></div>\n" +
            "    <div class=\"info-box\">\n" +
            "        <h3>Parkings de Toulouse</h3>\n" +
            "        <p><b>" + parkingsAvecCoords + " parkings</b> affichés</p>\n" +
            "        <p>Cliquez sur un point pour les détails</p>\n" +
            "        <div class=\"legend\">\n" +
            "            <div class=\"legend-item\">\n" +
            "                <div class=\"legend-color\" style=\"background-color: #3388ff;\"></div>\n" +
            "                <span>Parking normal</span>\n" +
            "            </div>\n" +
            "            <div class=\"legend-item\">\n" +
            "                <div class=\"legend-color\" style=\"background-color: #ff3333;\"></div>\n" +
            "                <span>Parking relais</span>\n" +
            "            </div>\n" +
            "        </div>\n" +  
            "    </div>\n" +
            "    <button class=\"geoloc-btn\" id=\"geolocBtn\" title=\"Centrer sur ma position\">\n" +
            "        <svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 24 24\" fill=\"#4285F4\">\n" +
            "            <path d=\"M12 8c-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4zm8.94 3A8.994 8.994 0 0 0 13 3.06V1h-2v2.06A8.994 8.994 0 0 0 3.06 11H1v2h2.06A8.994 8.994 0 0 0 11 20.94V23h2v-2.06A8.994 8.994 0 0 0 20.94 13H23v-2h-2.06zM12 19c-3.87 0-7-3.13-7-7s3.13-7 7-7 7 3.13 7 7-3.13 7-7 7z\"/>\n" +
            "        </svg>\n" +
            "    </button>\n" +
            "    <script>\n" +
            "        var map;\n" +
            "        var userMarker = null;\n" +
            "        var watchId = null;\n" +
            "        \n" +
            "        function initMap() {\n" +
            "            // Carte centrée sur Toulouse par défaut\n" +
            "            map = L.map('map').setView([43.604652, 1.444209], 13);\n" +
            "            \n" +
            "            // Tuiles OpenStreetMap\n" +
            "            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
            "                maxZoom: 19,\n" +
            "                attribution: '© OpenStreetMap contributors'\n" +
            "            }).addTo(map);\n" +
            "            \n" +
            "            // Ajouter les parkings\n" +
            markersJS.toString() +
            "            \n" +
            "            // Ajuster la vue pour voir tous les parkings\n" +
            boundsJS +
            "            \n" +
            "            // Configurer la géolocalisation\n" +
            "            setupGeolocation();\n" +
            "        }\n" +
            "        \n" +
            "        function setupGeolocation() {\n" +
            "            // Bouton de géolocalisation\n" +
            "            document.getElementById('geolocBtn').addEventListener('click', function() {\n" +
            "                locateUser(true); // true = centrer sur la position\n" +
            "            });\n" +
            "            \n" +
            "            // Essayer de géolocaliser automatiquement au démarrage\n" +
            "            setTimeout(function() {\n" +
            "                locateUser(false); // false = ne pas centrer automatiquement\n" +
            "            }, 1000);\n" +
            "        }\n" +
            "        \n" +
            "        function locateUser(centerOnUser) {\n" +
            "            if (!navigator.geolocation) {\n" +
            "                alert('Géolocalisation non supportée par votre navigateur');\n" +
            "                return;\n" +
            "            }\n" +
            "            \n" +
            "            var options = {\n" +
            "                enableHighAccuracy: true,\n" +
            "                timeout: 10000, // 10 secondes\n" +
            "                maximumAge: 0\n" +
            "            };\n" +
            "            \n" +
            "            navigator.geolocation.getCurrentPosition(\n" +
            "                function(position) {\n" +
            "                    var userLat = position.coords.latitude;\n" +
            "                    var userLng = position.coords.longitude;\n" +
            "                    var accuracy = position.coords.accuracy;\n" +
            "                    \n" +
            "                    updateUserPosition(userLat, userLng, accuracy, centerOnUser);\n" +
            "                    \n" +
            "                    // Démarrer le suivi de position\n" +
            "                    startTracking();\n" +
            "                },\n" +
            "                function(error) {\n" +
            "                    handleGeolocationError(error, centerOnUser);\n" +
            "                },\n" +
            "                options\n" +
            "            );\n" +
            "        }\n" +
            "        \n" +
            "        function updateUserPosition(lat, lng, accuracy, centerOnUser) {\n" +
            "            // Supprimer l'ancien marqueur si existant\n" +
            "            if (userMarker) {\n" +
            "                map.removeLayer(userMarker);\n" +
            "            }\n" +
            "            \n" +
            "            // Créer un marqueur jaune pour la position de l'utilisateur\n" +
            "            userMarker = L.circleMarker([lat, lng], {\n" +
            "                radius: 10,\n" +
            "                fillColor: '#FFD700', // Jaune\n" +
            "                color: '#ffffff',\n" +
            "                weight: 3,\n" +
            "                opacity: 1,\n" +
            "                fillOpacity: 0.9,\n" +
            "                className: 'user-marker'\n" +
            "            }).addTo(map);\n" +
            "            \n" +
            "            // Ajouter un cercle de précision (optionnel, plus discret)\n" +
            "            L.circle([lat, lng], {\n" +
            "                radius: accuracy,\n" +
            "                color: '#FFD700',\n" +
            "                fillColor: '#FFD700',\n" +
            "                fillOpacity: 0.1,\n" +
            "                weight: 1,\n" +
            "                dashArray: '5, 5'\n" +
            "            }).addTo(map);\n" +
            "            \n" +
            "            // Popup avec informations\n" +
            "            var popupContent = '<div style=\"text-align: center; padding: 10px; min-width: 200px;\">' +\n" +
            "                '<h4 style=\"margin-top: 0; color: #FF8C00;\">📍 Votre position</h4>' +\n" +
            "                '<p><b>Latitude:</b> ' + lat.toFixed(6) + '</p>' +\n" +
            "                '<p><b>Longitude:</b> ' + lng.toFixed(6) + '</p>' +\n" +
            "                '<p><small>Précision: ' + Math.round(accuracy) + ' mètres</small></p>' +\n" +
            "                '<p><small>' + new Date().toLocaleTimeString() + '</small></p>' +\n" +
            "                '</div>';\n" +
            "            \n" +
            "            userMarker.bindPopup(popupContent);\n" +
            "            \n" +
            "            // Centrer sur la position si demandé\n" +
            "            if (centerOnUser) {\n" +
            "                map.setView([lat, lng], 16); // Zoom plus élevé pour la position personnelle\n" +
            "                userMarker.openPopup();\n" +
            "            }\n" +
            "            \n" +
            "            // Mettre à jour le bouton\n" +
            "            updateGeolocButton(true);\n" +
            "            \n" +
            "            console.log('Position utilisateur: ', lat, lng, 'Précision:', accuracy, 'm');\n" +
            "        }\n" +
            "        \n" +
            "        function startTracking() {\n" +
            "            // Arrêter le suivi précédent si existant\n" +
            "            if (watchId) {\n" +
            "                navigator.geolocation.clearWatch(watchId);\n" +
            "            }\n" +
            "            \n" +
            "            // Démarrer le suivi continu (optionnel, peut être désactivé pour économiser la batterie)\n" +
            "            watchId = navigator.geolocation.watchPosition(\n" +
            "                function(position) {\n" +
            "                    var userLat = position.coords.latitude;\n" +
            "                    var userLng = position.coords.longitude;\n" +
            "                    var accuracy = position.coords.accuracy;\n" +
            "                    \n" +
            "                    updateUserPosition(userLat, userLng, accuracy, false);\n" +
            "                },\n" +
            "                function(error) {\n" +
            "                    console.error('Erreur suivi position:', error);\n" +
            "                },\n" +
            "                {\n" +
            "                    enableHighAccuracy: false, // Moins gourmand en batterie\n" +
            "                    maximumAge: 30000, // 30 secondes\n" +
            "                    timeout: 10000\n" +
            "                }\n" +
            "            );\n" +
            "        }\n" +
            "        \n" +
            "        function handleGeolocationError(error, centerOnUser) {\n" +
            "            var errorMessage = '';\n" +
            "            \n" +
            "            switch(error.code) {\n" +
            "                case error.PERMISSION_DENIED:\n" +
            "                    errorMessage = 'Permission de géolocalisation refusée.\\n' +\n" +
            "                                   'Autorisez la géolocalisation dans les paramètres de votre navigateur.';\n" +
            "                    break;\n" +
            "                case error.POSITION_UNAVAILABLE:\n" +
            "                    errorMessage = 'Position indisponible.\\n' +\n" +
            "                                   'Vérifiez votre connexion GPS ou Wi-Fi.';\n" +
            "                    break;\n" +
            "                case error.TIMEOUT:\n" +
            "                    errorMessage = 'Délai de géolocalisation dépassé.';\n" +
            "                    break;\n" +
            "                default:\n" +
            "                    errorMessage = 'Erreur inconnue de géolocalisation.';\n" +
            "            }\n" +
            "            \n" +
            "            // Afficher l'erreur seulement si l'utilisateur a demandé explicitement\n" +
            "            if (centerOnUser) {\n" +
            "                alert(errorMessage);\n" +
            "            }\n" +
            "            \n" +
            "            updateGeolocButton(false);\n" +
            "            console.warn('Erreur géolocalisation:', errorMessage);\n" +
            "        }\n" +
            "        \n" +
            "        function updateGeolocButton(isActive) {\n" +
            "            var btn = document.getElementById('geolocBtn');\n" +
            "            if (isActive) {\n" +
            "                btn.style.color = '#4285F4';\n" +
            "                btn.title = 'Position active - Cliquez pour recentrer';\n" +
            "            } else {\n" +
            "                btn.style.color = '#666';\n" +
            "                btn.title = 'Centrer sur ma position';\n" +
            "            }\n" +
            "        }\n" +
            "        \n" +
            "        initMap();\n" +
            "    </script>\n" +
            "</body>\n" +
            "</html>";
    }
    
    private void ouvrirStationnement(String idParking, String nomParking) {
        SwingUtilities.invokeLater(() -> {
            int choix = JOptionPane.showConfirmDialog(
                CarteOSMPanel.this,
                "Voulez-vous stationner dans le parking :\n" + nomParking + " ?",
                "Stationnement",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (choix == JOptionPane.YES_OPTION) {
                Parking parking = getParkingById(idParking);
                
                if (parking != null) {
                    if (emailUtilisateur == null || emailUtilisateur.isEmpty()) {
                        JOptionPane.showMessageDialog(CarteOSMPanel.this,
                            "Veuillez vous connecter d'abord pour stationner",
                            "Authentification requise",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    // Ouvrir la page de stationnement
                    Page_Garer_Parking page = new Page_Garer_Parking(emailUtilisateur, parking);
                    page.setVisible(true);
                    
                    // Fermer la fenêtre parente
                    Window parent = SwingUtilities.getWindowAncestor(CarteOSMPanel.this);
                    if (parent != null && parent instanceof JFrame) {
                        parent.dispose();
                    }
                } else {
                    JOptionPane.showMessageDialog(CarteOSMPanel.this,
                        "Parking non trouvé dans la base de données",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
    
    private void afficherErreur(String message) {
        removeAll();
        JLabel lblErreur = new JLabel("<html><center>" + message + "</center></html>", 
                                      SwingConstants.CENTER);
        lblErreur.setFont(new Font("Arial", Font.PLAIN, 12));
        lblErreur.setForeground(Color.RED);
        add(lblErreur, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
    
    // Méthode pour recharger la carte
    public void recharger() {
        SwingUtilities.invokeLater(() -> {
            if (webEngine != null) {
                Platform.runLater(() -> {
                    try {
                        String html = genererHTML();
                        webEngine.loadContent(html);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                // Si webEngine est null, réinitialiser complètement
                initialisationEnCours = false;
                initialiserCarte();
            }
        });
    }
    
    // Nettoyage des ressources
    public void nettoyer() {
        if (webEngine != null) {
            Platform.runLater(() -> {
                webEngine.load(null); // Décharger la page
            });
        }
    }
    
    // Méthode pour récupérer un parking par son ID en utilisant ParkingDAO
    private Parking getParkingById(String idParking) {
        try {
            Parking parking = parkingDAO.findById(idParking);
            if (parking == null) {
                System.err.println("Parking non trouvé: " + idParking);
            }
            return parking;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du parking: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Méthode pour récupérer tous les parkings en utilisant ParkingDAO
    private List<Parking> getParkingsFromDatabase() {
        try {
            List<Parking> parkings = parkingDAO.findAll();
            return parkings;
        } catch (SQLException e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }
    
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}