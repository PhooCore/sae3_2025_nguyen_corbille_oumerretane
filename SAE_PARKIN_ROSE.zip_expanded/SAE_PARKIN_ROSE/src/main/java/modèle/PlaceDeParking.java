package modèle;

public class PlaceDeParking {
    private int idPlace;
    private String numero;
    private int idParking;
    private boolean estDisponible;
    
    public PlaceDeParking(int idPlace, String numero, int idParking, boolean estDisponible) {
        this.idPlace = idPlace;
        this.numero = numero;
        this.idParking = idParking;
        this.estDisponible = estDisponible;
    }

	public int getIdPlace() {
		return idPlace;
	}

	public String getNumero() {
		return numero;
	}

	public int getIdParking() {
		return idParking;
	}

	public boolean isEstDisponible() {
		return estDisponible;
	}
    
    
}
