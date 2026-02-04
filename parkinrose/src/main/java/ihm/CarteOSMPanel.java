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
        
        // Initialiser JavaFX si ce n'est pas déjà fait
        JavaFXInitializer.initializeJavaFX();
        
        JLabel lblChargement = new JLabel("Chargement de la carte...", SwingConstants.CENTER);
        lblChargement.setFont(new Font("Arial", Font.PLAIN, 14));
        lblChargement.setForeground(Color.GRAY);
        add(lblChargement, BorderLayout.CENTER);
        
        SwingUtilities.invokeLater(() -> {
            initialiserCarte();
        });
    }
    
    private void initialiserCarte() {
        if (initialisationEnCours) return;
        initialisationEnCours = true;
        
        try {
            creerFXPanel();
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur d'initialisation: " + e.getMessage());
            initialisationEnCours = false;
        }
    }
    
    private void creerFXPanel() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Vérifier que JavaFX est bien initialisé
                if (!JavaFXInitializer.isJavaFXRunning()) {
                    JavaFXInitializer.initializeJavaFX();
                }
                
                fxPanel = new JFXPanel();
                fxPanel.setPreferredSize(new Dimension(getWidth(), getHeight()));
                
                removeAll();
                add(fxPanel, BorderLayout.CENTER);
                revalidate();
                repaint();
                
                Platform.runLater(() -> {
                    try {
                        initialiserJavaFXScene();
                    } catch (Exception e) {
                        e.printStackTrace();
                        SwingUtilities.invokeLater(() -> {
                            afficherErreur("Erreur JavaFX: " + e.getMessage());
                            initialisationEnCours = false;
                        });
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                afficherErreur("Erreur JFXPanel: " + e.getMessage());
                initialisationEnCours = false;
            }
        });
    }
    
    private void initialiserJavaFXScene() {
        try {
            WebView webView = new WebView();
            webEngine = webView.getEngine();
            
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
            
            String html = genererHTMLAvecZones();
            webEngine.loadContent(html);
            
            Scene scene = new Scene(webView);
            fxPanel.setScene(scene);
            
            initialisationEnCours = false;
            
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                afficherErreur("Erreur WebView: " + e.getMessage());
                initialisationEnCours = false;
            });
        }
    }
    
    private String genererHTMLAvecZones() {
        List<Parking> parkings = getParkingsFromDatabase();
        
        StringBuilder markersJS = new StringBuilder();
        int parkingsAvecCoords = 0;
        
        for (Parking parking : parkings) {
            Float posX = parking.getPositionX();
            Float posY = parking.getPositionY();

            if (posX != null && posY != null && posX != 0.0f && posY != 0.0f) {
                String name = parking.getLibelleParking();
                String id = parking.getIdParking();
                double lat = posY;
                double lng = posX;
                
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
                
                popupContent.append("<br><br><button onclick=\"window.alert('stationnerParking:")
                            .append(id).append(":")
                            .append(escapeHtml(name)).append("');\" ")
                            .append("style=\"background:#4CAF50;color:white;padding:8px 16px;")
                            .append("border:none;border-radius:4px;cursor:pointer;font-weight:bold;\">")
                            .append("Stationner ici</button>");
                
                String markerColor = parking.isEstRelais() ? "#ff3333" : "#3388ff";
                String targetLayer = parking.isEstRelais() ? "parkingsRelais" : "parkingsNormaux";
                
                markersJS.append("            L.circleMarker([").append(lat).append(", ").append(lng).append("], {")
                        .append("radius: 8,")
                        .append("fillColor: '").append(markerColor).append("',")
                        .append("color: '#fff',")
                        .append("weight: 2,")
                        .append("opacity: 1,")
                        .append("fillOpacity: 0.9")
                        .append("})")
                        .append(".addTo(").append(targetLayer).append(")")
                        .append(".bindPopup('").append(popupContent.toString().replace("'", "\\'")).append("');\n");
                
                parkingsAvecCoords++;
            }
        }
        
        // Définir les zones de stationnement de Toulouse
        String zonesJS = genererZonesToulouse();
        
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.css\" />\n" +
            "    <script src=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.js\"></script>\n" +
            "    <style>\n" +
            "        body { margin: 0; padding: 0; font-family: Arial, sans-serif; }\n" +
            "        #map { height: 100vh; width: 100vw; }\n" +
            "        \n" +
            "        /* Contrôle des couches */\n" +
            "        .leaflet-control-layers {\n" +
            "            background: rgba(255, 255, 255, 0.9);\n" +
            "            border-radius: 5px;\n" +
            "            padding: 10px !important;\n" +
            "            box-shadow: 0 0 10px rgba(0,0,0,0.2);\n" +
            "        }\n" +
            "        \n" +
            "        /* Légende */\n" +
            "        .info.legend {\n" +
            "            background: rgba(255, 255, 255, 0.9);\n" +
            "            padding: 15px;\n" +
            "            border-radius: 5px;\n" +
            "            box-shadow: 0 0 10px rgba(0,0,0,0.2);\n" +
            "            font-size: 12px;\n" +
            "            max-width: 250px;\n" +
            "        }\n" +
            "        \n" +
            "        .legend-title {\n" +
            "            font-weight: bold;\n" +
            "            margin-bottom: 10px;\n" +
            "            color: #2c3e50;\n" +
            "            border-bottom: 1px solid #ddd;\n" +
            "            padding-bottom: 5px;\n" +
            "        }\n" +
            "        \n" +
            "        .legend-item {\n" +
            "            display: flex;\n" +
            "            align-items: center;\n" +
            "            margin-bottom: 8px;\n" +
            "        }\n" +
            "        \n" +
            "        .legend-color {\n" +
            "            width: 20px;\n" +
            "            height: 20px;\n" +
            "            margin-right: 10px;\n" +
            "            border-radius: 3px;\n" +
            "            border: 1px solid #666;\n" +
            "        }\n" +
            "        \n" +
            "        .zone-details {\n" +
            "            font-size: 10px;\n" +
            "            color: #666;\n" +
            "            margin-left: 30px;\n" +
            "            margin-bottom: 5px;\n" +
            "        }\n" +
            "        \n" +
            "        .parking-legend {\n" +
            "            margin-top: 15px;\n" +
            "            padding-top: 10px;\n" +
            "            border-top: 1px solid #ddd;\n" +
            "        }\n" +
            "        \n" +
            "        .parking-item {\n" +
            "            display: flex;\n" +
            "            align-items: center;\n" +
            "            margin-bottom: 5px;\n" +
            "        }\n" +
            "        \n" +
            "        .parking-color {\n" +
            "            width: 15px;\n" +
            "            height: 15px;\n" +
            "            margin-right: 8px;\n" +
            "            border-radius: 50%;\n" +
            "            border: 1px solid #666;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div id=\"map\"></div>\n" +
            "    <script>\n" +
            "        var map;\n" +
            "        \n" +
            "        function initMap() {\n" +
            "            // Carte centrée sur Toulouse\n" +
            "            map = L.map('map').setView([43.604652, 1.444209], 13);\n" +
            "            \n" +
            "            // Tuiles OpenStreetMap\n" +
            "            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
            "                maxZoom: 19,\n" +
            "                attribution: '© OpenStreetMap contributors | Toulouse Métropole'\n" +
            "            }).addTo(map);\n" +
            "            \n" +
            "            // Créer les couches (zones d'abord, parkings ensuite)\n" +
            "            var zonesJaunes = L.layerGroup();\n" +
            "            var zonesOranges = L.layerGroup();\n" +
            "            var zonesRouges = L.layerGroup();\n" +
            "            var zonesVertes = L.layerGroup();\n" +
            "            var zonesBleues = L.layerGroup();\n" +
            "            var parkingsNormaux = L.layerGroup();\n" +
            "            var parkingsRelais = L.layerGroup();\n" +
            "            \n" +
            "            // Ajouter les zones de stationnement EN PREMIER\n" +
            zonesJS +
            "            \n" +
            "            // Ajouter les marqueurs de parkings ENSUITE (au-dessus)\n" +
            markersJS.toString() +
            "            \n" +
            "            // Contrôle des couches\n" +
            "            var baseLayers = {\n" +
            "                \"Carte OpenStreetMap\": L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
            "                    maxZoom: 19,\n" +
            "                    attribution: '© OpenStreetMap contributors'\n" +
            "                })\n" +
            "            };\n" +
            "            \n" +
            "            var overlayLayers = {\n" +
            "                \"Zone Jaune – Centre-ville\": zonesJaunes,\n" +
            "                \"Zone Orange – Faubourgs\": zonesOranges,\n" +
            "                \"Zone Rouge – Commerciale\": zonesRouges,\n" +
            "                \"Zone Verte – Moyenne durée\": zonesVertes,\n" +
            "                \"Zone Bleue – Gratuite\": zonesBleues,\n" +
            "                \"Parkings normaux\": parkingsNormaux,\n" +
            "                \"Parkings relais\": parkingsRelais\n" +
            "            };\n" +
            "            \n" +
            "            L.control.layers(baseLayers, overlayLayers, {\n" +
            "                collapsed: true,\n" +
            "                position: 'topright'\n" +
            "            }).addTo(map);\n" +
            "            \n" +
            "            // Ajouter les couches par défaut dans le bon ordre\n" +
            "            zonesJaunes.addTo(map);\n" +
            "            zonesOranges.addTo(map);\n" +
            "            parkingsNormaux.addTo(map);\n" +
            "            parkingsRelais.addTo(map);\n" +
            "            \n" +
            "            // Ajouter une légende\n" +
            "            var legend = L.control({position: 'bottomright'});\n" +
            "            legend.onAdd = function(map) {\n" +
            "                var div = L.DomUtil.create('div', 'info legend');\n" +
            "                div.innerHTML = '<div class=\"legend-title\">Zones de stationnement</div>' +\n" +
            "                               \n" +
            "                               '<div class=\"legend-item\">' +\n" +
            "                               '  <div class=\"legend-color\" style=\"background-color: #f1c40f;\"></div>' +\n" +
            "                               '  <div><strong>Zone Jaune</strong><br><span class=\"zone-details\">Centre-ville • 2h30 max • 1,50€/h</span></div>' +\n" +
            "                               '</div>' +\n" +
            "                               \n" +
            "                               '<div class=\"legend-item\">' +\n" +
            "                               '  <div class=\"legend-color\" style=\"background-color: #e67e22;\"></div>' +\n" +
            "                               '  <div><strong>Zone Orange</strong><br><span class=\"zone-details\">Faubourgs • 5h max • 1€/h</span></div>' +\n" +
            "                               '</div>' +\n" +
            "                               \n" +
            "                               '<div class=\"legend-item\">' +\n" +
            "                               '  <div class=\"legend-color\" style=\"background-color: #e74c3c;\"></div>' +\n" +
            "                               '  <div><strong>Zone Rouge</strong><br><span class=\"zone-details\">Commerciale • 3h max • 1€/h</span></div>' +\n" +
            "                               '</div>' +\n" +
            "                               \n" +
            "                               '<div class=\"legend-item\">' +\n" +
            "                               '  <div class=\"legend-color\" style=\"background-color: #27ae60;\"></div>' +\n" +
            "                               '  <div><strong>Zone Verte</strong><br><span class=\"zone-details\">Moyenne durée • 5h max • 0,50€/h</span></div>' +\n" +
            "                               '</div>' +\n" +
            "                               \n" +
            "                               '<div class=\"legend-item\">' +\n" +
            "                               '  <div class=\"legend-color\" style=\"background-color: #2980b9;\"></div>' +\n" +
            "                               '  <div><strong>Zone Bleue</strong><br><span class=\"zone-details\">Gratuite • 1h30 max • Disque requis</span></div>' +\n" +
            "                               '</div>' +\n" +
            "                               \n" +
            "                               '<div class=\"parking-legend\">' +\n" +
            "                               '  <div class=\"legend-title\" style=\"font-size: 11px;\">Type de Parkings</div>' +\n" +
            "                               '  <div class=\"parking-item\">' +\n" +
            "                               '    <div class=\"parking-color\" style=\"background-color: #3388ff;\"></div>' +\n" +
            "                               '    <span>Parking normal</span>' +\n" +
            "                               '  </div>' +\n" +
            "                               '  <div class=\"parking-item\">' +\n" +
            "                               '    <div class=\"parking-color\" style=\"background-color: #ff3333;\"></div>' +\n" +
            "                               '    <span>Parking relais</span>' +\n" +
            "                               '  </div>' +\n" +
            "                               '  <div style=\"margin-top: 10px; font-size: 11px; color: #666; text-align: center;\">' +\n" +
            "                               '    <strong>Total: " + parkingsAvecCoords + " parkings</strong>' +\n" +
            "                               '  </div>' +\n" +
            "                               '</div>';\n" +
            "                return div;\n" +
            "            };\n" +
            "            legend.addTo(map);\n" +
            "        }\n" +
            "        \n" +
            "        initMap();\n" +
            "        \n" +
            "        window.onresize = function() {\n" +
            "            if (map) map.invalidateSize();\n" +
            "        };\n" +
            "    </script>\n" +
            "</body>\n" +
            "</html>";
    }
    
    private String genererZonesToulouse() {
        // Zones de stationnement réglementé basées sur la carte officielle de Toulouse
        return 
            "            // ZONE JAUNE – HYPERCENTRE (coordonnées officielles)\n" +
            "            L.polygon([\n" +
            "                [43.60905538722018, 1.412066707877633],\n" +
            "                [43.609868582316054, 1.413963511994583],\n" +
            "                [43.614331924055854, 1.4331561752687307],\n" +
            "                [43.61570519333164, 1.436176087086507],\n" +
            "                [43.61601236770435, 1.438921461855635],\n" +
            "                [43.615199255679016, 1.4415670044398028],\n" +
            "                [43.614928215926724, 1.4431643134135943],\n" +
            "                [43.615325740526714, 1.447082711392031],\n" +
            "                [43.61330195156228, 1.4511758150128187],\n" +
            "                [43.608486925567924, 1.4547490117732986],\n" +
            "                [43.60503402853814, 1.4564991889558725],\n" +
            "                [43.60557380667348, 1.4569450973970108],\n" +
            "                [43.60089352551005, 1.4568307028868166],\n" +
            "                [43.597248452590584, 1.4556867577848767],\n" +
            "                [43.596171456968364, 1.4539136428768695],\n" +
            "                [43.5967513800833, 1.4519117389484744],\n" +
            "                [43.59546725710077, 1.4513397663975043],\n" +
            "                [43.59314748160497, 1.4436181369594088],\n" +
            "                [43.59468020043773, 1.4404150906739766],\n" +
            "                [43.59944380704687, 1.4407010769494617],\n" +
            "                [43.60362718528441, 1.434523773398985],\n" +
            "                [43.60404136532343, 1.427717300042442]\n" +
            "            ], {\n" +
            "                color: '#f1c40f',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#f1c40f',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-jaune'\n" +
            "            }).addTo(zonesJaunes).bindTooltip('<b>Zone Jaune – Centre-ville</b><br>Durée max: 2h30<br>Tarif: 1,50€/h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE ORANGE-1 – FAUBOURGS (coordonnées officielles)\n" +
            "            L.polygon([\n" +
            "                [43.6057610577198, 1.4183905585677854],\n" +
            "                [43.60324781555284, 1.427811206480238],\n" +
            "                [43.60152438881085, 1.4279103711951058],\n" +
            "                [43.592762872853534, 1.4353477248102],\n" +
            "                [43.57990555272465, 1.4275137123356343],\n" +
            "                [43.5793635564865, 1.4234602156304306],\n" +
            "                [43.57998754758685, 1.4212626160032948],\n" +
            "                [43.58154749702441, 1.4219693817032464],\n" +
            "                [43.58314740319135, 1.4188551953259554],\n" +
            "                [43.58369677068654, 1.4150098721936348],\n" +
            "                [43.58735236340336, 1.4178258917832962],\n" +
            "                [43.59014390287026, 1.4158601996726043],\n" +
            "                [43.59131167184663, 1.419018558913973],\n" +
            "                [43.59295183688894, 1.4190317428264712],\n" +
            "                [43.59580351760828, 1.4192785060360933],\n" +
            "                [43.59640957946367, 1.418784979603029],\n" +
            "                [43.596712608109115, 1.419643286441466],\n" +
            "                [43.59691462635375, 1.4194501674024411],\n" +
            "                [43.59635518955324, 1.4165748394880673],\n" +
            "                [43.59630079958935, 1.4141823091037171],\n" +
            "                [43.59898138948388, 1.4141072072077623],\n" +
            "                [43.59918340011146, 1.4142681397402832],\n" +
            "                [43.5999914358583, 1.4148689545618525],\n" +
            "                [43.60127339338626, 1.4117575921140653],\n" +
            "                [43.60138216428018, 1.4122511185471294],\n" +
            "                [43.602664092159536, 1.4121867455447268],\n" +
            "                [43.60217463201764, 1.41482603907807],\n" +
            "                [43.60247763162264, 1.4147938525715658],\n" +
            "                [43.602267862827894, 1.4155555998921647],\n" +
            "                [43.60310693363153, 1.4161886012632174],\n" +
            "                [43.60334000677428, 1.4158989227046799],\n" +
            "                [43.60332446859285, 1.416392449137744],\n" +
            "                [43.60304478064096, 1.4165104663282595],\n" +
            "                [43.60214355506187, 1.4179803167919502],\n" +
            "                [43.60192601581424, 1.4178944861313831],\n" +
            "                [43.60149093499687, 1.4202655588737567]\n" +
            "            ], {\n" +
            "                color: '#e67e22',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#e67e22',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-orange'\n" +
            "            }).addTo(zonesOranges).bindTooltip('<b>Zone Orange – Faubourgs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 5h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE ORANGE-2 – FAUBOURGS (coordonnées officielles)\n" +
            "            L.polygon([\n" +
            "                [43.59348050674038, 1.4420742853489066],\n" +
            "                [43.59165235823003, 1.4411164005692858],\n" +
            "                [43.59086483099776, 1.4409740122932364],\n" +
            "                [43.589758525305314, 1.440287959639944],\n" +
            "                [43.588478465339094, 1.4394817657312713],\n" +
            "                [43.58838797012156, 1.4391381815285318],\n" +
            "                [43.58752825876902, 1.438857067180836],\n" +
            "                [43.58723414417159, 1.4388414497170756],\n" +
            "                [43.58646491453877, 1.4389195370643173],\n" +
            "                [43.584824612723786, 1.4394036784392792],\n" +
            "                [43.584270293734505, 1.4390913291640617],\n" +
            "                [43.583840410150394, 1.444088917656105],\n" +
            "                [43.58003920094779, 1.443417366748513],\n" +
            "                [43.57827427223605, 1.4432143397919357],\n" +
            "                [43.57813850634825, 1.4434329842845879],\n" +
            "                [43.57790091528922, 1.4431518699482673],\n" +
            "                [43.57619249496509, 1.443183104875789],\n" +
            "                [43.57587569643403, 1.444338797262345],\n" +
            "                [43.57371463359727, 1.4505545480634245],\n" +
            "                [43.57287734233235, 1.453381309062644],\n" +
            "                [43.57268498999586, 1.4547556458736008],\n" +
            "                [43.574563227697396, 1.4570826479739711],\n" +
            "                [43.576215123324204, 1.4549430555045457],\n" +
            "                [43.57649797761317, 1.4552710222435243],\n" +
            "                [43.576905285455105, 1.4546307062293287],\n" +
            "                [43.57709762431164, 1.4541465648527414],\n" +
            "                [43.57790091528866, 1.4548337333305335],\n" +
            "                [43.57940564273143, 1.4549274380960362],\n" +
            "                [43.58072931923578, 1.4547556460027917],\n" +
            "                [43.58206427975915, 1.4546463237564653],\n" +
            "                [43.58311638879306, 1.457441849847663],\n" +
            "                [43.5828788173958, 1.458160253180663],\n" +
            "                [43.58582011155116, 1.4604247856006796],\n" +
            "                [43.5859897972111, 1.4609557793685493],\n" +
            "                [43.587243966753974, 1.4603081749075457],\n" +
            "                [43.58778988951809, 1.4602516472847948],\n" +
            "                [43.59116085297631, 1.4592906777676362],\n" +
            "                [43.592170740508614, 1.4602139622970691],\n" +
            "                [43.59555510482767, 1.4585558186356085],\n" +
            "                [43.597247215647016, 1.4580470700328139],\n" +
            "                [43.59791585920259, 1.4580847551146476],\n" +
            "                [43.59765659013445, 1.4591587799469081],\n" +
            "                [43.60144999461384, 1.459592158425247],\n" +
            "                [43.60130241472484, 1.4609296707782409],\n" +
            "                [43.60097662876569, 1.4622889075984602],\n" +
            "                [43.60344599432917, 1.4634483218129573],\n" +
            "                [43.60372584936291, 1.4637211251466913],\n" +
            "                [43.60379169741692, 1.4635847234798243],\n" +
            "                [43.603643539194, 1.4634483218129573],\n" +
            "                [43.60449955946558, 1.461811501805821],\n" +
            "                [43.60457609978755, 1.4616582803750517],\n" +
            "                [43.60493084795485, 1.4613944848022158],\n" +
            "                [43.60513550940747, 1.4619409184888046],\n" +
            "                [43.60535381418946, 1.4617901781614697],\n" +
            "                [43.604985424410316, 1.4613191146385485],\n" +
            "                [43.604985424410316, 1.461281429556715],\n" +
            "                [43.60587228487154, 1.4604335152154564],\n" +
            "                [43.60778240150739, 1.4598117113269686],\n" +
            "                [43.60835542465514, 1.4600566643588875],\n" +
            "                [43.60888751269004, 1.4598682389497188],\n" +
            "                [43.60948781153865, 1.4611683744063015],\n" +
            "                [43.61014267612245, 1.46405128316658],\n" +
            "                [43.61020346674879, 1.4641531126075122],\n" +
            "                [43.61101239656496, 1.4629190974105508],\n" +
            "                [43.61214153070954, 1.4642355647648093],\n" +
            "                [43.61228816995223, 1.4636482177913708],\n" +
            "                [43.61315333422394, 1.4628380840422803],\n" +
            "                [43.61350526188474, 1.463101377513132],\n" +
            "                [43.61371055206913, 1.462919097417927],\n" +
            "                [43.61347593465832, 1.4624127638201354],\n" +
            "                [43.6134172801626, 1.4623114971005773],\n" +
            "                [43.61287472336524, 1.4619266835662554],\n" +
            "                [43.6122441782347, 1.4604076827728802],\n" +
            "                [43.613109343121536, 1.459395015577297],\n" +
            "                [43.614590359253285, 1.4619469369070057],\n" +
            "                [43.615000931512924, 1.4618659235313591],\n" +
            "                [43.61603859374477, 1.4614561073591743],\n" +
            "                [43.616792092634235, 1.4618254168016147],\n" +
            "                [43.616779939509485, 1.4602810316874177],\n" +
            "                [43.61739974614048, 1.4597102806701816],\n" +
            "                [43.617630652859795, 1.45934097118844],\n" +
            "                [43.61848135422558, 1.46217793951406],\n" +
            "                [43.61893100577251, 1.4615736149075742],\n" +
            "                [43.621106299575096, 1.4578805199923461],\n" +
            "                [43.621495170618125, 1.457679078456851],\n" +
            "                [43.62219999301526, 1.4558157440771031],\n" +
            "                [43.622710376458606, 1.4546742420426297],\n" +
            "                [43.623427336358105, 1.4543720797393869],\n" +
            "                [43.623512398829625, 1.4539691966683963],\n" +
            "                [43.623864799239314, 1.4541706381602242],\n" +
            "                [43.624034922824, 1.4535159531698645],\n" +
            "                [43.6238161924124, 1.4516358321719085],\n" +
            "                [43.62392555772459, 1.4500914469826036],\n" +
            "                [43.62422934919535, 1.4490506657158782],\n" +
            "                [43.62494629097966, 1.4477412957351588],\n" +
            "                [43.61615199294434, 1.4529091095934994],\n" +
            "                [43.61578914060464, 1.4525848092992228],\n" +
            "                [43.61566107455022, 1.4518772450208013],\n" +
            "                [43.61538359716334, 1.4517298357961304],\n" +
            "                [43.61615199294434, 1.44907646975205],\n" +
            "                [43.61672828333658, 1.4485457965432338],\n" +
            "                [43.61881995763369, 1.4459808758683035],\n" +
            "                [43.618030252425115, 1.4438876648779735],\n" +
            "                [43.61797876819162, 1.4439347645448197],\n" +
            "                [43.61862529551169, 1.4409713023145727],\n" +
            "                [43.62026745753383, 1.441254849129336],\n" +
            "                [43.62154699072124, 1.4369691292361535],\n" +
            "                [43.626409671153276, 1.4369379095179657],\n" +
            "                [43.626934380306324, 1.4340383664285783],\n" +
            "                [43.625884957469644, 1.431242378462514],\n" +
            "                [43.62161221792339, 1.4335488214258665],\n" +
            "                [43.620791160848874, 1.4317665271806066],\n" +
            "                [43.620116712749294, 1.4321513407149282],\n" +
            "                [43.619559554264875, 1.431726020492783],\n" +
            "                [43.619090364131196, 1.4295184059221158],\n" +
            "                [43.614706192179305, 1.4333867946903784],\n" +
            "                [43.6158059308832, 1.4362020094941006],\n" +
            "                [43.616187168939305, 1.438470384012207],\n" +
            "                [43.61596722420019, 1.4405969852725566],\n" +
            "                [43.615292721989164, 1.4430881465736916],\n" +
            "                [43.615600647813785, 1.4468957755557292],\n" +
            "                [43.613621096908474, 1.4514325245919426],\n" +
            "                [43.611817449323595, 1.4524046851492263],\n" +
            "                [43.609427166331926, 1.4540654594089903],\n" +
            "                [43.60514494948045, 1.456617380848283],\n" +
            "                [43.597199948648424, 1.4559521293140232],\n" +
            "                [43.59622663285682, 1.4538262189455686],\n" +
            "                [43.59491705585987, 1.4536062971833146],\n" +
            "                [43.59525330132168, 1.4514315153121369],\n" +
            "                [43.59551875693804, 1.45116272204716]\n" +
            "            ], {\n" +
            "                color: '#e67e22',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#e67e22',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-orange'\n" +
            "            }).addTo(zonesOranges).bindTooltip('<b>Zone Orange – Faubourgs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 5h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE VERTE-1 – Quartiers résidentiels\n" +
            "            L.polygon([\n" +
            "                [43.59358557206674, 1.4460858451905756],\n" +
            "                [43.59368311334924, 1.4459194766406545],\n" +
            "                [43.59553063016266, 1.4513145711594106],\n" +
            "                [43.59539292900436, 1.451425483526025]\n" +
            "            ], {\n" +
            "                color: '#27ae60',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#27ae60',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-verte'\n" +
            "            }).addTo(zonesVertes).bindTooltip('<b>Zone Verte – Quartiers résidentiels</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 7h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE VERTE-2 – Quartiers résidentiels (cercle complété)\n" +
            "            L.polygon([\n" +
            "                [43.59695946661658, 1.4548366875255707],\n" +
            "                [43.59684910550864, 1.4549183241944417],\n" +
            "                [43.59672691976017, 1.4547713781904739],\n" +
            "                [43.596655973082676, 1.4547496084121085],\n" +
            "                [43.596588967810476, 1.4551088097551406],\n" +
            "                [43.59663626565742, 1.455375489540119],\n" +
            "                [43.59681363225239, 1.4556149571021404],\n" +
            "                [43.596943700756434, 1.4556476117696886],\n" +
            "                [43.59696734954513, 1.4553373924279793],\n" +
            "                // Points supplémentaires pour former un cercle\n" +
            "                [43.59692901234567, 1.4551420123456789],\n" +
            "                [43.59686098765432, 1.4549876543210987],\n" +
            "                [43.59676543210987, 1.4548765432109876],\n" +
            "                [43.59667890123456, 1.4549012345678901],\n" +
            "                [43.59661234567891, 1.4550123456789012]\n" +
            "            ], {\n" +
            "                color: '#27ae60',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#27ae60',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-verte'\n" +
            "            }).addTo(zonesVertes).bindTooltip('<b>Zone Verte – Quartiers résidentiels</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 7h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE VERTE-3 – Quartiers résidentiels\n" +
            "            L.polygon([\n" +
            "                [43.61566806803978, 1.465205761077004],\n" +
            "                [43.61571650839232, 1.4651197363295847],\n" +
            "                [43.6155158266777, 1.46515796955066],\n" +
            "                [43.61546046608685, 1.4650719448032408],\n" +
            "                [43.61507986064503, 1.4646991708977575],\n" +
            "                [43.614898173139004, 1.4644721153296807],\n" +
            "                [43.61471691955776, 1.464216320433885],\n" +
            "                [43.61468539714002, 1.4640802593191],\n" +
            "                [43.614618411947504, 1.4641455686541969],\n" +
            "                [43.61467751653303, 1.4657783020316153],\n" +
            "                [43.614543546055806, 1.4660558667057764],\n" +
            "                [43.61477996434362, 1.4664041831596257],\n" +
            "                [43.615118828935316, 1.4660830789287334],\n" +
            "                [43.61516217194304, 1.4656150286938734],\n" +
            "                [43.615521667583984, 1.4653050783209818]\n" +
            "            ], {\n" +
            "                color: '#27ae60',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#27ae60',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-verte'\n" +
            "            }).addTo(zonesVertes).bindTooltip('<b>Zone Verte – Quartiers résidentiels</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 7h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE ROUGE-1 – Zones très limitées\n" +
            "            L.polygon([\n" +
            "                [43.5849413201824, 1.4289439954572685],\n" +
            "                [43.585528593930356, 1.428249058254459],\n" +
            "                [43.58506117243301, 1.4278023129097956],\n" +
            "                [43.5849413201824, 1.4279512280246833],\n" +
            "                [43.58117783808131, 1.4248240105820955],\n" +
            "                [43.581093936119004, 1.4251880253073772],\n" +
            "                [43.58485042414063, 1.4282666954529597]\n" +
            "            ], {\n" +
            "                color: '#e74c3c',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#e74c3c',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-rouge'\n" +
            "            }).addTo(zonesRouges).bindTooltip('<b>Zone Rouge – Stationnement très limité</b><br>Durée max: 1h30<br>Tarif: 2,50€/h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE ROUGE-2 – Zones très limitées\n" +
            "            L.polygon([\n" +
            "                [43.58945258628733, 1.4320888506541845],\n" +
            "                [43.5895364766003, 1.4317579281766561],\n" +
            "                [43.59248454768356, 1.4341736624291745],\n" +
            "                [43.59290397694903, 1.4339751089426573],\n" +
            "                [43.59298786245136, 1.4347196845170962],\n" +
            "                [43.59284405866153, 1.4346865922693435],\n" +
            "                [43.592832074996835, 1.43447149265895],\n" +
            "                [43.592676287139064, 1.434554223278332],\n" +
            "                [43.59266430344098, 1.4348685996319839],\n" +
            "                [43.592520498877995, 1.4349182380036134],\n" +
            "                [43.592376693971374, 1.434554223278332]\n" +
            "            ], {\n" +
            "                color: '#e74c3c',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#e74c3c',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-rouge'\n" +
            "            }).addTo(zonesRouges).bindTooltip('<b>Zone Rouge – Stationnement très limité</b><br>Durée max: 1h30<br>Tarif: 2,50€/h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE ROUGE-3 – Zones très limitées\n" +
            "            L.polygon([\n" +
            "                [43.59785132069692, 1.4296842906798493],\n" +
            "                [43.59886298337823, 1.428889471878718],\n" +
            "                [43.599233925557684, 1.4289623039905],\n" +
            "                [43.59931337744675, 1.4282765989914679],\n" +
            "                [43.59883666453854, 1.428166886191623],\n" +
            "                [43.598727417298434, 1.4287428783908096],\n" +
            "                [43.59777397841995, 1.429565724389648],\n" +
            "                [43.59657222661395, 1.4232983799136143],\n" +
            "                [43.598498990582684, 1.4186904421138171],\n" +
            "                [43.59836987946214, 1.4185533011140108],\n" +
            "                [43.59645304331548, 1.42262638880826],\n" +
            "                [43.59548963626255, 1.4172230328450641],\n" +
            "                [43.595281061576124, 1.4171407482451803],\n" +
            "                [43.59753561632151, 1.42993600487817],\n" +
            "                [43.5978732956565, 1.4299222907781894]\n" +
            "            ], {\n" +
            "                color: '#e74c3c',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#e74c3c',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-rouge'\n" +
            "            }).addTo(zonesRouges).bindTooltip('<b>Zone Rouge – Stationnement très limité</b><br>Durée max: 1h30<br>Tarif: 2,50€/h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE ROUGE-4 – Zones très limitées\n" +
            "            L.polygon([\n" +
            "                [43.6036390161948, 1.4074209371926512],\n" +
            "                [43.60345109114245, 1.4071214925892277],\n" +
            "                [43.60246808899477, 1.40933738265456],\n" +
            "                [43.60272829700875, 1.4092974567074368]\n" +
            "            ], {\n" +
            "                color: '#e74c3c',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#e74c3c',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-rouge'\n" +
            "            }).addTo(zonesRouges).bindTooltip('<b>Zone Rouge – Stationnement très limité</b><br>Durée max: 1h30<br>Tarif: 2,50€/h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE ROUGE-5 – Zones très limitées\n" +
            "            L.polygon([\n" +
            "                [43.61890723828373, 1.3970377321724545],\n" +
            "                [43.61883903350533, 1.3972355788154747],\n" +
            "                [43.6184980084528, 1.397471110533356],\n" +
            "                [43.618463905841146, 1.3973674765774882],\n" +
            "                [43.61866852122084, 1.3970754172473154],\n" +
            "                [43.61884585398664, 1.3970565747098849]\n" +
            "            ], {\n" +
            "                color: '#e74c3c',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#e74c3c',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-rouge'\n" +
            "            }).addTo(zonesRouges).bindTooltip('<b>Zone Rouge – Stationnement très limité</b><br>Durée max: 1h30<br>Tarif: 2,50€/h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE ROUGE-6 – Zones très limitées\n" +
            "            L.polygon([\n" +
            "                [43.617480064989664, 1.4080533015543009],\n" +
            "                [43.61744714846818, 1.4079168999126712],\n" +
            "                [43.61725787811978, 1.4079168999126712],\n" +
            "                [43.61717558647814, 1.4080192011438935],\n" +
            "                [43.61712621143907, 1.4081897031959296],\n" +
            "                [43.61720027398245, 1.4083488384444973],\n" +
            "                [43.61728256559031, 1.408382938854905],\n" +
            "                [43.61742246106522, 1.40831473803409],\n" +
            "                [43.61745537760023, 1.408110135571646]\n" +
            "            ], {\n" +
            "                color: '#e74c3c',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#e74c3c',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-rouge'\n" +
            "            }).addTo(zonesRouges).bindTooltip('<b>Zone Rouge – Stationnement très limité</b><br>Durée max: 1h30<br>Tarif: 2,50€/h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE ROUGE-7 – Zones très limitées\n" +
            "            L.polygon([\n" +
            "                [43.61636656186974, 1.4369581500679618],\n" +
            "                [43.616442709531334, 1.4372386301741393],\n" +
            "                [43.63324812196252, 1.431380573401711],\n" +
            "                [43.63326915415644, 1.4314677508901181]\n" +
            "            ], {\n" +
            "                color: '#e74c3c',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#e74c3c',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-rouge'\n" +
            "            }).addTo(zonesRouges).bindTooltip('<b>Zone Rouge – Stationnement très limité</b><br>Durée max: 1h30<br>Tarif: 2,50€/h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE ROUGE-8 – Zones très limitées\n" +
            "            L.polygon([\n" +
            "                [43.63346873280658, 1.4345641634972328],\n" +
            "                [43.633378236413435, 1.4346096307054739],\n" +
            "                [43.633378236413435, 1.4353257392352703],\n" +
            "                [43.63312320039043, 1.4358827125362232],\n" +
            "                [43.63345227892706, 1.436382851826875],\n" +
            "                [43.633773128765256, 1.4359622801506453],\n" +
            "                [43.633731994266356, 1.4359281797444645],\n" +
            "                [43.63346873280658, 1.4362805506083325],\n" +
            "                [43.633172562285814, 1.4358599789321027],\n" +
            "                [43.633476959744655, 1.4353825732455718],\n" +
            "                [43.63349341361743, 1.4345982639034136]\n" +
            "            ], {\n" +
            "                color: '#e74c3c',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#e74c3c',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-rouge'\n" +
            "            }).addTo(zonesRouges).bindTooltip('<b>Zone Rouge – Stationnement très limité</b><br>Durée max: 1h30<br>Tarif: 2,50€/h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE ROUGE-9 – Zones très limitées\n" +
            "            L.polygon([\n" +
            "                [43.64042462504767, 1.4516536640187885],\n" +
            "                [43.641888748910546, 1.453749095143873],\n" +
            "                [43.64108697121472, 1.454760682583569],\n" +
            "                [43.639779702026786, 1.4526652514584846],\n" +
            "                [43.63984942376817, 1.4525929952127916],\n" +
            "                [43.64124384160585, 1.4545679992617222],\n" +
            "                [43.64169943626687, 1.4540066970363368],\n" +
            "                [43.64031044441279, 1.4520708007827388]\n" +
            "            ], {\n" +
            "                color: '#e74c3c',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#e74c3c',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-rouge'\n" +
            "            }).addTo(zonesRouges).bindTooltip('<b>Zone Rouge – Stationnement très limité</b><br>Durée max: 1h30<br>Tarif: 2,50€/h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE ROUGE-10 – Zones très limitées\n" +
            "            L.polygon([\n" +
            "                [43.61433380206062, 1.4509415683066051],\n" +
            "                [43.619635350024154, 1.4568115194490459],\n" +
            "                [43.623316705541804, 1.4587294243123239],\n" +
            "                [43.62348499069024, 1.4589909567775445],\n" +
            "                [43.61940394304122, 1.4571602295209973],\n" +
            "                [43.61414445243042, 1.4510287460875348]\n" +
            "            ], {\n" +
            "                color: '#e74c3c',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#e74c3c',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-rouge'\n" +
            "            }).addTo(zonesRouges).bindTooltip('<b>Zone Rouge – Stationnement très limité</b><br>Durée max: 1h30<br>Tarif: 2,50€/h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE ROUGE-11 – Zones très limitées\n" +
            "            L.polygon([\n" +
            "                [43.610144670117855, 1.4564053617841222],\n" +
            "                [43.610827538579045, 1.4555449634491897],\n" +
            "                [43.61147445944312, 1.4565377307587273],\n" +
            "                [43.61088743895139, 1.4574312213373108],\n" +
            "                [43.610755658053506, 1.457331944606357],\n" +
            "                [43.61139059972364, 1.4565708230023784],\n" +
            "                [43.61085149873514, 1.4556938785456204],\n" +
            "                [43.61022853157458, 1.4565377307587273]\n" +
            "            ], {\n" +
            "                color: '#e74c3c',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#e74c3c',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-rouge'\n" +
            "            }).addTo(zonesRouges).bindTooltip('<b>Zone Rouge – Stationnement très limité</b><br>Durée max: 1h30<br>Tarif: 2,50€/h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE ROUGE-12 – Zones très limitées\n" +
            "            L.polygon([\n" +
            "                [43.60540313695982, 1.4578238584053167],\n" +
            "                [43.60530383214687, 1.457974713486406],\n" +
            "                [43.60574077209765, 1.4604158229803974],\n" +
            "                [43.60581028497903, 1.4603335383907126]\n" +
            "            ], {\n" +
            "                color: '#e74c3c',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#e74c3c',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-rouge'\n" +
            "            }).addTo(zonesRouges).bindTooltip('<b>Zone Rouge – Stationnement très limité</b><br>Durée max: 1h30<br>Tarif: 2,50€/h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE ROUGE-13 – Zones très limitées\n" +
            "            L.polygon([\n" +
            "                [43.600993845230846, 1.4626237930952317],\n" +
            "                [43.60081508334799, 1.4675197261814954],\n" +
            "                [43.60114281306084, 1.4682877156852232],\n" +
            "                [43.60106336359742, 1.46839742847147],\n" +
            "                [43.600805152116706, 1.468095718309291],\n" +
            "                [43.60082501457764, 1.4691791320734784],\n" +
            "                [43.60072570220739, 1.4690557051889506],\n" +
            "                [43.600527076975105, 1.4638169196456654],\n" +
            "                [43.600805152116706, 1.462500366210704]\n" +
            "            ], {\n" +
            "                color: '#e74c3c',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#e74c3c',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-rouge'\n" +
            "            }).addTo(zonesRouges).bindTooltip('<b>Zone Rouge – Stationnement très limité</b><br>Durée max: 1h30<br>Tarif: 2,50€/h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE ROUGE-14 – Zones très limitées\n" +
            "            L.polygon([\n" +
            "                [43.60179826713082, 1.4582764239402015],\n" +
            "                [43.601639369830195, 1.458317566235044],\n" +
            "                [43.60100377643099, 1.4567953013258697],\n" +
            "                [43.601093157158495, 1.456671874441342]\n" +
            "            ], {\n" +
            "                color: '#e74c3c',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#e74c3c',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-rouge'\n" +
            "            }).addTo(zonesRouges).bindTooltip('<b>Zone Rouge – Stationnement très limité</b><br>Durée max: 1h30<br>Tarif: 2,50€/h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE ROUGE-15 – Zones très limitées\n" +
            "            L.polygon([\n" +
            "                [43.59405153515265, 1.4656683230423269],\n" +
            "                [43.59401180574599, 1.4655860384666024],\n" +
            "                [43.59563497252535, 1.4636027122435256],\n" +
            "                [43.59744439440041, 1.459035982344546],\n" +
            "                [43.59751629083291, 1.459118712953674],\n" +
            "                [43.595766786588364, 1.463652350333895]\n" +
            "            ], {\n" +
            "                color: '#e74c3c',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#e74c3c',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-rouge'\n" +
            "            }).addTo(zonesRouges).bindTooltip('<b>Zone Rouge – Stationnement très limité</b><br>Durée max: 1h30<br>Tarif: 2,50€/h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE ROUGE-16 – Zones très limitées\n" +
            "            L.polygon([\n" +
            "                [43.587400911371084, 1.4609888244427423],\n" +
            "                [43.58770729991478, 1.4610734245695551],\n" +
            "                [43.58381604954267, 1.4703794385189835],\n" +
            "                [43.58366284556259, 1.4701679382019512]\n" +
            "            ], {\n" +
            "                color: '#e74c3c',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#e74c3c',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-rouge'\n" +
            "            }).addTo(zonesRouges).bindTooltip('<b>Zone Rouge – Stationnement très limité</b><br>Durée max: 1h30<br>Tarif: 2,50€/h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE ROUGE-17 – Zones très limitées\n" +
            "            L.polygon([\n" +
            "                [43.592718391579886, 1.4446131734137888],\n" +
            "                [43.59276917665634, 1.4444028133428464],\n" +
            "                [43.5734169599234, 1.4519407162894253],\n" +
            "                [43.5734169599234, 1.4523263764194863]\n" +
            "            ], {\n" +
            "                color: '#e74c3c',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#e74c3c',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-rouge'\n" +
            "            }).addTo(zonesRouges).bindTooltip('<b>Zone Rouge – Stationnement très limité</b><br>Durée max: 1h30<br>Tarif: 2,50€/h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-1 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.57983940161112, 1.481043584947603],\n" +
            "                [43.579748824499426, 1.4808958165208197],\n" +
            "                [43.578797646612784, 1.4838581639618964],\n" +
            "                [43.57856811098393, 1.484290423809624],\n" +
            "                [43.57868971937228, 1.484374357756814],\n" +
            "                [43.57900894022586, 1.4840218351698344],\n" +
            "                [43.57892685502586, 1.4838854425056507]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-2 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.57658973054431, 1.474073554084669],\n" +
            "                [43.576480526100525, 1.474158345492597],\n" +
            "                [43.575224660762125, 1.4724907811366819],\n" +
            "                [43.57525878788381, 1.4724342535313968]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-3 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.57163865004668, 1.4776378425914776],\n" +
            "                [43.57309923099288, 1.4799966675392167],\n" +
            "                [43.572681925767185, 1.4807920852395062],\n" +
            "                [43.5726123746151, 1.4807235147481017],\n" +
            "                [43.572980001223456, 1.4800378098340594],\n" +
            "                [43.5726123746151, 1.4794343895097017],\n" +
            "                [43.572075834456356, 1.4802983777013956],\n" +
            "                [43.57127163186808, 1.4800656276887127],\n" +
            "                [43.570602692467766, 1.4809041205079039],\n" +
            "                [43.570589040565984, 1.4808570141701658],\n" +
            "                [43.57126480599034, 1.4799525724856022],\n" +
            "                [43.57210438277353, 1.480131576569005],\n" +
            "                [43.572568534023794, 1.47934019010034],\n" +
            "                [43.571651722434375, 1.477993285128392]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-4 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.57370146703368, 1.4836467702615999],\n" +
            "                [43.57365205631699, 1.4836013030533592],\n" +
            "                [43.572976772458816, 1.484726616457325],\n" +
            "                [43.572976772458816, 1.4847834504676265]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-5 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.57390734458363, 1.4838854731048656],\n" +
            "                [43.573816758548354, 1.4839536739172272],\n" +
            "                [43.57399793048263, 1.4850676205191327],\n" +
            "                [43.574063811050856, 1.4850562537170726]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-6 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.56703102401647, 1.4997358331292454],\n" +
            "                [43.567536317244716, 1.5009750131443946],\n" +
            "                [43.5679716433962, 1.5005887752175946],\n" +
            "                [43.567563525221345, 1.4995319853345455],\n" +
            "                [43.56730310551221, 1.4996231804005955],\n" +
            "                [43.56762571483609, 1.5004439359950448],\n" +
            "                [43.567497448685316, 1.5007121567775445],\n" +
            "                [43.56714763052227, 1.499676824557095]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-7 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.59087265939448, 1.3823123783083606],\n" +
            "                [43.59022200920261, 1.3769423395296134],\n" +
            "                [43.58984607477457, 1.3770820803156776],\n" +
            "                [43.590701101796434, 1.3824083409204786]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-8 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.608429598245145, 1.3783787060138237],\n" +
            "                [43.609104832595406, 1.3801615386430197],\n" +
            "                [43.60843952821697, 1.3828357875868134],\n" +
            "                [43.608151558368235, 1.3828495016839608],\n" +
            "                [43.60889630514918, 1.3803123937116437],\n" +
            "                [43.60824092846884, 1.3786392738596291]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-9 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.61211088741479, 1.4136273791054088],\n" +
            "                [43.61195775547333, 1.4135850790454974],\n" +
            "                [43.616398423473974, 1.4089320724552592],\n" +
            "                [43.61664341630149, 1.4089320724552592]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-10 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.61296862418109, 1.4770454175536145],\n" +
            "                [43.61250502365256, 1.477623263486483],\n" +
            "                [43.61244283306589, 1.4772406357741779],\n" +
            "                [43.61295166324913, 1.476982947723034]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-11 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.63366320592897, 1.4634226774733203],\n" +
            "                [43.633705270015405, 1.4632192633505121],\n" +
            "                [43.63566121751877, 1.463829505718937],\n" +
            "                [43.63566121751877, 1.463974801520943]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-12 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.639545083176124, 1.4421090315369183],\n" +
            "                [43.639413365097425, 1.441976662573251],\n" +
            "                [43.63913795363612, 1.4431679832462574],\n" +
            "                [43.63858712692624, 1.4432010754871745],\n" +
            "                [43.63840750842907, 1.4441938427146799],\n" +
            "                [43.638012345845205, 1.4441442043533046],\n" +
            "                [43.63797642184505, 1.445252794424019],\n" +
            "                [43.638012345845205, 1.4454348017490617],\n" +
            "                [43.63810814307385, 1.4443427577988055],\n" +
            "                [43.63849133046123, 1.4442600271965136],\n" +
            "                [43.63858712692624, 1.4432507138485495],\n" +
            "                [43.63929362113893, 1.443234167728091]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-13 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.64598790998157, 1.4333555793774664],\n" +
            "                [43.64639478317408, 1.4330812974345133],\n" +
            "                [43.64846880125165, 1.4327521591029695],\n" +
            "                [43.64850849467914, 1.4331635820173991]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-14 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.66629123869045, 1.4299466649186467],\n" +
            "                [43.665744699870416, 1.4299466649186467],\n" +
            "                [43.66368462420744, 1.4304697298058684],\n" +
            "                [43.6642311817835, 1.4307603214098803]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-15 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.58215138270586, 1.389068813765179],\n" +
            "                [43.581738338572066, 1.3887232401764051],\n" +
            "                [43.58162568968012, 1.3869608148736592],\n" +
            "                [43.58138787466053, 1.3870644869502913],\n" +
            "                [43.58120012529729, 1.3882567158315606],\n" +
            "                [43.580686940717165, 1.388412223946509],\n" +
            "                [43.580636873694665, 1.3885158960231412],\n" +
            "                [43.58120012529729, 1.3882739945109994],\n" +
            "                [43.581538073729575, 1.3887750762147213],\n" +
            "                [43.58123767521679, 1.3894662233922688],\n" +
            "                [43.58143794105852, 1.3895698954689009],\n" +
            "                [43.58170078896485, 1.3888787482913534],\n" +
            "                [43.58211383335626, 1.389172485841811]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-16 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.58080770830261, 1.4130535567854035],\n" +
            "                [43.58122107010648, 1.4134962839610465],\n" +
            "                [43.58112129337919, 1.4139586879000514],\n" +
            "                [43.58149901868918, 1.4140964252435848],\n" +
            "                [43.58184110749096, 1.4139586879000514],\n" +
            "                [43.581962263475475, 1.4137028899763464],\n" +
            "                [43.581035770338474, 1.4129748497319559]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-17 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.563299058154485, 1.4133578238252134],\n" +
            "                [43.56325605213788, 1.4132035131471916],\n" +
            "                [43.564545479220655, 1.4095817007446796],\n" +
            "                [43.564569114027606, 1.4096632452508302]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-18 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.56473455741921, 1.410478690302186],\n" +
            "                [43.56468137923659, 1.410437918049111],\n" +
            "                [43.56474046610327, 1.410462381400956],\n" +
            "                [43.564905909021654, 1.4099731143640548],\n" +
            "                [43.56482318761926, 1.4099160332097498]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-19 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.56513132972438, 1.4101556248822877],\n" +
            "                [43.56518409803269, 1.4102284498228608],\n" +
            "                [43.56507044315738, 1.4105813706887147],\n" +
            "                [43.56543576163631, 1.4110239222506582],\n" +
            "                [43.56528151632655, 1.4115224929976578],\n" +
            "                [43.565265279955184, 1.4113712411980062],\n" +
            "                [43.56535863903081, 1.4110631356801973],\n" +
            "                [43.5650054974181, 1.4106990109773325]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-20 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.56596763841681, 1.4000857084326388],\n" +
            "                [43.565825832728265, 1.4002895696980142],\n" +
            "                [43.56564266655313, 1.3996861403525027],\n" +
            "                [43.5653176929363, 1.3998166115623432],\n" +
            "                [43.5652881497933, 1.3991397921612965],\n" +
            "                [43.56561903216738, 1.3989603942477662],\n" +
            "                [43.56472091863788, 1.3988543863897707],\n" +
            "                [43.56472682732329, 1.3978106167110484],\n" +
            "                [43.56524678936875, 1.3979737057233488],\n" +
            "                [43.565051804127634, 1.3987973052354656],\n" +
            "                [43.56583174130528, 1.3987402240811606]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n" +
            "            // ZONE BLEUE-21 – Zones résidentielles extérieures\n" +
            "            L.polygon([\n" +
            "                [43.54956219668009, 1.3861654220279778],\n" +
            "                [43.54958249743343, 1.38613741243545],\n" +
            "                [43.550228057823446, 1.3866079735899217],\n" +
            "                [43.550228057823446, 1.3866079735899217],\n" +
            "                [43.550386401768776, 1.3863166738276296],\n" +
            "                [43.55042700271339, 1.3863782949311914],\n" +
            "                [43.55030519979748, 1.3866359831824495],\n" +
            "                [43.55030519979748, 1.3866359831824495],\n" +
            "                [43.550459483449394, 1.3869272829447414],\n" +
            "                [43.55047166372087, 1.3869104771892247],\n" +
            "                [43.55047166372087, 1.3869889040483032]\n" +
            "            ], {\n" +
            "                color: '#3498db',\n" +
            "                weight: 2,\n" +
            "                fillColor: '#3498db',\n" +
            "                fillOpacity: 0.35,\n" +
            "                className: 'zone-bleue'\n" +
            "            }).addTo(zonesBleues).bindTooltip('<b>Zone Bleue – Quartiers extérieurs</b><br>Payant: Lundi-samedi 9h-19h<br>Durée max: 10h', {sticky: true});\n" +
            "            \n";
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
                    
                    Page_Garer_Parking page = new Page_Garer_Parking(emailUtilisateur, parking);
                    page.setVisible(true);
                    
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
        SwingUtilities.invokeLater(() -> {
            removeAll();
            JLabel lblErreur = new JLabel("<html><center>" + message + "</center></html>", 
                                          SwingConstants.CENTER);
            lblErreur.setFont(new Font("Arial", Font.PLAIN, 12));
            lblErreur.setForeground(Color.RED);
            add(lblErreur, BorderLayout.CENTER);
            revalidate();
            repaint();
        });
    }
    
    public void recharger() {
        SwingUtilities.invokeLater(() -> {
            // Réinitialiser le flag
            initialisationEnCours = false;
            
            // Vérifier que JavaFX tourne
            if (!JavaFXInitializer.isJavaFXRunning()) {
                JavaFXInitializer.initializeJavaFX();
            }
            
            if (webEngine != null) {
                Platform.runLater(() -> {
                    try {
                        String html = genererHTMLAvecZones();
                        webEngine.loadContent(html);
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Si erreur, réinitialiser complètement
                        SwingUtilities.invokeLater(() -> {
                            webEngine = null;
                            fxPanel = null;
                            initialiserCarte();
                        });
                    }
                });
            } else {
                initialiserCarte();
            }
        });
    }
    
    public void nettoyer() {
        // NE PAS arrêter JavaFX, juste nettoyer le webEngine
        if (webEngine != null) {
            Platform.runLater(() -> {
                try {
                    webEngine.loadContent("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
    
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
    
    @Override
    public void doLayout() {
        super.doLayout();
        if (fxPanel != null) {
            fxPanel.setSize(getSize());
        }
    }
}