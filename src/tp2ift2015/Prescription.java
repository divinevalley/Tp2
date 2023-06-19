package tp2ift2015;

public class Prescription {
	
	// eg. Medicament1    5       6 
	private Date datePrescrit;
	private int id;
	private int qteParCycle; // 5
	private int nbReps; // 6
	private int qteTotaleCalculee; // 5x6 = 30
	private String nomMedicament; // Medicament1
	private boolean enStock; // true if "ok"; false if "commande" 
	
	
	public Prescription(Date datePrescrit, int id,  String nomMedicaments, int qteParCycle, int nbReps) {
		this.datePrescrit = datePrescrit; // on mettrait la date courante
		this.id = id;
		this.qteParCycle = qteParCycle;
		this.nbReps = nbReps;
		this.qteTotaleCalculee = qteParCycle * nbReps; // on automatise le calcul de la qte totale 
		this.nomMedicament = nomMedicaments;
		this.enStock = true; // default true
	}
	
	public Date getDatePrescrit() {
		return datePrescrit;
	}
	public void setDatePrescrit(Date datePrescrit) {
		this.datePrescrit = datePrescrit;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public int getQteParCycle() {
		return qteParCycle;
	}
	public void setQteParCycle(int qteParCycle) {
		this.qteParCycle = qteParCycle;
	}
	public int getQteNbReps() {
		return nbReps;
	}
	public void setQteNbReps(int qteNbReps) {
		this.nbReps = qteNbReps;
	}

	public int getQteTotaleCalculee() {
		return qteTotaleCalculee;
	}

	public String getMedicament() {
		return nomMedicament;
	}
	
	public boolean estEnStock() {
		return enStock;
	}
	public void setEnStock(boolean enStock) {
		this.enStock = enStock;
	}

	@Override
	public String toString() {
		return "PRESCRIPTION " + id + " "+ datePrescrit + ", " + nomMedicament + " " +  qteParCycle
				+ "x" + nbReps + "=" + qteTotaleCalculee + ", enStock?=" + enStock;
	}
	
	
	
}
