package tp2ift2015;

public class StockMed {
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
	
	
	

}
