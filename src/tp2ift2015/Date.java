package tp2ift2015;

public class Date {
	private int mm;
	private int aaaa;
	private int jj;
	
	public Date() {
		
	}
	
	public Date(int aaaa, int mm, int jj) {
		// TODO verifier date valide 
		//  vous devez gérer le nombre de jours par mois et les années bissextiles. 
		// La première date ne sera jamais avant le 2000-01-01. Les dates d'expirations ne vont pas dépasser 2025

		this.mm = mm;
		this.aaaa = aaaa;
		this.jj = jj;
	}

	// nous donne si une date est avant une autre date ou non 
	public boolean estAvant(Date dateAutre) {
	//... 
		return true;
	}
	
	// pour changer la date courante
	public void setDate(int aaaa, int mm, int jj) {
		this.mm = mm;
		this.aaaa = aaaa;
		this.jj = jj;
	}
	
	public Date dateApresXJours(int x) {
		
		return null;
	}


	@Override
	public String toString() {
		return "" + aaaa + "-" + mm + "-" + jj;
	}

}
