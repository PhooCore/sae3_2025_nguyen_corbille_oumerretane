package ihm;

import javax.swing.*;
import java.awt.*;

public class ExplicationZoneToulouse extends JFrame {

    public ExplicationZoneToulouse() {
        setTitle("Stationnement en voirie à Toulouse");
        setSize(800, 600);
        setLocationRelativeTo(null); // centre la fenêtre
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Contenu HTML pour l'explication
        String html =
        "<html>" +
        "<body style='font-family:Arial; margin:10px;'>" +
        "<h1>Stationnement en voirie à Toulouse</h1>" +
        "<p>Le stationnement en voirie à Toulouse est <b>très réglementé</b>, que ce soit pour de la courte durée, de la moyenne durée, dans le centre-ville ou hors centre-ville.</p>" +
        "<p>Le stationnement est <b>payant dans toute la ville</b> et réparti en <b>5 zones distinctes</b>.</p>" +

        "<h2>Zones de stationnement</h2>" +

        "<h3 style='color:#f1c40f;'>Zone Jaune – Centre-ville</h3>" +
        "<ul><li>Payant du lundi au samedi</li><li>De 9h à 20h</li><li>Durée maximale : <b>2h30</b></li></ul>" +

        "<h3 style='color:#e67e22;'>Zone Orange – Faubourgs</h3>" +
        "<ul><li>Payant du lundi au samedi</li><li>De 9h à 19h</li><li>Durée maximale : <b>5h</b></li></ul>" +

        "<h3 style='color:#e74c3c;'>Zone Rouge – Faubourg commerçant</h3>" +
        "<ul><li>Payant du lundi au samedi</li><li>De 9h à 19h (places JOB : 8h à 19h)</li><li>Durée maximale : <b>3h</b></li></ul>" +

        "<h3 style='color:#27ae60;'>Zone Verte – Moyenne durée</h3>" +
        "<ul><li>Payant du lundi au samedi</li><li>De 9h à 19h</li><li>Durée maximale : <b>5h</b></li></ul>" +

        "<h3 style='color:#2980b9;'>Zone Bleue – Gratuite</h3>" +
        "<ul><li>Gratuite avec disque bleu</li><li>De 9h à 12h et de 14h à 19h</li><li>Durée maximale : <b>1h30</b></li></ul>" +

        "<h2>Tarifs de stationnement</h2>" +

        "<h3>Zone Jaune</h3>" +
        "<table border='1' cellpadding='5'><tr><th>Durée</th><th>Prix</th></tr>" +
        "<tr><td>1h</td><td>1,50 €</td></tr><tr><td>2h</td><td>3 €</td></tr><tr><td>2h à 2h30</td><td>Majoration 30 €</td></tr></table>" +

        "<h3>Zone Orange</h3>" +
        "<table border='1' cellpadding='5'><tr><th>Durée</th><th>Prix</th></tr>" +
        "<tr><td>1h</td><td>1 €</td></tr><tr><td>2h</td><td>2 €</td></tr><tr><td>3h</td><td>4 €</td></tr><tr><td>4h</td><td>6 €</td></tr><tr><td>4h à 5h</td><td>Majoration 30 €</td></tr></table>" +

        "<h3>Zone Rouge</h3>" +
        "<table border='1' cellpadding='5'><tr><th>Durée</th><th>Prix</th></tr>" +
        "<tr><td>30 min</td><td>Gratuit (1 fois / demi-journée)</td></tr><tr><td>1h</td><td>1 €</td></tr><tr><td>2h</td><td>2 €</td></tr><tr><td>2h à 3h</td><td>Majoration 30 €</td></tr></table>" +

        "<h3>Zone Verte</h3>" +
        "<table border='1' cellpadding='5'><tr><th>Durée</th><th>Prix</th></tr>" +
        "<tr><td>1h</td><td>0,50 €</td></tr><tr><td>2h</td><td>1 €</td></tr><tr><td>3h</td><td>1,50 €</td></tr><tr><td>4h</td><td>2 €</td></tr><tr><td>4h à 5h</td><td>Majoration 30 €</td></tr></table>" +

        "<h3>Zone Bleue</h3>" +
        "<table border='1' cellpadding='5'><tr><th>Durée</th><th>Prix</th></tr>" +
        "<tr><td>1h30</td><td>Gratuit</td></tr></table>" +

        "</body></html>";

        JEditorPane editorPane = new JEditorPane("text/html", html);
        editorPane.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(editorPane);
        add(scrollPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ExplicationZoneToulouse().setVisible(true));
    }
}
