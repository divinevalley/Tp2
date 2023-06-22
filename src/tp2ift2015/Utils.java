package tp2ift2015;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/***
 * Class Utils contient des fonctions static qui serviront dans l'application main (Tp2) 
 * On ne va jamais instancier Utils. 
 * 
 * Les complexités temporelles devraient être exprimées en fonction de : 
 * n indique le nombre de types de médicaments différents; 
 * m indique le nombre d'items sur la prescription
 * k indique le nombre d'items sur la liste de demande; 
 * p indique le nombre de médicaments (maximal) partageant le même type; 
 * q représente le nombre d'items sur APPROV au total, que ces médicaments soient du 
 * même type ou pas (donc q <= n * p)
 */
public class Utils {

	/***
	 * Cette fonction sert à parser le fichier d'entrée, traiter les données, et renvoie le String final à mettre dans le fichier de sortie.
	 * Cette fonction appelle d'autres fonctions afficherCommandes(), lirePrescription(), jeterPerimesEtAfficher(), stockerMedicament()    
	 * 
	 * @param nomFichier
	 * @param stock
	 * @param commandes
	 * @return String à mettre dans le contenu du fichier (sera passé comme paramètre dans creerFichierFinal())  
	 */
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
					date = parseDate(colDate[1]); 

					// construire liste Commandes a afficher
					toPrint += date + " ";
					String commandesAAfficher = afficherCommandes(commandes);
					toPrint += commandesAAfficher;
				}

				// si on voit STOCK, on doit traiter immediatement car sur la meme ligne
				if (line.contains("STOCK")) {  
					transactionType='S';
					// regarder dans stock et màj (jeter les périmés), puis afficher le stock actualisé 
					toPrint += jeterPerimesEtAfficher(stock, date); // O (n*p)
				}

				if (line.contains(";")) {
					// le ";" signale que c'est la fin de la transaction. C'est comme ça qu'on sait qu'on a fini la transaction APPROV  
					if (transactionType== 'A') {
						toPrint += "APPROV OK";
					}
					toPrint += "\n";
					transactionType = 0; // si ";", réinitialiser codage du type pour qu'on ne le traite pas (sauter la ligne)  
				}

				// pour les types de transaction APPROV et PRESCRIPTION, on se base sur ce que le code a lu dans la derniere boucle
				if (transactionType == 'A' && !line.trim().equals(";")) {
					stockerMedicament(line, stock);		// Complexité O(logN)			
				}

				// pour PRESCRIPTION :
				if (transactionType == 'P' && !line.trim().equals(";")) {
					String resultatPrescription = lirePrescriptions(line, date, stock, commandes); // Complexité O(N + log(k))
					toPrint += resultatPrescription;
				}

				// Pour les entêtes ("APPROV", "PRESCRIPTION"), on ne va pas les traiter immédiatement
				// on va coder le type de transaction pour que la prochaine boucle sache quoi faire
				if (line.contains("APPROV")) {
					transactionType='A';
				} else if (line.contains("PRESCRIPTION")) {
					transactionType='P';
					toPrint += "PRESCRIPTION " + prescrId + "\n"; // afficher "PRESCRIPTION" avec id
					prescrId ++;
				} 
			}
		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
			System.out.println("Absolute path:" + new File(nomFichier).getAbsolutePath());
		}
		return toPrint;
	}

	/***
	 * A partir d'un String aaaa-mm-jj, lire et instancier un objet Date
	 * 
	 * @param dateString
	 * @return objet Date
	 */
	public static Date parseDate(String dateString) {
		String[] dateDecomposee = dateString.split("-");
		Date date = new Date (dateDecomposee[0], dateDecomposee[1], dateDecomposee[2]);   
		return date;
	}

	/***
	 * Transaction DATE
	 * 
	 * Construire String de commandes a afficher et vider la liste de commandes
	 * 
	 * Complexité : parcours une fois tous les éléments de commande (déjà trié donc pas besoin de trier). 
	 * = O(k) , où k indique nombre d'items sur la liste de commande; 
	 *     
	 * @param commandes
	 * @return String contenant toutes les commandes actuellement en cours à date
	 */
	public static String afficherCommandes(TreeMap<String, Integer> commandes) {

		String commandesToPrint = "";
		for (Map.Entry<String, Integer> entry : commandes.entrySet()) { // va pas s'executer si vide
			String nomMedCommande = entry.getKey(); // O(1) car on est déjà sur l'élément 
			Integer qteCommandee = entry.getValue(); // O(1) 
			commandesToPrint += nomMedCommande + " " + qteCommandee + "\n";
		}

		String toPrint = commandes.isEmpty() ? "OK\n" : "COMMANDES :\n" + commandesToPrint; 
		commandes.clear(); // vider la liste de commandes
		return toPrint;
	}

	/***
	 * DATE 
	 * Jeter les médicaments perimés, afficher les médicaments en date
	 * 
	 * Complexité : O(n*p), où n indique le nombre de types de médicaments différents
	 * et p indique le nombre (maximal) de médicaments du même type  
	 * 
	 * (et donc n * p = médicaments au total)
	 * 
	 * Les deux boucles for servent à parcourir tous les n*p éléments qui
	 * sont stockés dans une TreeMap dans une TreeMap (chaque médicament  
	 * est repertorié qu'une seule fois). Par exemple : 
	 * 
	 * Medicament11={Medicament11, dateExpi=2012-04-01, qte=95}, 
	 * Medicament12={Medicament12, dateExpi=2010-02-01, qte=52; Medicament12, dateExpi=2010-06-01, qte=27}, 
	 * Medicament14={Medicament14, dateExpi=2018-03-01, qte=110}, 
	 * Medicament15={Medicament15, dateExpi=2010-06-01, qte=57}, 
	 * Medicament17={Medicament17, dateExpi=2012-01-01, qte=37; Medicament17, dateExpi=2016-10-01, qte=54}, 
	 * 
	 * Ici n = 5, p = 2 
	 * 
	 * Il s'agit d'un parcours simple (une fois) sur chacun des n*p éléments. 
	 * Les autres opérations sont de complexité constante, grace à l'utilisation de l'itérateur. 
	 * 
	 * @param stock
	 * @param date
	 * @return
	 */
	public static String jeterPerimesEtAfficher(TreeMap<String, TreeMap<Medicament, Integer>> stock, Date date) {
		String stockToPrint = "STOCK " + date + "\n";

		// parcourir tous les médicaments et comparer la date
		Iterator<Entry<String, TreeMap<Medicament, Integer>>> itrStock = stock.entrySet().iterator();
		while (itrStock.hasNext()) {
			Entry<String, TreeMap<Medicament, Integer>> entryStock = itrStock.next(); 
			String nomMed = entryStock.getKey(); // Complexité : O(1) car on est déjà sur l'élément avec l'itérateur
			TreeMap<Medicament, Integer> mapMedsAvecMemeNom = entryStock.getValue(); // O(1) déjà dessus avec l'itérateur 

			//pour un nomMedicament donné, parcourir les medicaments (contient meme nom med avec diff dates expi)
			Iterator<Entry<Medicament, Integer>> itrMedicament = mapMedsAvecMemeNom.entrySet().iterator();
			while(itrMedicament.hasNext()) {
				Entry<Medicament, Integer> stockMedicament = itrMedicament.next();
				Date dateExpiStock = stockMedicament.getKey().getDateExpi();
				if (dateExpiStock.estAvant(date)) { // si périmé, supprimer 
					itrMedicament.remove(); // Complexité : O(1), car on est déjà dessus 
				} else { //sinon afficher le medicament
					stockToPrint += nomMed + " " + stockMedicament.getValue() + " " + dateExpiStock + "\n";
				}
			}
			if (mapMedsAvecMemeNom.isEmpty()){
				itrStock.remove(); // O(1)
			} 
		}
		return stockToPrint;
	}

	/***
	 * APPROV
	 * Lire chaque ligne du type A (APPROV), instancier et ajouter le Médicament au stock
	 * 
	 * Complexité de cette fonction seule (pour une seule ligne, donc un seul médicament lu) : 
	 * O(logN) à cause des opérations de .get et/ou .put dans une Map et aucune boucle
	 * 
	 * Complexité globale de toutes les lignes de transaction APPROV : 
	 * O(QlogN), où N indique le nombre de types de médicaments différents et Q indique 
	 * le nombre de médicaments au total dans APPROV, qu'ils soient du même type ou pas 
	 * 
	 * @param lineALire
	 * @param stock
	 */
	public static void stockerMedicament(String lineALire, TreeMap<String, TreeMap<Medicament, Integer>> stock) {

		String[] colApprov = lineALire.split("\\s+"); //split en colonnes. eg. line: Medicament1 120 2018-05-29
		String nomMedicamentRecu = colApprov[0];
		int qteRecu = Integer.parseInt(colApprov[1]);
		Date dateExpiMedic = parseDate(colApprov[2]);   
		Medicament medicamentRecu = new Medicament(nomMedicamentRecu, dateExpiMedic);

		if (!stock.containsKey(nomMedicamentRecu)) { // si ca n'existe pas du tout dans le stock, creer
			TreeMap<Medicament, Integer> newMapPourCeNomMed = new TreeMap<>();
			newMapPourCeNomMed.put(medicamentRecu, qteRecu); // O(logN) car besoin de mettre dans l'ordre  
			stock.put(nomMedicamentRecu, newMapPourCeNomMed); // O(logN) 
		} else { // si ca existe 
			TreeMap<Medicament, Integer> mapCorrespondante = stock.get(nomMedicamentRecu); // .get() coute O(logN) 
			if (mapCorrespondante.containsKey(medicamentRecu)) {   // si le médicament exacte existe, on a juste besoin de maj la qté
				qteRecu += mapCorrespondante.get(medicamentRecu); // O(logN) 
			}
			mapCorrespondante.put(medicamentRecu, qteRecu); // dans tous les cas, màj la map 
		}
	}


	/***
	 * PRESCRIPTION
	 * Parser la liste de médicaments prescrits, soustraire des stocks, ou commander si besoin
	 * 
	 * Complexité de cette fonction seule : O(p + log(k) + log(n)) car recherche dichotomique 
	 * dans n types de médicaments du stock, recherche dichotomique dans les k commandes, 
	 * et itération sur les p médicaments partageant le même nom. 
	 * Mais vu que cette fonction va boucler pour chacune des m lignes d'une prescription, 
	 * la complexité globale sera de O(m*(p +log(k) + log(n))). Cela peut être  
	 * simplifié à O(m*(log(n))), car on sait que k <= n et p <= n. 
	 * 
	 * 
	 * @param lineALire
	 * @param dateCour
	 * @param stock
	 * @param commandes
	 * @return
	 */
	public static String lirePrescriptions(String lineALire, Date dateCour, TreeMap<String, TreeMap<Medicament, Integer>> stock, TreeMap<String, Integer> commandes){

		// variables infos line
		String[] colPrescri = lineALire.split("\\s+|\t"); //split en colonnes. eg. line: Medicament1    5       6 
		String nomMedicament = colPrescri[0]; // eg. Medicament1
		int qteParCycle = Integer.parseInt(colPrescri[1]); // eg. 5
		int nbReps = Integer.parseInt(colPrescri[2]); // eg. 6
		int qteTotaleBesoin = qteParCycle * nbReps; // 5x6 = 30
		Date besoinJusquau = dateCour.dateApresXJours(qteTotaleBesoin);

		String toPrint = "";
		boolean besoinCommander = true;

		if (stock.containsKey(nomMedicament)){ // si medicament existe. Complexité O(logN) car recherche dichotomique

			//itérer sur nos objets Medicaments correspondant à ce nom, voir date : 
			Iterator<Entry<Medicament,Integer>> itrStock = stock.get(nomMedicament).entrySet().iterator(); // O(logN)
			while(itrStock.hasNext() && besoinCommander == true) { // Complexité : itérer sur les éléments correspondant au nom : O(p) 
				Entry<Medicament,Integer> nextMed = itrStock.next();
				Date dateExpiMedicamentStock =  nextMed.getKey().getDateExpi(); // Complexité : on est déjà dessus, donc O(1) 
				int qteEnStock = nextMed.getValue();

				// si en date ET qte suffisante pour prendre tout ce qu'il nous faut (il faut tout prendre du meme lot)
				if (besoinJusquau.estAvant(dateExpiMedicamentStock) && (qteEnStock > qteTotaleBesoin)) {
					// on decremente le stock 
					nextMed.setValue(qteEnStock - qteTotaleBesoin); // Complexité : on est déjà dessus, donc O(1) 
					besoinCommander = false; // => sortir boucle
				} else if (besoinJusquau.estAvant(dateExpiMedicamentStock) && (qteEnStock == qteTotaleBesoin)) {
					// il y a exactement la qté qu'il faut, on supprime du stock
					itrStock.remove();
					besoinCommander = false; // ==> sortir boucle
				}
			}
			//menage du stock: si on a tout pris d'un certain nomMedicament, supprimer completement du stock
			if (stock.get(nomMedicament).isEmpty()) {
				stock.remove(nomMedicament); // Complexité : on est déjà dessus (iterator), donc O(1) 
			}
		}

		// une fois sorti de la boucle, il est toujours possible qu'on ait besoin de commander
		// Complexité (de cette partie ci dessous) de O(logk) 
		if (besoinCommander == true) {
			// si med existe déjà sur liste de commandes, rajouter par dessus
			if (commandes.containsKey(nomMedicament)) { // Complexité : besoin de trouver l'élément dans une TreeMep donc O(log(k)) 
				int totalDejaSurCommande = commandes.get(nomMedicament);
				commandes.put(nomMedicament, qteTotaleBesoin + totalDejaSurCommande); // Complexité .put() : O(log(k))  
			} else {
				commandes.put(nomMedicament, qteTotaleBesoin);
			}
		}

		String indicateurCommande = besoinCommander ? "COMMANDE" : "OK";
		toPrint += nomMedicament + " " + qteParCycle + " " + nbReps + " " + indicateurCommande + "\n"; 
		return toPrint;
	}


	/***
	 * Cette fonction crée le fichier .txt final. 
	 * 
	 * @param nomFichierTxt
	 * @param contenuFichier
	 */
	public static void creerFichierFinal(String nomFichierTxt, String contenuFichier) {
		try {
			File fichierACreer = new File(nomFichierTxt); //pour créer fichier
			if (fichierACreer.createNewFile()) {
			}
		} catch (IOException e) {
			System.out.println("Erreur creation du fichier");
			e.printStackTrace();
		}

		try {
			FileWriter fileWriter = new FileWriter(nomFichierTxt); //pour écrire dans le fichier
			fileWriter.write(contenuFichier);
			fileWriter.close();
		} catch (IOException e) {
			System.out.println("Erreur chargement du contenu fichier");
			e.printStackTrace();
		}
	}


}
