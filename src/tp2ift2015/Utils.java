package tp2ift2015;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

public class Utils {


	public static String lireFichier(String nomFichier, TreeMap<String, TreeMap<Medicament, Integer>> stock, TreeMap<String, Integer> commandes) {
		String toPrint = "";
		try{
			FileReader fileReader = new FileReader(nomFichier);
			BufferedReader br = new BufferedReader(fileReader);
			String line;

			char transactionType = 0; // pour stocker à quoi on fait affaire ("DATE", "APPROV", etc)
			int linenb = 0; 
			int prescrId = 1;
			Date date = new Date();
			while((line = br.readLine()) != null) {
				linenb++; //pour deboguage 
				
				// si DATE, besoin de traiter immediatement car sur une meme ligne. eg. "DATE aaaa-mm-jj" 
				if (line.contains("DATE")) {  
					transactionType='D';
					
					//lire date et mettre a jour date courante
					String[] colDate = line.split("\\s+");
					date = parseDate(colDate[1]); // MAJ date courante
					
					// construire liste Commandes a afficher
					toPrint += date + " ";
					System.out.print(date + " "); // TODO remove
					String commandesAAfficher = afficherCommandes(commandes);
					toPrint += commandesAAfficher;
					System.out.print(commandesAAfficher + "\n"); //TODO remove
				}

				// si on voit STOCK, on doit traiter immediatement car sur la meme ligne
				if (line.contains("STOCK")) {  
					transactionType='S';
					// regarder dans stock et màj (virer les périmés), puis afficher le stock actualisé 
					toPrint += jeterPerimesEtAfficher(stock, date);
					System.out.print(jeterPerimesEtAfficher(stock, date)); // TODO remove
				}
				
				if (line.contains(";")) {
					if (transactionType== 'A') {
						toPrint += "APPROV OK";
						System.out.print("APPROV OK"); // TODO remove
					}
					toPrint += "\n";
					System.out.print("         ; coucou ; line" + linenb + "  \n"); // TODO remove
					transactionType = 0; // si ";", réinitialiser codage du type pour qu'on ne le traite pas (sauter la ligne)  

				}
				
				// pour les types de transaction APPROV et PRESCRIPTION, on se base sur ce que le code a lu dans la derniere boucle
				if (transactionType == 'A' && !line.trim().equals(";")) {
					stockerMedicament(line, stock);					
				}

				// pour PRESCRIPTION : 
				if (transactionType == 'P' && !line.trim().equals(";")) {
					String resultatPrescription = lirePrescriptions(line, date, stock, commandes);
					toPrint += resultatPrescription;
					System.out.print(resultatPrescription); // TODO remove
				}
				
				// pour les entêtes ("APPROV", "PRESCRIPTION"), on ne va pas les traiter immédiatement
				// on va coder le type de transaction pour que la prochaine boucle sache quoi faire
				if (line.contains("APPROV")) {
					transactionType='A';
				} else if (line.contains("PRESCRIPTION")) {
					transactionType='P';
					toPrint += "PRESCRIPTION " + prescrId + "\n"; // afficher "PRESCRIPTION" avec id
					System.out.print("PRESCRIPTION " + prescrId + "\n"); // TODO remove
					prescrId ++;
				} else if(line.trim().equals(";")) { // qd la ligne a juste un ";", mettre entree de ligne

				}
			}
			// TODO add la sortie String ici? 
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
			System.out.println("Absolute path:" + new File(nomFichier).getAbsolutePath());
		}
		return toPrint;
	}
	
	// A partir d'un String aaaa-mm-jj, lire et instancier un objet Date
	public static Date parseDate(String dateString) {
		String[] dateDecomposee = dateString.split("-");
		Date date = new Date (dateDecomposee[0], dateDecomposee[1], dateDecomposee[2]);   
		return date;
	}

	// construire String de commandes a afficher et vider la liste de commandes
	public static String afficherCommandes(TreeMap<String, Integer> commandes) {

		String commandesToPrint = "";
		for (Map.Entry<String, Integer> entry : commandes.entrySet()) { // va pas s'executer si vide
			String nomMedCommande = entry.getKey();
			Integer qteCommandee = entry.getValue();
			commandesToPrint += nomMedCommande + " " + qteCommandee + "\n";
		}
		System.out.println(" commandes : " + commandes); //TODO remove
		String toPrint = commandes.isEmpty() ? "OK\n" : "COMMANDES :\n" + commandesToPrint; 
		commandes.clear(); // vider la liste de commandes
		return toPrint;
	}
	
	// jeter les médicaments perimés, afficher les médicaments en date
	// O(N) parcours tous les elements (au pire) du stock pour jeter
	public static String jeterPerimesEtAfficher(TreeMap<String, TreeMap<Medicament, Integer>> stock, Date date) {
		String stockToPrint = "STOCK " + date + "\n";
		
		// parcourir tous les médicaments et comparer la date // TODO profiter du fait que c'est trie par date deja 
		Iterator<Entry<String, TreeMap<Medicament, Integer>>> itrStock = stock.entrySet().iterator();
		while (itrStock.hasNext()) {
			Entry<String, TreeMap<Medicament, Integer>> entryStock = itrStock.next(); 
			String nomMed = entryStock.getKey();
			TreeMap<Medicament, Integer> mapMedsAvecMemeNom = entryStock.getValue();
			
			//pour un nomMedicament donné, parcourir les medicaments (contient meme nom med avec diff dates expi)
			Iterator<Entry<Medicament, Integer>> itrMedicament = mapMedsAvecMemeNom.entrySet().iterator();
			while(itrMedicament.hasNext()) {
				Entry<Medicament, Integer> stockMedicament = itrMedicament.next();
				Date dateExpiStock = stockMedicament.getKey().getDateExpi();
				if (dateExpiStock.estAvant(date)) { // si périmé, supprimer 
					System.out.println(nomMed + " " + stockMedicament.getValue() + " " + dateExpiStock + "perime!");
					itrMedicament.remove();
				} else { //sinon afficher le medicament
					stockToPrint += nomMed + " " + stockMedicament.getValue() + " " + dateExpiStock + "\n";
				}
			}
			
			if (mapMedsAvecMemeNom.isEmpty()){
				itrStock.remove();
			} 
		}
		
		return stockToPrint;
	}
	
	// servira pour lire chaque ligne du type A (approv), instancier et ajouter le Médicament au stock 
	public static void stockerMedicament(String lineALire, TreeMap<String, TreeMap<Medicament, Integer>> stock) {

		String[] colApprov = lineALire.split("\\s+"); //split en colonnes. eg. line: Medicament1 120 2018-05-29
		String nomMedicamentRecu = colApprov[0];
		int qteRecu = Integer.parseInt(colApprov[1]);
		Date dateExpiMedic = parseDate(colApprov[2]);   
		Medicament medicamentRecu = new Medicament(nomMedicamentRecu, dateExpiMedic);
		
		// TODO gerer si on l'a deja pour en rajouter au stock
		if (!stock.containsKey(nomMedicamentRecu)) { // si ca n'existe pas du tout dans le stock, creer
			TreeMap<Medicament, Integer> newMapPourCeNomMed = new TreeMap<>();
			newMapPourCeNomMed.put(medicamentRecu, qteRecu);
			stock.put(nomMedicamentRecu, newMapPourCeNomMed);
		} else { // si ca existe 
			TreeMap<Medicament, Integer> mapCorrespondante = stock.get(nomMedicamentRecu);
			if (mapCorrespondante.containsKey(medicamentRecu)) {   // si le médicament exacte existe, on a juste besoin de maj la qté
				qteRecu += mapCorrespondante.get(medicamentRecu);
			}

			mapCorrespondante.put(medicamentRecu, qteRecu); // dans tous les cas, màj la map 
		}
		System.out.println(stock);
	}

// parser la liste de médicaments prescrits, soustraire des stocks, ou commander si besoin,   
	public static String lirePrescriptions(String lineALire, Date dateCour, TreeMap<String, TreeMap<Medicament, Integer>> stock, TreeMap<String, Integer> commandes){
		
		String[] colPrescri = lineALire.split("\\s+|\t"); //split en colonnes. eg. line: Medicament1    5       6 
		String nomMedicament = colPrescri[0]; // eg. Medicament1
		System.out.println("-----------\n nomMedicament: " + nomMedicament); //TODO
		int qteParCycle = Integer.parseInt(colPrescri[1]); // eg. 5
		int nbReps = Integer.parseInt(colPrescri[2]); // eg. 6
		int qteTotaleBesoin = qteParCycle * nbReps; // 5x6 = 30
		System.out.println("besoin de " + qteTotaleBesoin); //TODO

		String toPrint = "";
		boolean besoinCommander = false;
		
		System.out.println("date auj: " + dateCour);
		Date besoinJusquau = dateCour.dateApresXJours(qteTotaleBesoin);
		System.out.println("besoin jusqua " + besoinJusquau);
		System.out.println(stock);
		
		int nbMedsACommander = qteTotaleBesoin;
		if (stock.containsKey(nomMedicament)){ // si med existe

			//itérer sur nos Medicaments (medicament-date), voir date
			Iterator<Entry<Medicament,Integer>> itrStock = stock.get(nomMedicament).entrySet().iterator(); 
			nbMedsACommander = compterNbMedicamentsACommander(itrStock, besoinJusquau, qteTotaleBesoin, false); //compter si stock (en date) est suffisant 	

			// on a compté. il est possible que les méd en stock en date soit insuffisant   
			if (nbMedsACommander>0) { // donc il faut commander dans ce cas 
				commandes.put(nomMedicament, nbMedsACommander); 
				besoinCommander = true;
			} else {// sinon, ca veut dire nbMedsACommander = 0, donc pas besoin de commander. 
				//decrementer stock
				itrStock = stock.get(nomMedicament).entrySet().iterator(); //reinitialiser itr au debut
				int result = compterNbMedicamentsACommander(itrStock, besoinJusquau, qteTotaleBesoin, true);
				System.out.println("*********result (should be 0): " + result);
			}

			// si on a tout pris d'un certain nomMedicament, supprimer completement du stock
			if (stock.get(nomMedicament).isEmpty()) {
				System.out.println("nomMed n'existe plus! on supprime du stock! ");
				stock.remove(nomMedicament); 
			}
		} else { // si nom medicament n'existe pas du tout dans stock, commander
			System.out.println("n'existe pas du tout. commander.");
			besoinCommander = true;
			 // need to take into account CUMULATIVE amt (if med already on commande list)
			if (commandes.containsKey(nomMedicament)) {
				int totalDejaSurCommande = commandes.get(nomMedicament);
				commandes.put(nomMedicament, nbMedsACommander + totalDejaSurCommande);
			} else {
				commandes.put(nomMedicament, nbMedsACommander);
			}
		}
		String indicateurCommande = besoinCommander ? "COMMANDE" : "OK";
		
//		System.out.println(prescription);
		toPrint += nomMedicament + " " + qteParCycle + " " + nbReps + " " + indicateurCommande + "\n"; 
		return toPrint;
	}
	
	
	
	
	
	// en gros on "essaie" de rempli au besoin et voit si on arrive ou pas. Ca permet de savoir si on en a assez en stock en date ou pas. avant qu'on le décremente "pour de vrai" 
	private static int compterNbMedicamentsACommander(Iterator<Entry<Medicament,Integer>> itrStock, Date besoinJusquau, int qteTotaleBesoin, boolean decrementerStock) {
		System.out.println("function is running! boolean = " + decrementerStock);
		// controlleurs boucle while: 
		boolean premierePartie = true;
		boolean deuxiemePartie = false;
		
		int qteBesoinADecrementer = qteTotaleBesoin; // on a besoin que qteTotaleBesoin reste le meme, pour servir de référence 

		while(itrStock.hasNext()) {
			Entry<Medicament, Integer> medicamentEtQte = itrStock.next(); // avancer itr
			System.out.println("avancer itr... " + medicamentEtQte);
			
			if (premierePartie) { // avancer itr jusq au premier med en date

				Date dateExpiMed = medicamentEtQte.getKey().getDateExpi();
				System.out.println("Premiere Partie\nDateExpiMed " + dateExpiMed);
				
				boolean dateExpiTropTot = dateExpiMed.estAvant(besoinJusquau); // donne false si on arrive à un méd qui sera en date pour toute la durée. false => sortir boucle
				System.out.println("dateexpitroptot = " + dateExpiTropTot); //TODO remove

				premierePartie =  dateExpiTropTot; // (WHILE condition --> va devenir false)
				deuxiemePartie = !premierePartie; // qd 1ere partie se desactive, ça active la 2e partie boucle
				
				if (premierePartie) { // une fois que c'est false, on va directement à la deuxième partie (sans itérer Next)
					continue;
				}
				
			}
			if (deuxiemePartie) { //mnt on est au bon médic en date. check stock 
				
				int qteEnStock = medicamentEtQte.getValue();
				System.out.println("C'est bon! trouvé méd en date. Deuxieme Partie. qte en stock: " + qteEnStock); //TODO remove
				// check stock
				if(qteEnStock > qteBesoinADecrementer) { // si stock est plus que suffisant, on prend la quantité exacte
					int qteQuiReste = qteEnStock - qteBesoinADecrementer;
					System.out.println("decrementerStock: " + decrementerStock);
					if (decrementerStock) {
						System.out.println("setting value... " + medicamentEtQte + " value : " + qteQuiReste);
						medicamentEtQte.setValue(qteQuiReste); //MAJ qte dans stock // XXX  ne pas faire si false 
					}
					qteBesoinADecrementer = 0; 
					deuxiemePartie = false; // => sortir boucle		
	
				} else { // si stock insuffisant, 
					qteBesoinADecrementer -= qteEnStock; // on prend TOUT et on passe au prochain médicament
					
					if (decrementerStock) {
						itrStock.remove(); // XXX ne pas faire si false	
					}
					 
//					System.out.println("======== removing... " + medicamentEtQte);
				}

			}
		}
		
		return qteBesoinADecrementer; // si on a tout ce qu'il nous faut, ce sera 0. sinon, ce serait le nb à commander. 

	}
	
}
