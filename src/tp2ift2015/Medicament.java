package tp2ift2015;

/**
 * Class Medicament contient le nom du médicament ainsi que sa date d'expiration.
 * Et lorsqu'on met un médicament dans le stock (TreeMap), il sera 
 * trié par date d'expiration (ordre croissant). d'où le besoin du 
 * implements Comparable. 
 *
 */
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
