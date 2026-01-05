package modele.dao;

import modele.Paiement;
import modele.dao.requetes.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaiementDAO extends DaoModele<Paiement> {
    
    private static PaiementDAO instance;
    
    // Constructeur privé pour le singleton
    private PaiementDAO() {}
    
    // Méthode pour obtenir l'instance unique (Singleton)
    public static PaiementDAO getInstance() {
        if (instance == null) {
            instance = new PaiementDAO();
        }
        return instance;
    }
    
    @Override
    protected Paiement creerInstance(ResultSet curseur) throws SQLException {
        Paiement paiement = new Paiement();
        paiement.setIdPaiement(curseur.getString("id_paiement"));
        paiement.setNomCarte(curseur.getString("nom_carte"));
        paiement.setNumeroCarte(curseur.getString("numero_carte"));
        paiement.setCodeSecretCarte(curseur.getString("code_secret_carte"));
        paiement.setMontant(curseur.getDouble("montant"));
        paiement.setIdUsager(curseur.getInt("id_usager"));
        paiement.setDatePaiement(curseur.getTimestamp("date_paiement").toLocalDateTime());
        paiement.setMethodePaiement(curseur.getString("methode_paiement"));
        paiement.setStatut(curseur.getString("statut"));
        paiement.setIdAbonnement(curseur.getString("id_abonnement"));
        
        // Déterminer le type avec la bonne casse
        if (paiement.getIdAbonnement() != null && !paiement.getIdAbonnement().isEmpty()) {
            paiement.setTypePaiement("Abonnement");
        } else {
            paiement.setTypePaiement("Stationnement");
        }
        
        return paiement;
    }
    @Override
    public List<Paiement> findAll() throws SQLException {
        RequeteSelectPaiement req = new RequeteSelectPaiement();
        return find(req);
    }
    
    @Override
    public Paiement findById(String... id) throws SQLException {
        if (id.length == 0) {
            return null;
        }
        RequeteSelectPaiementById req = new RequeteSelectPaiementById();
        return findById(req, id[0]);
    }
    
    @Override
    public void create(Paiement paiement) throws SQLException {
        RequeteInsertPaiement req = new RequeteInsertPaiement();
        miseAJour(req, paiement);
    }
    
    @Override
    public void update(Paiement paiement) throws SQLException {
        RequeteUpdatePaiement req = new RequeteUpdatePaiement();
        miseAJour(req, paiement);
    }
    
    @Override
    public void delete(Paiement paiement) throws SQLException {
        RequeteDeletePaiement req = new RequeteDeletePaiement();
        miseAJour(req, paiement);
    }
    
    // Méthodes spécifiques pour les paiements
    
    /**
     * Récupère tous les paiements d'un utilisateur
     * @param idUsager ID de l'utilisateur
     * @return Liste des paiements de l'utilisateur
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Paiement> getPaiementsByUsager(int idUsager) throws SQLException {
        RequeteSelectPaiementsByUsager req = new RequeteSelectPaiementsByUsager();
        return find(req, String.valueOf(idUsager));
    }
    
    /**
     * Récupère les paiements d'abonnement d'un utilisateur
     * @param idUsager ID de l'utilisateur
     * @return Liste des paiements d'abonnement
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Paiement> getPaiementsAbonnementByUsager(int idUsager) throws SQLException {
        String sql = "SELECT * FROM Paiement WHERE id_usager = ? AND id_abonnement IS NOT NULL ORDER BY date_paiement DESC";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsager);
            return select(pstmt);
        }
    }
    
    /**
     * Récupère les paiements de stationnement d'un utilisateur
     * @param idUsager ID de l'utilisateur
     * @return Liste des paiements de stationnement
     * @throws SQLException en cas d'erreur SQL
     */
    public List<Paiement> getPaiementsStationnementByUsager(int idUsager) throws SQLException {
        String sql = "SELECT * FROM Paiement WHERE id_usager = ? AND id_abonnement IS NULL ORDER BY date_paiement DESC";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsager);
            return select(pstmt);
        }
    }
    
    /**
     * Enregistre un paiement (alias pour create)
     * @param paiement L'objet Paiement à enregistrer
     * @return true si l'insertion a réussi
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean enregistrerPaiement(Paiement paiement) throws SQLException {
        create(paiement);
        return true;
    }
    
    /**
     * Vérifie si un paiement existe par son ID
     * @param idPaiement ID du paiement
     * @return true si le paiement existe
     * @throws SQLException en cas d'erreur SQL
     */
    public boolean paiementExiste(String idPaiement) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Paiement WHERE id_paiement = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, idPaiement);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Récupère le total des dépenses d'un utilisateur
     * @param idUsager ID de l'utilisateur
     * @return Montant total dépensé
     * @throws SQLException en cas d'erreur SQL
     */
    public double getTotalDepenses(int idUsager) throws SQLException {
        String sql = "SELECT SUM(montant) as total FROM Paiement WHERE id_usager = ? AND statut = 'REUSSI'";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsager);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }
    
    /**
     * Récupère le dernier paiement d'un utilisateur
     * @param idUsager ID de l'utilisateur
     * @return Dernier paiement ou null
     * @throws SQLException en cas d'erreur SQL
     */
    public Paiement getDernierPaiement(int idUsager) throws SQLException {
        String sql = "SELECT * FROM Paiement WHERE id_usager = ? " +
                    "ORDER BY date_paiement DESC, id_paiement DESC LIMIT 1";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsager);
            List<Paiement> resultats = select(pstmt);
            return resultats.isEmpty() ? null : resultats.get(0);
        }
    }
}