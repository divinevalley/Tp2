### Autoévaluation 

Le programme fonctionne correctement.

### Analyse de la complexité temporelle en notation grand O

k - indique nombre d'items sur la liste de commande;
n - indique nombre de types de médicaments différents;
m - indique nombre d'items sur la prescription
p - indique nombre (maximal) de médicaments partageant le même type
q - représente le nombre de médicaments distincts en stock (donc q <= n * p)
(voir commentaires dans le code pour plus de détails dans l'analyse)

#### DATE

* afficherCommandes() : Complexité de O(k) car on parcourt la liste de commandes

* jeterPerimesEtAfficher() : Complexité : O(q) car on parcourt la totalité des médicaments qu'ils soient du même type ou pas

* Complexité globale pour DATE : O(k + q)

#### PRESCRIPTION

* lirePrescriptions() : Complexité de O(n + log(k)) pour une seule ligne (un médicament) lu par le programme, car besoin de parcourir le stock (n éléments) et retrouver l'élément dans la liste de commandes (k éléments). 

Complexité globale : Avec m médicaments par prescription, on a une complexité globale pour PRESCRIPTION : O(m*(log(n))), qui se simplifie de O(m*(p +log(k) + log(n)))

#### APPROV

* stockerMedicament() : Complexité de O(log(n)) pour une seule ligne. 
Complexité globale : O(q * log(n)), car pour chaque ligne q de APPROV, on a besoin de rechercher et/ou placer dans une liste de n éléments répertoriés en stock (TreeMap). 