Etapes pour exécuter le TP:

Note: 
* Les fichiers du client vont être dans le dossier Client_Files et ceux du serveur dans Server_Files
** Si vous exécutez la commande lock alors que le client ne dispose pas du fichier en local (dans Client_Files) une exception sera levée, alors avant d’appeler lock exécuter la commande ./client.sh get [nomDuFichier]
*** Cette implémentation connait des limites:
	- Si on redémarre le serveur d’authentification, il faut supprimer le fichier credentials.txt chez le client parce que l’attribut users qui store les utilisateurs a été réinitialisé.
	- Même chose pour le serveur de fichiers avec les fichiers verrouillés.

I. Avec un seul client

1- Lancez la commande « ant clean » suivi de « ant » ensuite sur votre terminal rendez vous au fichier bin créé et exécuter la commande: rmiregistry &.
2- Lancez en premier le serveur d'authentification avec la commande: ./authservice.sh 
3- Lancez ensuite le serveur de fichier avec la commande: ./server.sh
4- Pour commencer il faut s'authentifier auprès du serveur d'authentification, pour cela:
	./client.sh [username] [password] (ex: ./client.sh user1 pass1)
5- A partir de la on peut exécuter toutes les fonctions du serveur:
	5.1- Créer un fichier: ./client.sh create monfichier
	5.2- Verrouiller un fichier: ./client.sh lock monfichier
	5.3- Récupérer un fichier du serveur: ./client.sh get monfichier
	5.4- apporter les modifications local sur le serveur: ./client.sh push monfichier
	5.5- lister tout les fichiers sur le serveur: ./client.sh list
	5.6- synchroniser notre dossier local avec le serveur: ./client.sh syncLocalDirectory

II. Avec 2 clients:

Procédé exactement de la même manière pour lancer le premier client. Pour le deuxième client rendez vous dans le dossier « client 2 » lancez les commandes « ant clean » et « ant » puis authentifier vous a nouveau comme pour le client 1.