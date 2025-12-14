package modele;

public class Usager {
    private int idUsager;
    private String nomUsager;
    private String prenomUsager;
    private String mailUsager;
    private String motDePasse;
    private boolean isAdmin;

    public Usager(String nomUsager, String prenomUsager, String mailUsager, String motDePasse) {
        this.nomUsager = nomUsager;
        this.prenomUsager = prenomUsager;
        this.mailUsager = mailUsager;
        this.motDePasse = motDePasse;
    }

    public Usager() {}

    public int getIdUsager() { 
        return idUsager; 
    }
    
    public String getNomUsager() { 
        return nomUsager; 
    }
    
    public String getPrenomUsager() { 
        return prenomUsager; 
    }
    
    public String getMailUsager() { 
        return mailUsager; 
    }
    
    public String getMotDePasse() { 
        return motDePasse; 
    }

    public void setIdUsager(int idUsager) { 
        this.idUsager = idUsager; 
    }
    
    public void setNomUsager(String nomUsager) { 
        this.nomUsager = nomUsager; 
    }
    
    public void setPrenomUsager(String prenomUsager) { 
        this.prenomUsager = prenomUsager; 
    }
    
    public void setMailUsager(String mailUsager) { 
        this.mailUsager = mailUsager; 
    }
    
    public void setMotDePasse(String motDePasse) { 
        this.motDePasse = motDePasse; 
    }
    
    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}