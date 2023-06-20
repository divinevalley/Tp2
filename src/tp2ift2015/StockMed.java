package tp2ift2015;

// TODO supprimer cette classe? 

public class StockMed implements Comparable <StockMed>{
	private Medicament medicament;
	private int qte;
	
	public StockMed(Medicament medicament, int qte) {
		this.medicament = medicament;
		this.qte = qte;
	}

	public Medicament getMedicament() {
		return medicament;
	}

	public void setMedicament(Medicament medicament) {
		this.medicament = medicament;
	}

	public int getQte() {
		return qte;
	}

	public void setQte(int qte) {
		this.qte = qte;
	}

	@Override
	public String toString() {
		return medicament + ", qte=" + qte;
	}

	@Override
	public int compareTo(StockMed autre) { // on va vouloir un tri par ordre de date d'expi
		return this.medicament.getDateExpi().convertirDateEnInt() - autre.medicament.getDateExpi().convertirDateEnInt();
	} 
	
	
	

}
