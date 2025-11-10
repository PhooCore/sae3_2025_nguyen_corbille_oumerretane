package modèle;

import java.time.LocalDateTime;

/**
 * Classe représentant un usager dans le système.
 * Contient les informations personnelles et d'authentification d'un utilisateur.
 */
public class Usager {
    private int idUsager;
    private String nomUsager;
    private String prenomUsager;
    private String mailUsager;
    private String motDePasse;
    private LocalDateTime dateInscription;

    /**
     * Constructeur avec paramètres pour créer un nouvel usager.
     * 
     * @param nomUsager le nom de famille de l'usager
     * @param prenomUsager le prénom de l'usager
     * @param mailUsager l'adresse email de l'usager
     * @param motDePasse le mot de passe de l'usager
     */
    public Usager(String nomUsager, String prenomUsager, String mailUsager, String motDePasse) {
        this.nomUsager = nomUsager;
        this.prenomUsager = prenomUsager;
        this.mailUsager = mailUsager;
        this.motDePasse = motDePasse;
    }

    /**
     * Constructeur vide essentiel pour le DAO.
     * Permet de créer des objets Usager à partir de la base de données
     * sans avoir à fournir tous les paramètres immédiatement.
     */
    public Usager() {}


    /**
     * @return l'identifiant unique de l'usager
     */
    public int getIdUsager() { 
        return idUsager; 
    }
    
    /**
     * @return le nom de famille de l'usager
     */
    public String getNomUsager() { 
        return nomUsager; 
    }
    
    /**
     * @return le prénom de l'usager
     */
    public String getPrenomUsager() { 
        return prenomUsager; 
    }
    
    /**
     * @return l'adresse email de l'usager
     */
    public String getMailUsager() { 
        return mailUsager; 
    }
    
    /**
     * @return le mot de passe de l'usager
     */
    public String getMotDePasse() { 
        return motDePasse; 
    }



    /**
     * @param idUsager l'identifiant unique à assigner à l'usager
     */
    public void setIdUsager(int idUsager) { 
        this.idUsager = idUsager; 
    }
    
    /**
     * @param nomUsager le nom de famille à assigner à l'usager
     */
    public void setNomUsager(String nomUsager) { 
        this.nomUsager = nomUsager; 
    }
    
    /**
     * @param prenomUsager le prénom à assigner à l'usager
     */
    public void setPrenomUsager(String prenomUsager) { 
        this.prenomUsager = prenomUsager; 
    }
    
    /**
     * @param mailUsager l'adresse email à assigner à l'usager
     */
    public void setMailUsager(String mailUsager) { 
        this.mailUsager = mailUsager; 
    }
    
    /**
     * @param motDePasse le mot de passe à assigner à l'usager
     */
    public void setMotDePasse(String motDePasse) { 
        this.motDePasse = motDePasse; 
    }
}