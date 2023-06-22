package tp2ift2015;

import java.util.TreeMap;
import java.util.TreeSet;
/***
 * IFT2015, TP2 : : Gestion des stocks d’une pharmacie
 * Eté 2023
 * 
 * @param args[0] représente le fichier d'entrée (par exemple "exemple5.txt"), 
 * et args[1] représente le nom désiré pour le fichier de sortie créé (par ex. "exemple5+.txt")  
 * @author Deanna WUNG, Océane HAYS
 *
 */
public class Tp2 {

	public static void main(String[] args) {
		//definir nos args
//		args[0] = "exemple5.txt";
//		args[1] = "blabla.txt";

		// nos données (liste de commandes et liste des médicaments en stock) :  
		TreeMap<String, Integer> commandes = new TreeMap<>();  // O(1)
		TreeMap<String, TreeMap<Medicament, Integer>> stockMedicaments = new TreeMap<String, TreeMap<Medicament, Integer>>();

		// construire le path pour chercher le nom du fichier desiré
		String path = System.getProperty("user.dir"); // O(1)
		path += "/" + args[0];
//        System.out.println(path);

		// lire fichier 
//		path = "C:\\Users\\Deanna\\Documents\\UdeM\\IFT2015 structures de donnees\\Devoir2\\tests\\" + args0;
		String toPrint = Utils.lireFichier(path, stockMedicaments, commandes); // O(N)

		// creer fichier de sortie
		Utils.creerFichierFinal(args[1], toPrint); //O(1)

	}

}
