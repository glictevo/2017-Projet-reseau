import java.util.regex.*;
import java.util.Scanner;
import java.net.*;
import java.io.*;

public class Promoteur{

  static String ip;
  static String port;
  static Scanner sc = new Scanner(System.in);

  public static void main(String[] args){
    System.out.println("Sur quelle ip voulez-vous diffuser ?");
    ip = sc.nextLine();
    //Regex trouvée ici : https://stackoverflow.com/questions/5284147/validating-ipv4-addresses-with-regexp
    while(!ip.matches("^(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))$")){
      System.out.println("Votre ip n'est pas de la bonne forme, écrivez-la à nouveau");
      ip = sc.nextLine();
    }
    ip = Utils.completionIp(ip);

    System.out.println("Quel port voulez-vous utiliser ?");
    int port_int = -1;

    boolean parsablePort = false;
    while (!parsablePort) {
      String port_int_str = sc.nextLine();
      try{
        port_int = Integer.parseInt(port_int_str);
        if(port_int >= 9999 || port_int < 0) {
          parsablePort = false;
          System.out.println("Le port doit être un nombre entre 0 et 9999 (non compris)");
        } else {
          parsablePort = true;
        }
      } catch(NumberFormatException e){
        parsablePort = false;
      }
    }
    port = Utils.completerDe0Jusqua(String.valueOf(port_int), 4);

    System.out.println("Vous pouvez maintenant rentrer une commande (help pour la liste des commandes)");
    while(true){
      String commande = sc.nextLine();

      switch(commande){
        case "connectionServeur":
          connectionServeur();
          break;
        case "diffusion":
          diffusion();
          break;
        case "sondage":
          diffusionSondage();
          break;
        case "help":
          help();
          break;
        default:
          System.out.println("Je n'ai pas compris votre commande. Utilisez la commande 'help' pour obtenir la liste des commandes");
          break;
      }
    }
  }

  /**
   * Cette fonction permet au promoteur de demander une connexion à un Serveur, et d'envoyer un message publicitaire à ses clients
   */
  public static void connectionServeur(){
    System.out.println("Vous allez rentrer les informations concernant le serveur sur lequel vous voulez vous connecter.");
    System.out.println("Quel est le nom de la machine ?");
    String nomServeur = sc.nextLine();
    System.out.println("Quel est le port du serveur ?");

    int portServeur = -1;
    boolean parsablePort = false;
    while (!parsablePort) {
      String portServeur_str = sc.nextLine();
      try{
        portServeur = Integer.parseInt(portServeur_str);
        if(portServeur >= 9999 || portServeur < 0) {
          parsablePort = false;
          System.out.println("Le port doit être un nombre entre 0 et 9999 (non compris)");
        } else {
          parsablePort = true;
        }
      } catch(NumberFormatException e){
        parsablePort = false;
      }
    }
    //On se connecte au serveur
    try{
      Socket socket = new Socket(nomServeur, portServeur);

      //On initialise le reader et le writer
      DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
      DataInputStream reader = new DataInputStream(socket.getInputStream());

      writer.writeBytes("PUBL? " + ip + " " + port + " ");
      System.out.println("ip : |" + ip + "|");
      System.out.println("port : |" + port + "|");

      System.out.println("Rentrez le message de publicité que vous voulez envoyer aux clients du serveur");
      byte[] message = Utils.conversionUTF8(sc.nextLine());
      if(message.length > 200){
        System.out.println("Votre message ne peut pas faire plus de 200 octets, raccourcissez votre message");
        message = Utils.conversionUTF8(sc.nextLine());
      }

      writer.write(message);
      writer.writeBytes("+++");
      writer.flush();

      //On attend la réponse du serveur
      Utils.getType(reader);
      Utils.enlever3Plus(reader);
      System.out.println("Le publicité a été diffusée aux clients du serveur");

      socket.close();
    } catch (SocketException se){
      System.out.println("Problème de connexion avec le serveur.");
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
      return ;
    }
  }

  /**
   * Cette fonction permet de diffuser un message publicitaire à une adresse de diffusion
   */
  public static void diffusion(){
    try {
      DatagramSocket dso = new DatagramSocket();
      byte[] data = new byte[305];

      System.arraycopy(Utils.conversionUTF8("PROM "), 0, data, 0, 5);

      System.out.println("Entrez votre message publicitaire");
      byte[] message = Utils.conversionUTF8(sc.nextLine());
      if(message.length > 300){
        System.out.println("Votre message ne peut pas faire plus de 300 octets, raccourcissez votre message");
        message = Utils.conversionUTF8(sc.nextLine());
      }

      System.arraycopy(message, 0, data, 5, message.length);

      //On complète de #
      for(int i = 5 + message.length; i < 305; i++){
        data[i] = (byte) '#';
      }

      InetSocketAddress ia = new InetSocketAddress(Utils.getCleanIp(ip), Integer.parseInt(port));
      DatagramPacket paquet = new DatagramPacket(data, data.length, ia);
      dso.send(paquet);
    } catch(Exception e){
      System.out.println(e);
      e.printStackTrace();
      System.out.println("Fail de la diffusion");
    }
  }

  /**
   * Cette fonction gère la diffusion d'un sondage aux clients d'un serveur
   * On demande un port UDP libre sur lequel écouter les réponses du sondage
   */
  public static void diffusionSondage(){

      System.out.println("Vous allez rentrer les informations concernant le serveur sur lequel vous voulez vous connecter.");
      System.out.println("Quel est le nom de la machine ?");
      String nomServeur = sc.nextLine();
      System.out.println("Quel est le port du serveur ?");
      int portServeur = Integer.parseInt(sc.nextLine());

      try{
        Socket socket = new Socket(nomServeur, portServeur);

        //On initialise le reader et le writer
        DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
        DataInputStream reader = new DataInputStream(socket.getInputStream());

        System.out.println("Quel est le port UDP sur lequel vous voulez écouter ?");
        int portUDPParse = -1;
        String portUDP = "";
        boolean parsableUDP = false;
        while (!parsableUDP) {
          portUDP = sc.nextLine();
          try{
            portUDPParse = Integer.parseInt(portUDP);
            if(portUDPParse > 9998 || portUDPParse < 0) {
              parsableUDP = false;
              System.out.println("Veuillez écrire un nombre correct pour le port.");
            } else {
              parsableUDP = true;
            }
          } catch(NumberFormatException e){
            parsableUDP = false;
          }
        }

        System.out.println("Combien de possibilités de réponses contient votre sondage ? (Maximum 9)");
        boolean parsable = false;
        String nbQuestions = "";
        int nbQuestionsParsee = -1;
        while (!parsable) {
          nbQuestions = sc.nextLine();
          try{
            nbQuestionsParsee = Integer.parseInt(nbQuestions);
            if(nbQuestionsParsee > 9 || nbQuestionsParsee < 1) {
              parsable = false;
              System.out.println("Veuillez écrire un nombre correct de réponses.");
            } else {
              parsable = true;
            }
          } catch(NumberFormatException e){
            parsable = false;
          }
        }

        writer.writeBytes("SNDG? " + ip + " " + portUDP + " " + nbQuestions + " ");
        System.out.println("ip : |" + ip + "|");
        System.out.println("portUDP : |" + portUDP + "|");
        System.out.println("Nombre de questions : |" + nbQuestions + "|");

        System.out.println("Rentrez le sondage que vous voulez envoyer aux clients du serveur");
        byte[] sondage = Utils.conversionUTF8(sc.nextLine());
        if(sondage.length > 1000){
          System.out.println("Votre message ne peut pas faire plus de 1000 octets, raccourcissez votre message");
          sondage = Utils.conversionUTF8(sc.nextLine());
        }

        System.out.println("Combien de réponses de clients souhaitez-vous recevoir avant de fermer le sondage ?");
        boolean isNum = false;
        int nbRepoParsee = -1;
        while (!isNum) {
          String nbRepoMax = sc.nextLine();
          try{
            nbRepoParsee = Integer.parseInt(nbRepoMax);
            if(nbRepoParsee < 1) {
              System.out.println("Veuillez écrire un nombre correct de réponses.");
              isNum = false;
            } else {
              isNum = true;
            }
          } catch(NumberFormatException e){
            isNum = false;
          }
        }
        writer.write(sondage);
        writer.writeBytes("+++");
        writer.flush();


        //On attend la réponse du serveur
        String type = Utils.getType(reader);
        Utils.enlever3Plus(reader);
        if(type.equals("SNDG>")) {
          System.out.println("Le sondage a été diffusé aux clients du serveur");

          UDPPromoteurService serviceUDP = new UDPPromoteurService(portUDPParse, nbQuestionsParsee, nbRepoParsee);
          Thread threadSondage = new Thread(serviceUDP);
          threadSondage.start();

        } else if(type.equals("SNDG<")) {
          System.out.println("L'identifiant n'est pas correct, le sondage n'a pas été diffusé");
        } else {
          System.out.println("La réponse du serveur est non identifiée.");
        }

        socket.close();
      } catch (Exception e){
        System.out.println(e);
        e.printStackTrace();
        return ;
      }
    }

  /**
   * Affiche les commandes offertes aux promoteurs
   */
  public static void help(){
    System.out.println("");
    System.out.println("--------------------");

    System.out.println("connectionServeur : se connecter à un serveur pour envoyer une pub");
    System.out.println("diffusion : diffuser un message");
    System.out.println("sondage : diffuser un sondage");

    System.out.println("--------------------");
    System.out.println("");
  }
}
