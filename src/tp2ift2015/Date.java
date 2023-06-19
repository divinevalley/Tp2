package tp2ift2015;

public class Date {
	private String mm;
	private String aaaa;
	private String jj;
	
	public Date() {
		
	}
	
	public Date(String aaaa, String mm, String jj) {
		// TODO verifier date valide 
		//  vous devez gérer le nombre de jours par mois et les années bissextiles. 
		// La première date ne sera jamais avant le 2000-01-01. Les dates d'expirations ne vont pas dépasser 2025

		this.mm = mm;
		this.aaaa = aaaa;
		this.jj = jj;
	}

	// retourne true si this date est avant une autre date 
	public boolean estAvant(Date dateAutre) {
		int anneeThis = Integer.parseInt(aaaa);
		int anneeAutre = Integer.parseInt(dateAutre.aaaa);
		int moisThis = Integer.parseInt(mm);
		int moisAutre = Integer.parseInt(dateAutre.mm);
		int jourThis = Integer.parseInt(jj);
		int jourAutre = Integer.parseInt(dateAutre.jj);
		
		if (anneeThis<anneeAutre) {
			return true; // si clairement l'année est avant, on s'arrête là
		} else if (anneeThis > anneeAutre) {// si clairement l'année est après, on s'arrête
			return false;
		} else { // si annee egale, besoin de calculer 
			
			if (moisThis<moisAutre) { // meme logique, mnt regarder mois
				return true;
			} else if (moisThis > moisAutre) {
				return false;
			} else { // si meme mois, regarder date
				
				if (jourThis < jourAutre) {
					return true;
				} else if (jourThis > jourAutre) {
					return false;
				} else { // si exactement meme date on va dire false?
					return false;
				}
			}
		}
	}
	
	public Date dateApresXJours(int x) {
		//TODO 
		
		return null;
	}

	// retourner un int qui représente le nombre de jours depuis le 2000-01-01 	
	public int convertirDateEnInt() {
		int anneeThis = Integer.parseInt(aaaa);
		int moisThis = Integer.parseInt(mm);
		int jourThis = Integer.parseInt(jj);
		
		int anneesPassees = anneeThis - 2000; 
		int moisPasses = moisThis - 1;
		int dateEnInt = moisPasses * 30 + jourThis; // TODO approx ! 

		return dateEnInt;
	}


	@Override
	public String toString() {
		return "" + aaaa + "-" + mm + "-" + jj;
	}

}
