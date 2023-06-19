package tp2ift2015;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class Utils {


	public static String lireFichier(String nomFichier, TreeMap<String, StockMed> stock, TreeMap<String, Integer> commandes) {
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
	public static String jeterPerimesEtAfficher(TreeMap<String, StockMed> stock, Date date) {
		String stockToPrint = "STOCK " + date + "\n";
		
		// parcourir tous les médicaments (StockMed) et comparer la date
		Iterator<Map.Entry<String, StockMed>> iterator = stock.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, StockMed> entry = iterator.next();
			String nomMed = entry.getKey();
			StockMed stockMed = entry.getValue();	
			Date dateExpiStock = stockMed.getMedicament().getDateExpi();
			
			if (dateExpiStock.estAvant(date)) { // si périmé, supprimer 
				iterator.remove();
			} else { // sinon afficher le médicament
				stockToPrint += nomMed + " " + stockMed.getQte() + " " + stockMed.getMedicament().getDateExpi() + "\n";
			}
		}
		
		return stockToPrint;
	}
	
	// servira pour lire chaque ligne du type A (approv), instancier et ajouter le Médicament au stock 
	public static void stockerMedicament(String lineALire, TreeMap<String, StockMed> stock) {

		String[] colApprov = lineALire.split("\\s+"); //split en colonnes. eg. line: Medicament1 120 2018-05-29
		String nomMedicamentRecu = colApprov[0];
		int qteMedicamentRecu = Integer.parseInt(colApprov[1]);
		Date dateExpiMedic = parseDate(colApprov[2]);   
		Medicament medRecu = new Medicament(nomMedicamentRecu, dateExpiMedic);
		
		// TODO gerer si on l'a deja pour en rajouter au stock
		StockMed stockMed = new StockMed(medRecu, qteMedicamentRecu);
		stock.put(nomMedicamentRecu, stockMed); //ajouter à notre stock existant
//		System.out.println(stock);

	}

// parser la liste de médicaments prescrits, instancier objets Prescriptions, commander si besoin
	public static String lirePrescriptions(String lineALire, Date dateCour, int idPrescription, TreeMap<String, StockMed> stock, TreeMap<String, Integer> commandes){
		
		String[] colPrescri = lineALire.split("\\s+|\t"); //split en colonnes. eg. line: Medicament1    5       6 
		String nomMedicament = colPrescri[0]; // eg. Medicament1
		int qteParCycle = Integer.parseInt(colPrescri[1]); // eg. 5
		int nbReps = Integer.parseInt(colPrescri[2]); // eg. 6
		int qteTotale = qteParCycle * nbReps; // 5x6 = 30
		Prescription prescription = new Prescription(dateCour, idPrescription, nomMedicament, qteParCycle, nbReps);

		
		String toPrint = "";
		String indicateurCommande = "OK";
		
		// si besoin commander, marquer le médicament dans la prescription comme commandé et l'ajouter à la liste de commandes  
		if (besoinCommander(dateCour, nomMedicament, qteTotale, stock)) { 
			prescription.setEnStock(false); // rappel: true par defaut 
			commandes.put(nomMedicament, qteTotale); 
			indicateurCommande = "COMMANDE";
		}
		
//		System.out.println(prescription);
		toPrint += nomMedicament + " " + qteParCycle + " " + nbReps + " " + indicateurCommande + "\n"; 
		
		return toPrint;
	}
	
	public static boolean besoinCommander(Date dateCour, String nomMedicament, int besoinQteTotale, TreeMap<String, StockMed> stock) {
		Date besoinJusquau = dateCour.dateApresXJours(besoinQteTotale);
		Boolean besoinCommander = false; 
		
		// si stock expire avant dateBesoin, marquer comme pas en stock et commander (ajouter à commandes)
		if (stock.containsKey(nomMedicament)) { // check stock existe
			if (stock.get(nomMedicament).getMedicament().getDateExpi().estAvant(besoinJusquau)) { //check date stock 
				besoinCommander = true;
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
