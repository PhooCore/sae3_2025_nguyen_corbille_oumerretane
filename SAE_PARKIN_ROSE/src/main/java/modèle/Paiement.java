package modèle;

public class Paiement {
    private String idPaiement;
    private String nomCarte;
    private String numeroCarte;
    private String codeSecretCarte;
    private String idAbonnement;
    private double montant;
    private int idUsager; // Ajout de l'ID utilisateur

    public Paiement(String nomCarte, String numeroCarte, String codeSecretCarte, double montant, int idUsager) {
        this.nomCarte = nomCarte;
        this.numeroCarte = numeroCarte;
        this.codeSecretCarte = codeSecretCarte;
        this.montant = montant;
        this.idUsager = idUsager;
        this.idAbonnement = "ABO_SIMPLE"; // Paiement ponctuel
        this.idPaiement = "PAI_" + System.currentTimeMillis();
    }


    public String getIdPaiement() { return idPaiement; }
    public void setIdPaiement(String idPaiement) { this.idPaiement = idPaiement; }
    
    public String getNomCarte() { return nomCarte; }
    public void setNomCarte(String nomCarte) { this.nomCarte = nomCarte; }
    
    public String getNumeroCarte() { return numeroCarte; }
    public void setNumeroCarte(String numeroCarte) { this.numeroCarte = numeroCarte; }
    
    public String getCodeSecretCarte() { return codeSecretCarte; }
    public void setCodeSecretCarte(String codeSecretCarte) { this.codeSecretCarte = codeSecretCarte; }
    
    public String getIdAbonnement() { return idAbonnement; }
    public void setIdAbonnement(String idAbonnement) { this.idAbonnement = idAbonnement; }
    
    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }
    public int getIdUsager() { return idUsager; }
    public void setIdUsager(int idUsager) { this.idUsager = idUsager; }
}