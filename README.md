# Projet de réseau 3ème année de licence informatique - Paris 7 Diderot

Réalisé par : Timon De Morel, Perrine Pullicino, Guillaume Lictevout

## Obligatoire : Serveur

Pour lancer le Serveur, vous devez entrer

    java Serveur XXXX YYYY

où XXXX est le port TCP des clients du serveur et YYYY est le port TCP des promoteurs du serveur.

Par exemple,

    java Serveur 5004 5005

## Obligatoire : Client

Le client a accès à une liste de commandes étoffée :

- **demandeAmi** : Utilisez cette commande pour demander un utilisateur (à spécifier) en ami.
- **message** : Utilisiez cette commande pour envoyer un message à un utilisateur. On vous demandera de rentrer le nom de l'utilisateur et le message à envoyer.
- **inondation** : Vous devrez préciser un message à envoyer, et cette fonction l'enverra à tous vos amis, puis aux amis de vos amis et ainsi de suite.
- **liste** : Il s'agit de la commande qui demandera au serveur la liste des clients.
- **consultation** : Vous devrez appeler cette commande lorsque vous verrez que vous avez reçu une notification (ou que vous voulez vous assurer que vous n'en avez pas). Elle vous affichera le dernier message reçu, puis remontera dans les messages non lus.
- **deco** : Lance la fonction de deconnexion du serveur.
- **help** : Affiche la liste des commandes.

## Obligatoire : Promoteur

Un promoteur est donc un publicitaire. Il communique avec le serveur.

Tapez

    java Promoteur

pour créer un promoteur.

On vous demandera sur quelle adresse IP vous souhaitez diffuser. Rentrez une adresse IP de classe D (pour la multi-diffusion)

On vous demande ensuite quel port vous souhaitez utiliser. Tant que ce port n'est pas déjà utilisé, vous pouvez choisir celui qui vous plaira.

La commande

    help

vous laisse voir ce qu'un promoteur peut faire.

Sa fonction principale est d'envoyer des publicités, il possède donc une commande

    connectionServeur

vous demande d'entrer le nom de la machine, et le port du serveur (le deuxieme argument de serveur, c'est à dire **le port TCP des promoteurs du serveur **) diffuse un message de publicité aux clients.

**Vous pouvez ensuite envoyer un message aux clients du serveur dans la foulée.**


La commande

    diffusion

permet de diffuser aux **abonnés** une publicité à fournir, sur une **ip de diffusion**.


## Extension : Sondages

Nous vous proposons, si vous êtes un promoteur, d'envoyer des sondages aux clients connectés. Ces sondages prennent la forme d'une question posée par vos soins, et des réponses que vous proposez à la question. Les clients sont tenus de répondre à la question.

Le but est d'obtenir une vue d'ensemble de l'opinion sur un sujet des clients connectés. Il ne s'agit pas de cibler un client en particulier pour sa réponse. Vous obtenez les résultats de votre sondages sous la forme :

    La réponse 1 a été choisie par X personnes

    La réponse 2 a été choisie par Y personnes

et ainsi de suite.

### Comment utiliser les sondages ?

##### Du coté du Promoteur
- (Préciser l'adresse ip à laquelle on veut envoyer les résultats du sondage (ici, localhost, donc **127.0.0.1 **en IPv4) lors de l'inscription en tant que promoteur)

- Lancer un sondage via le promoteur : "sondage"

- Quel est le nom de la machine ? Localhost, par exemple.

- Quel est le port du serveur ? Le deuxieme port entré pour le serveur. Dans notre exemple 5005

- Quel est le port UDP sur lequel on veut écouter -> Le port du *sondage*

- Préciser le nombre de possibilités que contient votre sondage : Si votre sondage est par exemple : **"Quel est votre film préféré : 1) Casablanca, 2) Avengers, 3) Tintin au Pérou ?"**, vous devez rentrer 3.

- "Combien de réponses de clients souhaitez-vous recevoir avant de fermer le sondage ?" vous demande de préciser combien d'utilisateurs peuvent entrer leur réponse avant de fermer le sondage.

- On a donc diffusé le sondage aux clients connectés. On attend les réponses. Une fois les réponses obtenues, la socket est fermée et le port UDP de l'étape 4 peut être réutilisé par d'autres sondages.

##### Du coté du Client

- Vous recevez une notification (6) vous indiquant que vous avec un sondage en attente.
- En allant le visionner, on vous demande de rentrer votre réponse. Votre réponse doit être un nombre figurant dans la liste.
- Vous pouvez ensuite retourner à vos activités.

#### Améliorations des sondages :

1) Si on peut savoir combien d'utilisateurs sont en ligne en ce moment, on peut vérifier que le nombre rentré dans l'étape 6) est inférieur ou égal.

2) On peut peut-être proposer aux clients de ne pas répondre s'ils n'en ont pas envie. Mais c'est moins drôle.
