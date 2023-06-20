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
					System.out.println(date + " "); // TODO remove
					String commandesAAfficher = afficherCommandes(line, commandes);
					toPrint += commandesAAfficher + "\n";
					System.out.println(commandesAAfficher + "\n"); //TODO remove
				}

				// si on voit STOCK, on doit traiter immediatement car sur la meme ligne
				if (line.contains("STOCK")) {  
					transactionType='S';
					// regarder dans stock et màj (virer les périmés), puis afficher le stock actualisé 
					toPrint += jeterPerimesEtAfficher(stock, date);
					System.out.println(jeterPerimesEtAfficher(stock, date)); // TODO remove
				}
				
				// pour les types de transaction APPROV et PRESCRIPTION, on se base sur ce que le code a lu dans la derniere boucle
				if (transactionType == 'A' && !line.trim().equals(";")) {
					stockerMedicament(line, stock);					
				}

				// pour PRESCRIPTION : 
				if (transactionType == 'P' && !line.trim().equals(";")) {
					String resultatPrescription = lirePrescriptions(line, date, prescrId, stock, commandes);
					toPrint += resultatPrescription;
					System.out.println(resultatPrescription); // TODO remove
				}
				
				// pour les entêtes ("APPROV", "PRESCRIPTION"), on ne va pas les traiter immédiatement
				// on va coder le type de transaction pour que la prochaine boucle sache quoi faire
				if (line.contains("APPROV")) {
					transactionType='A';
				} else if (line.contains("PRESCRIPTION")) {
					transactionType='P';
					toPrint += "PRESCRIPTION " + prescrId + "\n"; // afficher "PRESCRIPTION" avec id
					System.out.println("PRESCRIPTION " + prescrId + "\n"); // TODO remove
					prescrId ++;
				} else if(line.trim().equals(";")) { // qd la ligne a juste un ";", mettre entree de ligne
					switch(transactionType) {
						case 'P':
//							prescrId ++; // quand on voit un ";", et la dernière transaction était P, incrémenter prescrId
							break;
						case 'A':
							toPrint += "APPROV OK";
							System.out.println("APPROV OK"); // TODO remove
							break;
					}
					toPrint += "\n";
					System.out.println(" ; coucou ; " + linenb + "  \n"); // TODO remove
					transactionType = 0; // si ";", réinitialiser codage du type pour qu'on ne le traite pas (sauter la ligne)  
					// TODO add la sortie String ici? 
				}
			}
			
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
	public static String afficherCommandes(String line, TreeMap<String, Integer> commandes) {

		String commandesToPrint = "";
		for (Map.Entry<String, Integer> entry : commandes.entrySet()) { // va pas s'executer si vide
			String nomMedCommande = entry.getKey();
			Integer qteCommandee = entry.getValue();
			commandesToPrint = nomMedCommande + " " + qteCommandee + "\n";
		}
		
		String toPrint = commandes.isEmpty() ? "OK" : "COMMANDES :\n" + commandesToPrint; 
		commandes.clear(); // vider la liste de commandes
		return toPrint;
	}
	
	// jeter les médicaments perimés, afficher les médicaments en date
	public static String jeterPerimesEtAfficher(TreeMap<String, TreeMap<Medicament, Integer>> stock, Date date) {
		String stockToPrint = "STOCK " + date + "\n";
		
		// parcourir tous les médicaments (StockMed) et comparer la date // TODO profiter du fait que c'est trie par date deja 
		Iterator<Entry<String, TreeMap<Medicament, Integer>>> itrStock = stock.entrySet().iterator();
		while (itrStock.hasNext()) {
			Entry<String, TreeMap<Medicament, Integer>> entry = itrStock.next(); // TODO maj 
			String nomMed = entry.getKey();
			TreeMap<Medicament, Integer> mapMedsQuiCorrespon = entry.getValue();
			
			//pour un nomMedicament, parcourir les medicaments dans la Map (contient meme med avec diff dates expi)
			Iterator<Entry<Medicament, Integer>> itrMedicament = mapMedsQuiCorrespon.entrySet().iterator();
			while(itrMedicament.hasNext()) {
				Entry<Medicament, Integer> stockMedicament = itrMedicament.next();
				Date dateExpiStock = stockMedicament.getKey().getDateExpi();
				if (dateExpiStock.estAvant(date)) { // si périmé, supprimer 
					itrMedicament.remove();
				} else { //sinon on va vouloir afficher le medicament
					stockToPrint += nomMed + " " + stockMedicament.getValue() + " " + dateExpiStock + "\n";
				}
			}
			
			if (mapMedsQuiCorrespon.isEmpty()){
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

// parser la liste de médicaments prescrits, instancier objets Prescriptions, soustraire des stocks, ou commander si besoin,   
	public static String lirePrescriptions(String lineALire, Date dateCour, int idPrescription, TreeMap<String, TreeMap<Medicament, Integer>> stock, TreeMap<String, Integer> commandes){
		
		String[] colPrescri = lineALire.split("\\s+|\t"); //split en colonnes. eg. line: Medicament1    5       6 
		String nomMedicament = colPrescri[0]; // eg. Medicament1
		int qteParCycle = Integer.parseInt(colPrescri[1]); // eg. 5
		int nbReps = Integer.parseInt(colPrescri[2]); // eg. 6
		int qteTotaleBesoin = qteParCycle * nbReps; // 5x6 = 30
		Prescription prescription = new Prescription(dateCour, idPrescription, nomMedicament, qteParCycle, nbReps);
//		String indicateurCommande = "";
		String toPrint = "";
		boolean besoinCommander = true;
		
		
		Date besoinJusquau = dateCour.dateApresXJours(qteTotaleBesoin);
		
		if (stock.containsKey(nomMedicament)){ // si med existe, on va essayer, soustraire des stocks
			
			//itérer sur nos Medicaments (medicament-date), voir date
			Iterator<Entry<Medicament,Integer>> itrStock = stock.get(nomMedicament).entrySet().iterator();
			
			while(itrStock.hasNext() && qteTotaleBesoin > 0) { // tant qu'il y a du stock à explorer et qu'on n'a pas encore rempli Rx   
				Entry<Medicament, Integer> medicamentEtQte = itrStock.next(); 
				int qteEnStock = medicamentEtQte.getValue();
				
				if(qteEnStock > qteTotaleBesoin) { // si pas besoin de tout prendre, on ne prend que ce qu'il faut (quantité exacte)
					int qteQuiReste = qteEnStock - qteTotaleBesoin;
					medicamentEtQte.setValue(qteQuiReste); //MAJ qte dans stock
					qteTotaleBesoin = 0; 
					besoinCommander = false; // seulement si on arrive à tout remplir, on va mettre false 
				} else { // si stock insuffisant, 
					qteTotaleBesoin -= qteEnStock; // on prend TOUT
					itrStock.remove();
//					System.out.println("======== removing... " + medicamentEtQte);
				}
			}
			
			// qd on sort, ça veut dire que, soit (1) on a tout ce qu'il nous faut, soit (2) on a terminé mais il en manque encore... possible besoin de commander

			// si on a tout pris, supprimer completement du stock
			if (stock.get(nomMedicament).isEmpty()) { // TODO: is this ok? 
				stock.remove(nomMedicament); 
			}
		} else { // si besoin commander, ajouter à la liste de commandes
			
			prescription.setEnStock(false); // rappel: true par defaut // TODO pas besoin attribut ? 
			commandes.put(nomMedicament, qteTotaleBesoin); 
		}
		String indicateurCommande = besoinCommander ? "COMMANDE" : "OK";
		
//		System.out.println(prescription);
		toPrint += nomMedicament + " " + qteParCycle + " " + nbReps + " " + indicateurCommande + "\n"; 
		return toPrint;
	}
	
	// sera appelée dans lirePrescriptions()
	// si besoin commander, return int qte à commander
	public static boolean besoinCommander(Date dateCour, String nomMedicament, int besoinQteTotale, TreeMap<String, TreeMap<Medicament, Integer>> stock) {
		Date besoinJusquau = dateCour.dateApresXJours(besoinQteTotale);
		Boolean besoinCommander = false; 

		if (stock.containsKey(nomMedicament)) { // si le nomMedicament existe dans notre stock 
			TreeMap<Medicament, Integer> medicamentsQuiCorrespon = stock.get(nomMedicament);
			
			Iterator<Entry<Medicament, Integer>> itr = medicamentsQuiCorrespon.entrySet().iterator();
			while(itr.hasNext() && besoinQteTotale>0) {
				Entry<Medicament, Integer> medicamentEtQte = itr.next();
				int qteEnStock = medicamentEtQte.getValue();
				
				if(qteEnStock < besoinQteTotale) { // si stock insuffisant,
					besoinQteTotale -= qteEnStock; // prend TOUT, et on diminue le besoin (jusqu'à 0) 
				}
			}
			
			
			if (medicamentsQuiCorrespon.firstKey().getDateExpi().estAvant(besoinJusquau)) { //check date stock 
				besoinCommander = true;  // TODO. on doit continuer et regarder le prochain médicament et sa date 
			}
		} else { // si stock existe pas, commander 
			besoinCommander = true;
		}
		return besoinCommander;
	}

	public ArrayList<Prescription> traiterPrescriptions(ArrayList<Prescription> listePrescription, TreeSet<Medicament> stock){
		// pour chaque médicament dans la liste de prescriptions, checker stock voir si on en a ou pas, 
		// et si en date ou pas, pour la quantité 
		// sinon, calculer quantité à commander 
		// modifier la liste de Prescription d'origine pour indiqué en stock ou commandé, 
		// renvoie la liste de commandes 

		return null;
	}
}
