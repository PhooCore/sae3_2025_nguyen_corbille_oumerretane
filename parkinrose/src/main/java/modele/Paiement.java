package modele;

import java.time.LocalDateTime;

public class Paiement {
    private String idPaiement;
    private String nomCarte;
    private String numeroCarte;
    private String codeSecretCarte;
    private String idAbonnement;
    private double montant;
    private int idUsager;
    private LocalDateTime datePaiement;
    private String methodePaiement;
    private String statut;
    private String typePaiement;

    // Constructeur pour les stationnements
    public Paiement(String nomCarte, String numeroCarte, String codeSecretCarte, double montant, int idUsager) {
        this.nomCarte = nomCarte;
        this.numeroCarte = numeroCarte;
        this.codeSecretCarte = codeSecretCarte;
        this.montant = montant;
        this.idUsager = idUsager;
        this.idPaiement = "PAY_" + System.currentTimeMillis();
        this.datePaiement = LocalDateTime.now();
        this.methodePaiement = "CARTE";
        this.statut = "REUSSI";
        this.typePaiement = "Stationnement";
    }

    // Constructeur pour les abonnements
    public Paiement(String nomCarte, String numeroCarte, String codeSecretCarte, double montant, int idUsager, String idAbonnement) {
        this(nomCarte, numeroCarte, codeSecretCarte, montant, idUsager);
        this.idAbonnement = idAbonnement;
        this.typePaiement = "Abonnement";
    }

    public Paiement() {
    }

    // Getters et Setters
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
    
    public LocalDateTime getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDateTime datePaiement) { this.datePaiement = datePaiement; }
    
    public String getMethodePaiement() { return methodePaiement; }
    public void setMethodePaiement(String methodePaiement) { this.methodePaiement = methodePaiement; }
    
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getTypePaiement() { 
        if (typePaiement == null) {
            // Détermine automatiquement le type en fonction de l'ID abonnement
            return idAbonnement != null && !idAbonnement.isEmpty() ? "Abonnement" : "Stationnement";
        }
        return typePaiement; 
    }
    
    public void setTypePaiement(String typePaiement) { 
        this.typePaiement = typePaiement; 
    }
}