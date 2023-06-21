package tp2ift2015;

import java.util.TreeMap;
import java.util.TreeSet;

public class Tp2 {

	public static void main(String[] args) {
		
		// en mémoire universelle : (pas objets) 
		TreeMap<String, Integer> commandes = new TreeMap<>(); // liste de commandes courante. 
		TreeMap<String, TreeMap<Medicament, Integer>> stockMedicaments = new TreeMap<String, TreeMap<Medicament, Integer>>();
		
		// lire fichier 
		// (garder en memoire format fichier de sortie)
		String path = "C:\\Users\\Deanna\\Documents\\UdeM\\IFT2015 structures de donnees\\Devoir2\\tests\\";
		String nomFichier = "exemple5.txt";
		
		String toPrint = Utils.lireFichier(path+nomFichier, stockMedicaments, commandes);
		
		System.out.println(commandes);
		
		System.out.println(toPrint);
		


		

	}

}
