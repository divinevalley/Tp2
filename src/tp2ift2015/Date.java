package tp2ift2015;

public class Date {
	private String aaaa, mm, jj;
	private int aaaaInt, mmInt, jjInt; 

	public Date() {
	}

	public Date(String aaaa, String mm, String jj) {
		//  vous devez gérer le nombre de jours par mois et les années bissextiles. 
		// La première date ne sera jamais avant le 2000-01-01. Les dates d'expirations ne vont pas dépasser 2025

		aaaaInt = Integer.parseInt(aaaa);
		mmInt = Integer.parseInt(mm);
		jjInt = Integer.parseInt(jj);

		// valider date
		if (jjInt <= nbJoursParMois(mmInt, aaaaInt) && aaaaInt>0 && mmInt > 0 && jjInt > 0 && mmInt<13) { 
			// (on ne veut pas de trucs 2020-31-02, on verifie qu'il y a aucun negatif) 
			this.aaaa = aaaa;
			this.mm = mm;
			this.jj = jj;
		} else {
			//			throw new IllegalStateException("Date non valide ! : " + aaaa + "-" + mm + "-" + jj);
			System.err.println("Date non valide ! : " + aaaa + "-" + mm + "-" + jj);
			this.aaaa = aaaa;
			this.mm = mm;
			this.jj = ""+ nbJoursParMois(mmInt, aaaaInt);
		}
	}

	public static boolean aBesoinZero(int mmOuJj) {
		boolean aBesoinZero = false;
		if (mmOuJj<10) {
			aBesoinZero = true;
		}
		return aBesoinZero;
	}

	// retourne true si this date est avant une autre date 
	public boolean estAvant(Date dateAutre) {

		if (this.aaaaInt<dateAutre.aaaaInt) {// si clairement l'année autre est après, on s'arrête la
			return true;
		} else if (this.aaaaInt>dateAutre.aaaaInt) {
			return false;
		} else {

			// si annee egale, regarder mois
			if (this.mmInt < dateAutre.mmInt) {
				return true;
			} else if (this.mmInt > dateAutre.mmInt){
				return false; 
			} else {
				// si meme mois, regarder jour...
				if (this.jjInt < dateAutre.jjInt) {
					return true;
				} else { // si exactement meme date on va dire false?
					return false;
				}
			}
		}
	}

	public Date dateApresXJours(int x) {

		int newJour = this.jjInt + x; // on ajoute X, voir si ça déborde 
		int newMois = this.mmInt; // commencer a compter depuis mois courant, avancer si besoin 
		int newAnnee = this.aaaaInt; 

		while (newJour > nbJoursParMois(newMois, newAnnee)) { // si l'ajout du X depasse ("deborde" sur) le mois courant  
			newJour -= nbJoursParMois(newMois, newAnnee); // on reduit le nombre de jours, et on passe au mois suivant 
			newMois++;
			if (newMois>12) { // si on deborde a la fin de dec, on passe a la prochaine annee
				newMois-=12;
				newAnnee++;
			}	
		}

		//convertir en String pour instancier avec les 0 si besoin (pour format 2020-01-01 plutot que 2020-1-1)
		String newAnneeString = "" + newAnnee;
		String newMoisString = aBesoinZero(newMois) ? "0" + newMois : "" + newMois;
		String newJourString = aBesoinZero(newJour) ? "0" + newJour : "" + newJour;

		Date dateApresXJours = new Date(newAnneeString, newMoisString, newJourString);
		return dateApresXJours;
	}

	// retourner un int qui représente le nombre de jours depuis le 2000-01-01 	(inclusif)
	public int convertirDateEnInt() { //TODO re-test
		int anneeThis = Integer.parseInt(aaaa);
		int moisThis = Integer.parseInt(mm);
		int jourThis = Integer.parseInt(jj);

		int nbJours = 0; //counter 

		int nbAnneesBissextilesPassees = 1 + (anneeThis - 2000)/4; // eg. on est 2005, on compte 1 pour l'année 2000, puis on fait 5/4 => 1 + 1 => 2 annees (2000, 2004)
		nbAnneesBissextilesPassees += estBissextile(anneeThis) ? -1 : 0; // ajustement si bissextile 
		nbJours += (anneeThis - 2000) * 365 + (1 * nbAnneesBissextilesPassees); // + nb jours depuis 2000 jusqu'à cette année 

		for (int i = 1; i < moisThis; i++) { // + nb jours dans les mois passés de cette année 
			nbJours += nbJoursParMois(i, anneeThis);
		}

		nbJours += jourThis; // + nb jours ce mois ci 

		return nbJours;
	}

	public static int nbJoursParMois(int mois, int annee) {
		int nbJours = 31; // par defaut
		if (mois == 2) { // si fevr, = 28 ou
			nbJours = 28;
			if (estBissextile(annee)) { // = 29 si bissextile
				nbJours=29;
			}
		} else if (mois == 4 || mois == 6 || mois == 9 || mois == 11) {
			nbJours = 30; // si avr, juin, sep, nov, = 30. 
		}
		return nbJours;
	}

	public static boolean estBissextile(int annee) {
		boolean estBissextile = false;
		if (annee % 4 == 0) {
			estBissextile = true;
		}
		return estBissextile;
	}


	@Override
	public String toString() {
		return "" + aaaa + "-" + mm + "-" + jj;
	}

}
