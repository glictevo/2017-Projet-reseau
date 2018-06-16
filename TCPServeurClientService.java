import java.util.ArrayList;
import java.net.*;
import java.io.*;
import java.nio.*;

public class TCPServeurClientService implements Runnable{


  Socket socket;

  DataOutputStream writer;
  DataInputStream reader;

  //les infos relatives au serveur
  ServeurInfos infos;

  //le client étudié
  ClientServeur clientActuel;

  /**
   * Constructeur de TCPServeurClientService
   * @param socket
   * @param infos
   */
  public TCPServeurClientService(Socket socket, ServeurInfos infos){
    this.socket = socket;
    this.infos = infos;
  }

  public void run(){
    System.out.println("Je lance un thread de communication avec un client.");
    try {
      writer = new DataOutputStream(socket.getOutputStream());
      reader = new DataInputStream(socket.getInputStream());

      while(true){
        String type = Utils.getType(reader);
        boolean flag = true;

        //Attention, peut-être qu'il faudra des arguments pour les fonctions
        System.out.println("--------------------");
        switch(type){
          case "REGIS":
            flag = inscription();
            break;
          case "CONNE":
            flag = connection();
            break;
          case "FRIE?":
            flag = demandeAmitie();
            break;
          case "MESS?":
            flag = envoieMessage();
            break;
          case "FLOO?":
            flag = inondation();
            break;
          case "LIST?":
            flag = listeClients();
            break;
          case "CONSU":
            flag = consultation();
            break;
          case "IQUIT":
            flag = deconnexion();
            break;
          default:
            System.out.println("On a reçu une commande inconnue du client");
            System.out.println("/" + type + "/");
            socket.close();
            return ;
        }

        if (!flag){
          socket.close();
          return ;
        }
      }

      //System.out.println("je close la connexion");
      //socket.close();
    } catch(EOFException eofe){
      System.out.println("La connexion avec le client " + clientActuel.nom + " a été interrompue.");
    } catch(SocketException se){
      System.out.println("La connexion avec le client " + clientActuel.nom + " a été interrompue.");
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }

  /**
   * Cette fonction gère l'inscription d'un client auprès du serveur.
   * On teste les informations fournies par le client dans cette fonction.
   * @return false, si une erreur (pseudo déjà existant par exemple) est détectée, true sinon
   */
  public boolean inscription(){
    try {
      reader.readByte();
      String pseudo = Utils.getId(reader);
      reader.readByte();
      int portUDP = Utils.getNumNoctets(reader, 4);
      reader.readByte();
      int motDePasse = Utils.getMotDePasse(reader);
      Utils.enlever3Plus(reader);

      System.out.println("Tentative d'inscription de " + pseudo + " sur le port " + portUDP + " et le mot de passe : " + motDePasse);

      //On vérifie maintenant que le pseudo du client n'est pas déjà dans la liste des clients
      if(infos.clients.size() >= 100){
        System.out.println("Inscription refusée : il y a déjà 100 clients d'inscrits sur ce serveur.");
        writer.writeBytes("GOBYE+++");
        writer.flush();
        return false;
      }

      for(ClientServeur client : infos.clients){
        if(client.nom.equals(pseudo)){
          System.out.println("Inscription refusée : ce pseudo existe déjà");
          writer.writeBytes("GOBYE+++");
          writer.flush();
          return false;
        }
      }

      //On ajoute maintenant le client à la liste des clients.
      ClientServeur client = new ClientServeur(pseudo, motDePasse, portUDP, socket.getInetAddress());
      infos.clients.add(client);
      System.out.println("Inscription acceptée.");
      writer.writeBytes("WELCO+++");
      writer.flush();

      //On update clientActuel
      clientActuel = client;

      return true;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Cette fonction gère la reconnexion d'un client auprès du serveur
   * @return true si la reconnexion est réussie, false sinon
   */
  public boolean connection(){
    try{
      reader.readByte();
      String pseudo = Utils.getId(reader);
      reader.readByte();
      int motDePasse = Utils.getMotDePasse(reader);
      Utils.enlever3Plus(reader);

      System.out.println("Tentative de connexion de " + pseudo + " avec le mot de passe : " + motDePasse);

      //On vérifie que le client existe et qu'il a le bon mot de passe
      for(ClientServeur client : infos.clients){
        if(client.nom.equals(pseudo) && client.motDePasse == motDePasse){
          System.out.println("Connexion de " + pseudo);
          writer.writeBytes("HELLO+++");
          writer.flush();

          clientActuel = client;
          return true;
        }
      }
      System.out.println("Refus de connexion : ce client n'existe pas ou le mot de passe est erroné");
      writer.writeBytes("GOBYE+++");
      writer.flush();

      return false;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
      return false;
    }
  }

  /**
   * On gère ici la demande d'amitié envoyée par un client vers un autre client.
   * @return true si la demande est envoyée, false sinon.
   */
  public boolean demandeAmitie(){
    try {
      reader.readByte();
      String pseudo = Utils.getId(reader);
      Utils.enlever3Plus(reader);

      System.out.println(clientActuel.nom + " demande " + pseudo + " en ami");

      //On vérifie que le pseudo demandé existe bien, et que ce n'est pas celui qui fait la demande
      for(ClientServeur client : infos.clients){
        if(client.nom.equals(pseudo) && !clientActuel.nom.equals(pseudo)){
          for(ClientServeur ami : clientActuel.amis){
            if(ami.nom.equals(pseudo)){
              System.out.println("Demande d'amitié refusée : " + clientActuel.nom + " est déjà ami avec " + pseudo);
              writer.writeBytes("FRIE<+++");
              writer.flush();

              return true;
            }
          }
          //Faire l'envoi UDP
          boolean envoi = infos.envoiUDP(client, '0');

          //On ajoute au flux
          client.flux.add("0 " + clientActuel.nom);

          if(envoi){
            writer.writeBytes("FRIE>+++");
            writer.flush();
            return true;
          }
        }
      }
      System.out.println("Demande d'amitié refusée : le pseudo " + pseudo + " n'existe pas ou alors c'est le pseudo de celui qui fait la demande.");
      writer.writeBytes("FRIE<+++");
      writer.flush();

      return true;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Cette fonction gère l'envoi d'un message d'un client vers un autre client
   * @return true si l'envoi est confirmé, false sinon.
   */
  public boolean envoieMessage(){
    try {
      reader.readByte();
      String pseudo = Utils.getId(reader);
      reader.readByte();
      int nombreDeMessages = Utils.getNumNoctets(reader, 4);
      Utils.enlever3Plus(reader);

      String messageTotal = Utils.getMessage3(reader, nombreDeMessages);

      System.out.println(clientActuel.nom + " envoie un message à " + pseudo);
      System.out.println("Le message : " + messageTotal + "\nReçu en " + nombreDeMessages + " sous messages");


      //On regarde si le pseudo existe
      for(ClientServeur client : infos.clients){
        if(client.nom.equals(pseudo)){
          boolean sontAmis = false;
          for(ClientServeur ami : client.amis){
            if(ami == clientActuel){
              sontAmis = true;
            }
          }

          if(sontAmis){
            //On envoie un UDP
            boolean envoi = infos.envoiUDP(client, '3');

            if(envoi){
              client.flux.add("3 " + clientActuel.nom + " " + messageTotal);
              System.out.println("Message bien envoyé.");
              writer.writeBytes("MESS>+++");
              writer.flush();
              return true;
            }
          }
        }
      }

      System.out.println("Fail de l'envoi de message : le pseudo n'existe pas, ou alors ils ne sont pas amis.");
      writer.writeBytes("MESS<+++");
      writer.flush();

      return true;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Cette fonction gère l'inondation demandée par un client.
   * Elle transmet le message souhaité à tous ses amis et ainsi de suite.
   * @return true si l'inondation est réussie, false sinon.
   */
  public boolean inondation(){
    try {
      reader.readByte();
      String message = Utils.getMessage2(reader);
      System.out.println("Message d'inondation de " + clientActuel.nom + " : " + message + " de taille : " + message.length());

      for(ClientServeur ami : clientActuel.amis){
        System.out.println("Message envoyé à : " + ami.nom);
        infos.envoiUDP(ami, '4');
        ami.flux.add("4 " + clientActuel.nom + " " + message);
      }

      writer.writeBytes("FLOO>+++");
      writer.flush();

      return true;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Cette fonction fourni les noms des clients connectés pour être affichés suite à l'apppel par un client de la commande "liste"
   * @return true si la liste est affichée.
   */
  public boolean listeClients(){
    try {
      Utils.enlever3Plus(reader);

      int nombreDeClients = infos.clients.size();
      String nombreDeMessages = Utils.completerDe0Jusqua(String.valueOf(nombreDeClients), 3);
      writer.writeBytes("RLIST " + nombreDeMessages + "+++");
      writer.flush();

      System.out.println("On envoie " + nombreDeMessages + " messages pour donner la liste des clients à " + clientActuel.nom);

      for(ClientServeur client : infos.clients){
        writer.writeBytes("LINUM " + client.nom + "+++");
        writer.flush();
      }

      return true;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Cette fonction est la fonction générale gérant les demandes de consultation des notifications d'un client.
   * Elle appelle la sous-fonction associée au type de notification.
   * @return true si la consultation se déroule correctement, false sinon.
   */
  public boolean consultation(){
    try {
      Utils.enlever3Plus(reader);

      System.out.println("Demande de consultation de " + clientActuel.nom);

      try {
        String notification = clientActuel.flux.remove(clientActuel.flux.size() - 1);

        switch(notification.charAt(0)){
          case '0' :
            return consultationDemandeAmitie(notification);
          case '1' :
            return consultationAcceptationAmitie(notification);
          case '2' :
            return consultationRefusAmitie(notification);
          case '3' :
            return consultationMessage(notification);
          case '4' :
            return consultationInondation(notification);
          case '5' :
            return consultationPublicite(notification);
          case '6' :
            return consultationSondage(notification);
          default :
            System.out.println("ça ne devrait jamais arriver ici");
            return false;
        }
      } catch (Exception e){
        writer.writeBytes("NOCON+++");
        writer.flush();
        return true;
      }
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Cette sous-fonction gère la consultation de demandes d'amitié
   * @param notification, la notification
   * @return true si la consultation est réussie, false sinon
   */
  public boolean consultationDemandeAmitie(String notification){
    try {
      System.out.println("Consultation d'une demande d'amitié");
      String pseudo = notification.substring(2, notification.length());
      writer.writeBytes("EIRF> ");
      byte[] pseudo_b = Utils.conversionUTF8(pseudo);
      writer.write(pseudo_b);
      writer.writeBytes("+++");
      writer.flush();

      //On récupère le client qui avait envoyé la demande
      ClientServeur clientDemandeur = null;
      for(ClientServeur client : infos.clients){
        if(client.nom.equals(pseudo)){
          clientDemandeur = client;
        }
      }

      //On lit la réponse du client
      String type = Utils.getType(reader);
      Utils.enlever3Plus(reader);
      if(type.equals("OKIRF")){
        System.out.println(clientActuel.nom + " a accepté la demande d'amitié de " + pseudo);
        infos.envoiUDP(clientDemandeur, '1');
        clientDemandeur.flux.add("1 " + clientActuel.nom);

        clientDemandeur.amis.add(clientActuel);
        clientActuel.amis.add(clientDemandeur);
      } else {
        System.out.println(clientActuel.nom + " a refusé la demande d'amitié de " + pseudo);
        infos.envoiUDP(clientDemandeur, '2');
        clientDemandeur.flux.add("2 " + clientActuel.nom);
      }

      writer.writeBytes("ACKRF+++");
      writer.flush();

      return true;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Cette sous-fonction gère la consultation de l'acceptation d'une demande d'amitié
   * @param notification, la notification
   * @return true si la consultation est réussie, false sinon
   */
  public boolean consultationAcceptationAmitie(String notification){
    try {
      System.out.println("Consultation d'acceptation d'amitié");
      String pseudo = notification.substring(2, notification.length());
      writer.writeBytes("FRIEN ");
      byte[] pseudo_b = Utils.conversionUTF8(pseudo);
      writer.write(pseudo_b);
      writer.writeBytes("+++");
      writer.flush();
      return true;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Cette sous-fonction gère la consultation d'un refus de demande en ami
   * @param notification, la notification
   * @return true si la consultation est réussie, false sinon
   */
  public boolean consultationRefusAmitie(String notification){
    try {
      System.out.println("Consultation de refus d'amitié");
      String pseudo = notification.substring(2, notification.length());
      writer.writeBytes("NOFRI ");
      byte[] pseudo_b = Utils.conversionUTF8(pseudo);
      writer.write(pseudo_b);
      writer.writeBytes("+++");
      writer.flush();
      return true;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Cette sous-fonction gère la consultation des messages envoyés par les autres clients
   * @param notification, la notification
   * @return true si la consultation est réussie, false sinon
   */
  public boolean consultationMessage(String notification){
    try {
      System.out.println("Consultation de message");
      String pseudo = notification.substring(2, 10);
      String message = notification.substring(11, notification.length());
      byte[] message_b = Utils.conversionUTF8(message);
      int nombreDeMessages = message_b.length / 200 + (message_b.length % 200 == 0 ? 0 : 1);

      System.out.println("message_b length : " + message_b.length);

      //On envoie le premier message
      writer.writeBytes("SSEM> ");
      byte[] pseudo_b = Utils.conversionUTF8(pseudo);
      writer.write(pseudo_b);
      writer.writeBytes(" " + Utils.completerDe0Jusqua(String.valueOf(nombreDeMessages), 4) + "+++");
      writer.flush();

      System.out.println("On va envoyer " + nombreDeMessages + " messages");

      //On envoie ensuire LE message morceau par morceau
      for(int i = 0; i < nombreDeMessages; i++){
        writer.writeBytes("MUNEM " + Utils.completerDe0Jusqua(String.valueOf(nombreDeMessages), 4) + " ");
        byte[] mess = new byte[(message_b.length % 200 != 0 ? message_b.length%200 : 200)];
        System.arraycopy(message_b, i*200, mess, 0, (message_b.length % 200 != 0 ? message_b.length%200 : 200));
        System.out.println("J'envoie (" + i + ") : " + new String(mess));
        writer.write(mess);
        writer.writeBytes("+++");
        writer.flush();
      }

      return true;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Cette sous-fonction gère la consultation des messages de flood des autres clients
   * @param notification, la notification
   * @return true si la consultation est réussie, false sinon
   */
  public boolean consultationInondation(String notification){
    System.out.println("Consultation d'inondation");
    try {
      String pseudo = notification.substring(2, 11);
      String message = notification.substring(11, notification.length());
      writer.writeBytes("OOLF> ");
      byte[] pseudo_b = Utils.conversionUTF8(pseudo);
      writer.write(pseudo_b);
      //writer.writeBytes(" ");
      byte[] message_b = Utils.conversionUTF8(message);
      writer.write(message_b);
      writer.writeBytes("+++");
      writer.flush();

      return true;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Cette sous-fonction gère la consultation des messages publicitaires des promoteurs
   * @param notification, la notification
   * @return true si la consultation est réussie, false sinon
   */
  public boolean consultationPublicite(String notification){
    try {
      System.out.println("Consultation de publicité");
      String ip = notification.substring(2, 17);
      String port = notification.substring(18, 23);
      String message = notification.substring(23, notification.length());

      writer.writeBytes("LBUP> " + ip + " " + port + " ");
      byte[] message_b = Utils.conversionUTF8(message);
      writer.write(message_b);
      writer.writeBytes("+++");
      writer.flush();

      return true;
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Cette sous-fonction gère la consultation des sondages, envoyés par les promoteurs
   * @param notification, la notification
   * @return true si la consultation est réussie, false sinon
   */
  public boolean consultationSondage (String notification) {
    try {
      System.out.println("Consultation de sondage");
      String ip = notification.substring(2, 17);
      System.out.println("ip : |" + ip + "| de longueur " + ip.length());
      String port = notification.substring(18, 23);
      System.out.println("port : " + port);
      String nbQuestions = notification.substring(23, 24);
      System.out.println("nbQuestions : " + nbQuestions);
      String message = notification.substring(25, notification.length());
      System.out.println("message : " + message);

      writer.writeBytes("SNDGE " + ip + " " + port + " " + nbQuestions + " ");
      byte[] message_b = Utils.conversionUTF8(message);
      writer.write(message_b);
      writer.writeBytes("+++");
      writer.flush();

      return true;
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Cette fonction permet d'envoyer un message de demande de déconnexion du serveur
   * @return true si le message est envoyé, false sinon
   */
  public boolean deconnexion(){
    try {
      System.out.println("Deconnexion de " + clientActuel.nom);
      Utils.enlever3Plus(reader);

      writer.writeBytes("GOBYE+++");
      writer.flush();

      return false;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
      return false;
    }
  }

}
