import java.util.regex.*;
import java.util.Scanner;
import java.nio.charset.*;
import java.net.*;
import java.io.*;
import java.nio.*;

public class Client{

  //informations du client
  static String pseudo;
  static byte[] motDePasse = new byte[2];

  static String portUDP;
  static Socket socket;

  static Scanner sc = new Scanner(System.in);

  static DataOutputStream writer;
  static DataInputStream reader;


  /**
   *
   * @param password
   */
  public static void traitementMotDePasse(int password){
    byte[] mdp = ByteBuffer.allocate(4).putInt(password).order(ByteOrder.LITTLE_ENDIAN).array();
    motDePasse[0] = mdp[2];
    motDePasse[1] = mdp[3];
  }


  public static void main(String[] args){

    //On demande les infos sur le serveur
    System.out.println("Vous allez rentrer les informations concernant le serveur sur lequel vous voulez vous connecter.");
    System.out.println("Quel est le nom de la machine ?");
    String nomServeur = sc.nextLine();
    int portServeur = -1;
    while(portServeur < 0 || portServeur >= 9999){
      System.out.println("Quel est le port du serveur ?");
      try {
        portServeur = Integer.parseInt(sc.nextLine());
      } catch (Exception e){
        System.out.println("Le port doit être un nombre entre 0 et 9999 (non compris)");
      }
    }

    //On demande des infos sur le client
    System.out.println("Etes-vous déjà inscrit sur ce serveur ? (o/n)");
    String inscrit = sc.nextLine();
    while(!inscrit.equals("o") && !inscrit.equals("n")){
      System.out.println("Veuillez écrire 'o' pour oui ou 'n' pour non");
      inscrit = sc.nextLine();
    }

    System.out.println("Quel est votre pseudo ?");
    pseudo = sc.nextLine();
    while(!pseudo.matches("[a-zA-Z0-9]{8}")){
      System.out.println("Votre pseudo doit faire 8 caractères de long, avec seulement des lettres ou des chiffres");
      pseudo = sc.nextLine();
    }
    int password = -1;
    while(password < 0 || password > 65535){
      System.out.println("Quel est votre mot de passe ? (doit être un nombre entre 0 et 65535 compris.)");
      try {
        password = Integer.parseInt(sc.nextLine());
      } catch (Exception e){
        System.out.println("Votre mot de passe doit être un nombre entre 0 et 65535 compris.");
      }
    }
    traitementMotDePasse(password);

    //NOTE : je ne sais pas si on laissera le client choisir au final, on verra
    int portUDPtmp = -1;
    while(portUDPtmp < 0 || portUDPtmp >= 9999){
      System.out.println("Sur quel port voulez-vous écouter en UDP ?");
      try{
        portUDPtmp = Integer.parseInt(sc.nextLine());
      } catch (Exception e){
        System.out.println("Le port doit être un nombre compris entre 0 et 9999 (non compris)");
      }
    }
    portUDP = Utils.completerDe0Jusqua(String.valueOf(portUDPtmp), 4);

    //On se connecte au serveur
    try{
      socket = new Socket(nomServeur, portServeur);

      //On initialise le reader et le writer
      writer = new DataOutputStream(socket.getOutputStream());
      reader = new DataInputStream(socket.getInputStream());

      //On se connecte au serveur selon si on est inscrit ou non
      if(inscrit.equals("o")){
        if(!connection()){
          System.out.println("La connexion n'a pas pu se faire.");
          socket.close();
          return ;
        }
      } else {
        if(!inscription()){
          System.out.println("L'inscription n'a pas pu se faire.");
          socket.close();
          return ;
        }
      }

      //On lance le thread pour recevoir les paquets en UDP
      try {
        UDPClientService service = new UDPClientService(Integer.parseInt(portUDP));
        Thread t = new Thread(service);
        t.start();
      } catch (Exception e){
        System.out.println(e);
        e.printStackTrace();
        return ;
      }

      //On est maintenant connecté. On lit les commandes du client
      while(true){
        System.out.print("$ ");
        String commande = sc.nextLine();
        boolean flag = true;

        switch(commande){
          case "demandeAmi":
            try {
              flag = demandeAmitie();
            } catch (Exception e) {
              System.out.println("Une erreur est survenue lors de votre demande d'amitié.");
            }
            break;
          case "message":
            try {
              flag = envoieMessage();
            } catch (Exception e) {
              System.out.println("Une erreur est survenue lors de l'envoi de votre message.");
            }
            break;
          case "inondation":
            try {
              flag = inondation();
            } catch (Exception e) {
              System.out.println("Une erreur est survenue dans votre tentative d'inondation.");
            }
            break;
          case "liste":
            try {
              flag = listeClients();
            } catch (Exception e) {
              System.out.println("Une erreur est survenue lors de la consultation de la liste des utilisateurs.");
            }
            break;
          case "consultation":
            try {
              flag = consultation();
            } catch (Exception e) {
              System.out.println("Une erreur est survenue lors de la consultation des messages.");
            }
            break;
          case "deco":
            try {
              flag = deconnexion();
            } catch (Exception e) {
              System.out.println("Une erreur est survenue lors de la déconnexion.");
            }
            break;
          case "help":
            help();
            break;
          default:
            System.out.println("Je n'ai pas compris votre commande. Utilisez la commande 'help' pour obtenir la liste des commandes");
            break;
        }

        if(!flag){
          socket.close();
          return ;
        }
      }
    } catch (SocketException se){
      System.out.println("La connexion avec le serveur a été interrompue.");
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }

  /**
   * Gère l'inscription d'un client au serveur
   * @return true si l'inscription est réussie, false sinon
   */
  public static boolean inscription() throws Exception{
    //On écrit la requête
    writer.writeBytes("REGIS ");
    byte[] id = Utils.conversionUTF8(pseudo + " ");
    writer.write(id, 0, id.length);
    writer.writeBytes(portUDP);
    writer.writeBytes(" ");
    writer.write(motDePasse[0]);
    writer.write(motDePasse[1]);
    //writer.write(motDePasse, 0, 2);
    writer.writeBytes("+++");
    writer.flush();

    //On lit la réponse
    String type = Utils.getType(reader);
    Utils.enlever3Plus(reader);

    if(type.equals("GOBYE")){
      return false;
    }
    System.out.println("Vous avez bien été inscrit sur le serveur");

    return true;
  }

  /**
   * Connecte un client au serveur. Cette fonction est appelée lorsqu'on précise avoir déjà été inscrit sur le serveur
   * @return true si la connexion a pu s'opérer, false si une exception a été catchée
   */
  public static boolean connection() throws Exception{
    //On envoie la requête
    writer.writeBytes("CONNE ");
    byte[] id = Utils.conversionUTF8(pseudo + " ");
    writer.write(id, 0, id.length);
    writer.write(motDePasse, 0, 2);
    writer.writeBytes("+++");
    writer.flush();

    //On lit la réponse
    String type = Utils.getType(reader);
    Utils.enlever3Plus(reader);

    if(type.equals("GOBYE")){
      return false;
    }
    System.out.println("Vous êtes maintenant connecté au serveur.");

    return true;
  }

  /**
   * Permet de demander un utilisateur connecté en ami, l'utilisateur est à spécifier dans l'execution de la fonction
   * @return true si la demande est transmise, false si une exception est catchée
   */
  public static boolean demandeAmitie() throws Exception{
    //On envoie la requête
    writer.writeBytes("FRIE? ");

    System.out.println("Rentrez le pseudo de la personne à qui vous voulez faire une demande d'amitié");
    String id_s = sc.nextLine();
    while(!id_s.matches("[a-zA-Z0-9]{8}")){
      System.out.println("Le pseudo doit faire 8 caractères de long, avec seulement des lettres ou des chiffres");
      id_s = sc.nextLine();
    }
    byte[] id = Utils.conversionUTF8(id_s);
    writer.write(id, 0, id.length);
    writer.writeBytes("+++");
    writer.flush();

    //On lit la réponse
    String type = Utils.getType(reader);
    Utils.enlever3Plus(reader);

    if(type.equals("FRIE>")){
      System.out.println("La demande d'amitié a été transmise");
    } else {
      System.out.println("Le pseudo est inconnu du serveur ou vous avez rentré votre propre pseudo");
    }

    return true;
  }

  /**
   * Cette fonction permet d'envoyer un message à un utilisateur, précisé dans l'execution de la fonction
   * @return true si le message est envoyé, false si une exception est catchée
   */
  public static boolean envoieMessage() throws Exception{
    //On envoie la requête pour donner le nombre d'envois
    writer.writeBytes("MESS? ");
    System.out.println("Rentrez le pseudo de la personne à qui vous voulez envoyer un message");
    String id_str = sc.nextLine();

    //verifie la validité du pseudo
    while(!id_str.matches("[a-zA-Z0-9]{8}")){
      System.out.println("Ce pseudo est invalide. Veuillez saisir un pseudo de 8 caractere de long.");
      id_str = sc.nextLine();
    }
    byte[] id = Utils.conversionUTF8(id_str);
    writer.write(id, 0, id.length);

    writer.writeBytes(" ");

    System.out.println("Rentrez le message que vous voulez lui envoyer");
    byte[] message = Utils.conversionUTF8(sc.nextLine());

    int nombreDeMessages = message.length / 200 + (message.length % 200 == 0 ? 0 : 1);
    String nbMessages = Utils.completerDe0Jusqua(String.valueOf(nombreDeMessages), 4);
    writer.writeBytes(nbMessages + "+++");
    writer.flush();

    System.out.println("(" + nombreDeMessages + " sous-messages vont être envoyés au serveur)");

    //On envoie les messages
    for(int i = 0; i < nombreDeMessages; i++){
      String numMessage = Utils.completerDe0Jusqua(String.valueOf(i), 4);
      writer.writeBytes("MENUM " + numMessage + " ");
      byte[] mess = new byte[(i == nombreDeMessages - 1 ? message.length%200 : 200)];
      System.arraycopy(message, i*200, mess, 0, (i == nombreDeMessages - 1 ? message.length%200 : 200));
      System.out.println("--------------" + new String(mess));
      writer.write(mess);
      writer.writeBytes("+++");
    }
    writer.flush();

    //On récupère la réponse
    String type = Utils.getType(reader);
    Utils.enlever3Plus(reader);
    if(type.equals("MESS>")){
      System.out.println("Votre message a bien été envoyé.");
    } else {
      System.out.println("Votre message n'a pas pu être envoyé.");
    }

    return true;
  }

  /**
   * Cette fonction vous permet d'envoyer un message à tous vos amis, et aux amis de vos amis, et ainsi de suite
   * @return true si le flood a été envoyé, false sinon
   */
  public static boolean inondation() throws Exception{
    //On envoie la requête
    System.out.println("Rentrez le message que vous voulez envoyer : ");
    byte[] message = Utils.conversionUTF8(sc.nextLine());
    if(message.length > 200){
      System.out.println("Un message d'inondation ne peut pas faire plus de 200 octets, raccourcissez votre message");
      return true;
    }

    writer.writeBytes("FLOO? ");
    writer.write(message);
    writer.writeBytes("+++");
    writer.flush();

    //On lit la réponse
    String type = Utils.getType(reader);
    Utils.enlever3Plus(reader);
    if(type.equals("FLOO>")){
      System.out.println("Votre message d'inondation a été envoyé.");
    } else {
      System.out.println("Votre message d'inondation n'a pas pu être envoyé");
    }

    return true;
  }

  /**
   * Cette fonction vous affiche la liste des clients connectés dans le serveur
   * @return true si l'affichage se fait avec succès, false sinon
   */
  public static boolean listeClients() throws Exception{
    //On envoie la requête
    writer.writeBytes("LIST?+++");
    writer.flush();

    //On lit la réponse
    Utils.getType(reader);
    reader.readByte();
    int nombreDeMessages = Utils.getNumNoctets(reader, 3);
    Utils.enlever3Plus(reader);

    //On affiche le nom des clients
    for(int i = 0; i < nombreDeMessages; i++){
      Utils.getType(reader);
      reader.readByte();
      System.out.println(i + " : " + Utils.getId(reader));
      Utils.enlever3Plus(reader);
    }

    return true;
  }

  /**
   * Cette fonction permet d'afficher les notifications de l'utilisateur. Elles ont un code associé.
   * Il s'agit ici de la fonction générale de consultation d'une notification.
   * Cette fonction appelle ensuite des sous-fonctions relatives à chaque type de message reçu.
   * @return true si la notification est affichée, false si le type de message n'est pas connu ou que l'affichage échoue.
   */
  public static boolean consultation() throws Exception{
    //On envoie la requête
    writer.writeBytes("CONSU+++");
    writer.flush();

    System.out.println("--------------------");

    //On lit la réponse
    String type = Utils.getType(reader);
    switch(type){
      case "SSEM>" :
        return consultationMessage();
      case "OOLF>" :
        return consultationInondation();
      case "EIRF>" :
        return consultationDemandeAmitie();
      case "FRIEN" :
        return consultationAcceptationAmitie();
      case "NOFRI" :
        return consultationRefusAmitie();
      case "LBUP>" :
        return consultationPublicite();
      case "SNDGE" :
        return consultationSondage();
      case "NOCON" :
        Utils.enlever3Plus(reader);
        System.out.println("Il n'y a pas de notifications.");
        return true;
      default:
        System.out.println("On a reçu un message inconnu");
        return false;
    }
  }

  /**
   * Il s'agit de la fonction de consultation des messages envoyés par les autres clients
   */
  public static boolean consultationMessage() throws Exception{
    reader.readByte();
    String pseudo = Utils.getId(reader);
    reader.readByte();
    int nombreDeMessages = Utils.getNumNoctets(reader, 4);
    Utils.enlever3Plus(reader);

    System.out.println("Message de : " + pseudo + "\n\n" + Utils.getMessage3(reader, nombreDeMessages));

    return true;
  }

  /**
   * Il s'agit de la fonction de consultation du flood envoyé par les autres clients
   */
  public static boolean consultationInondation() throws Exception{
    reader.readByte();
    String pseudo = Utils.getId(reader);
    reader.readByte();
    String message = Utils.getMessage2(reader);

    System.out.println("Message de : " + pseudo + "\n\n" + message);

    return true;
  }

  /**
   * Il s'agit de la fonction de consultation de la demande d'amitié d'un autre utilisateur inscrit au serveur.
   */
  public static boolean consultationDemandeAmitie() throws Exception{
    reader.readByte();
    String pseudo = Utils.getId(reader);
    Utils.enlever3Plus(reader);

    System.out.println(pseudo + " souhaite vous ajouter à ses amis, acceptez-vous ? (o/n)");
    String accepter = sc.nextLine();
    while(!accepter.equals("o") && !accepter.equals("n")){
      System.out.println("Veuillez écrire 'o' pour oui ou 'n' pour non");
      accepter = sc.nextLine();
    }

    if(accepter.equals("o")){
      writer.writeBytes("OKIRF+++");
    } else {
      writer.writeBytes("NOKRF+++");
    }
    writer.flush();

    String type = Utils.getType(reader);
    Utils.enlever3Plus(reader);

    if(accepter.equals("o")){
      System.out.println("Vous êtes maintenant ami avec " + pseudo);
    } else {
      System.out.println("Vous avez refusé la demande d'amitié de " + pseudo);
    }

    return true;
  }

  /**
   * Il s'agit de la fonction de consultation de la notification affichant l'acceptation d'une demande d'ajout en ami.
   */
  public static boolean consultationAcceptationAmitie() throws Exception{
    reader.readByte();
    String pseudo = Utils.getId(reader);
    Utils.enlever3Plus(reader);

    System.out.println(pseudo + " a accepté votre demande d'amitié");

    return true;
  }

  /**
   * Il s'agit de la fonction de consultation de la notification affichant le refus d'une demande d'ajout en ami.
   */
  public static boolean consultationRefusAmitie() throws Exception{
    reader.readByte();
    String pseudo = Utils.getId(reader);
    Utils.enlever3Plus(reader);

    System.out.println(pseudo + " a refusé votre demande d'amitité");

    return true;
  }

  /**
   * Il s'agit de la fonction de consultation des publicités envoyées par les promoteurs
   */
  public static boolean consultationPublicite() throws Exception{
    reader.readByte();
    String ip = Utils.getIp(reader);
    System.out.println("ip : " + ip);
    reader.readByte();
    int port = Utils.getNumNoctets(reader, 4);

    reader.readByte();
    String message = Utils.getMessage2(reader);

    System.out.println("Publicité : " + message);
    System.out.println("Souhaitez-vous vous abonner à ce promoteur ? (o/n)");
    String reponse = sc.nextLine();
    while(!reponse.equals("o") && !reponse.equals("n")){
      System.out.println("Veuillez écrire 'o' pour oui ou 'n' pour non");
      reponse = sc.nextLine();
    }

    if(reponse.equals("o")){
      Thread t = new Thread(new UDPPubliciteClientService(Utils.getCleanIp(ip), port));
      t.start();
    }

    return true;
  }

  /**
   * Il s'agit de la fonction de consultation des sondages envoyés par les promoteurs
   */
  public static boolean consultationSondage() throws Exception{
    reader.readByte();
    String ip = Utils.getIp(reader);
    System.out.println("IP : " + ip);
    reader.readByte();
    int port = Utils.getNumNoctets(reader, 4);
    System.out.println("PORT : " + port);
    reader.readByte();
    reader.readByte();
    int nbQuestions = Utils.getNumNoctets(reader, 1);
    System.out.println("QUESTIONS : " + nbQuestions);
    reader.readByte();
    String sondage = Utils.getMessage2(reader);
    System.out.println("Sondage : " + sondage + " \n ");

    System.out.println("Votre réponse :");
    boolean parsable = false;
    String reponse = "";
    while(!parsable) {
      reponse = sc.nextLine();
      try{
        int reponseParsee = Integer.parseInt(reponse);
        if(reponseParsee < 1 || reponseParsee > nbQuestions) {
          parsable = false;
          System.out.println("Veuillez écrire le numéro correct de votre réponse.");
        } else {
          parsable = true;
        }
      }catch(NumberFormatException e){
        parsable = false;
        System.out.println("Veuillez écrire le numéro de votre réponse, pas de lettres.");
      }
    }

    DatagramSocket dso = new DatagramSocket();
    byte[]data = new byte [4];
    reponse = Utils.completerDe0Jusqua(reponse, 4);
    data = reponse.getBytes();
    InetSocketAddress ia = new InetSocketAddress(Utils.getCleanIp(ip), Integer.parseInt(Integer.toString(port)));
    DatagramPacket paquet = new DatagramPacket(data, data.length, ia);
    dso.send(paquet);

    return true;
  }

  /**
   * Cette fonction permet, à l'aide de la commande deco, de se déconnecter proprement du serveur
   * @return false, dans les deux cas, pour permettre l'arrêt de la socket
   */
  public static boolean deconnexion() throws Exception{
    writer.writeBytes("IQUIT+++");
    writer.flush();

    //On "lit" la réponse du serveur
    Utils.getType(reader);
    Utils.enlever3Plus(reader);

    System.out.println("Vous êtes maintenant deconnecté");

    return false;
  }

  /**
   * Suite à la commande help du client, ce dernier peut, grâce à cette fonction, afficher les commandes qui lui sont proposées.
   */
  public static void help(){
    System.out.println("");
    System.out.println("--------------------");

    //Affiche la liste des commandes
    System.out.println("message : envoyer un message à un ami");
    System.out.println("demandeAmi : demander en ami quelqu'un");
    System.out.println("liste : donne la liste des clients inscrits sur le serveur");
    System.out.println("consultation : consulter une notification");
    System.out.println("deconnexion : se déconnecter du serveur");
    System.out.println("inondation : envoyer un message à tous ses amis");

    System.out.println("--------------------");
    System.out.println("");
  }
}
