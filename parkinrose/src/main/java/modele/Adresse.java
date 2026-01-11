package modele;

public class Adresse {
    private int idAdresse;
    private int idUsager;
    private String numero;
    private String rue;
    private String complement;
    private String codePostal;
    private String ville;
    private String pays;
    private boolean estPrincipale;

    public Adresse() {}

    public Adresse(int idUsager, String numero, String rue, String codePostal, String ville) {
        this.idUsager = idUsager;
        this.numero = numero;
        this.rue = rue;
        this.codePostal = codePostal;
        this.ville = ville;
        this.pays = "France";
        this.estPrincipale = false;
    }

    // Getters et Setters
    public int getIdAdresse() { return idAdresse; }
    public void setIdAdresse(int idAdresse) { this.idAdresse = idAdresse; }
    
    public int getIdUsager() { return idUsager; }
    public void setIdUsager(int idUsager) { this.idUsager = idUsager; }
    
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    
    public String getRue() { return rue; }
    public void setRue(String rue) { this.rue = rue; }
    
    public String getComplement() { return complement; }
    public void setComplement(String complement) { this.complement = complement; }
    
    public String getCodePostal() { return codePostal; }
    public void setCodePostal(String codePostal) { this.codePostal = codePostal; }
    
    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
    
    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }
    
    public boolean isEstPrincipale() { return estPrincipale; }
    public void setEstPrincipale(boolean estPrincipale) { this.estPrincipale = estPrincipale; }
    
    // Méthodes utilitaires
    public String getAdresseComplete() {
        StringBuilder sb = new StringBuilder();
        sb.append(numero).append(" ").append(rue);
        
        if (complement != null && !complement.trim().isEmpty()) {
            sb.append(", ").append(complement);
        }
        
        sb.append("\n").append(codePostal).append(" ").append(ville);
        
        if (pays != null && !"France".equals(pays)) {
            sb.append("\n").append(pays);
        }
        
        return sb.toString();
    }
    
    public String getAdresseLigne() {
        StringBuilder sb = new StringBuilder();
        sb.append(numero).append(" ").append(rue);
        
        if (complement != null && !complement.trim().isEmpty()) {
            sb.append(", ").append(complement);
        }
        
        sb.append(", ").append(codePostal).append(" ").append(ville);
        return sb.toString();
    }
}