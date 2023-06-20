package tp2ift2015;

public class Medicament implements Comparable<Medicament>{
	private final String nomMedicament;
	private final Date dateExpi;
	
	public Medicament(String nomMedicament, Date dateExpi) {
		this.nomMedicament = nomMedicament;
		this.dateExpi = dateExpi;
	}

	public String getNomMedicament() {
		return nomMedicament;
	}

	public Date getDateExpi() {
		return dateExpi;
	}

	@Override
	public String toString() {
		return nomMedicament + ", dateExpi=" + dateExpi;
	}

	@Override
	public int compareTo(Medicament autre) { // on va vouloir un tri par ordre de date d'expi
		return this.dateExpi.convertirDateEnInt() - autre.dateExpi.convertirDateEnInt();
	}
	
	

}
