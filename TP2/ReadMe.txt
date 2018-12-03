Etapes pour exécuter le TP:

Note: 
* 
Il faut toujours exécuter le service de noms en premier. Ainsi si le serveur de noms est tué il faut tué tout les autres serveurs
lancez le service de noms puis relances les serveurs de calcul.
*
I. En mode sécurisé

1- Lancez la commande « ant clean » suivi de « ant ».
2- Modifier le fichier config/config_calculServer avec l'adresse IP ou vous voulez exécuter votre service de noms et le port souhaité.
3- Modifier le fichier config/config_nameService avec l'adresse IP ou vous voulez executer votre service de noms.

Optionnel:
	- Si vous voulez modifier le username et le password du répartiteur modifier le fichier config/config_repartiteur

4- Lancez en premier le serveur d'authentification avec la commande: ./nameService.sh 
5- Lancez ensuite le nombre de serveur de calculs désirés avec la commande:
./calculServer.sh [capacité] ex(./calculServer.sh 4)
6- Lancez ensuite le répartiteur en mode sécurisé avec ./repartiteur.sh [filePath]

II. En mode non sécurisé:

même étapes 1,2,3 et 4
5- Lancez ensuite le nombre de serveur de calculs désirés avec la commande:
./calculServer.sh [capacité] [malice] 
Exemple: pour un serveur avec capacité de 5 et malice a 75% : ./calculServer.sh 5 75
6- Lancez ensuite le repartiteur en mode non sécurisé avec ./repartiteur.sh [filePath]

Pour vérifier la gestion des pannes:

Exécuter un des deux modes du répartiteur avec le fichier operations-x qui contient 2500 opérations et qui prendra plus de
temps d'exécution.
Arrêtez un serveur avant la fin de l'exécution du répartiteur.
Quand vous arrêtez un serveur de calcul, il n'est pas nécessaire de relancer le service de noms, le répartiteur dira au service
de noms de ne plus considérer ce server.

Disclaimer: Il n'était pas demandé de traiter les pannes de serveur en mode idle ainsi si vous arrêter un serveur en dehors de
l'exécution du répartiteur le service de noms ne sera pas mis a jour et cela causera des exceptions.