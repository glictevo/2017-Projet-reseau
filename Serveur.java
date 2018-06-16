import java.lang.*;
import java.net.*;
import java.io.*;

public class Serveur{

  public static void main(String[] args){

    //On regarde les informations reçues en arguments pour les différents ports.
    if(args.length != 2){
      System.out.println("usage : java serveur [num port TCP clients] [num port TCP promoteurs]");
      return ;
    }

    //infos TCP
    int portTCPClients = Integer.parseInt(args[0]);
    int portTCPPromoteurs = Integer.parseInt(args[1]);

    if(portTCPClients < 0 || portTCPClients >= 9999 || portTCPPromoteurs < 0 || portTCPPromoteurs >= 9999){
      System.out.println("Les ports doivent être compris entre 0 et 9998");
      return ;
    }

    ServeurInfos infos = new ServeurInfos(portTCPClients, portTCPPromoteurs);

    //On lance deux threads de connexion :
      //Un pour la réception des clients
      //Un autre pour la réception des promoteurs
      //Ils se chargent de faire la connexion, puis appellent le thread correspondant
    ConnexionClientService serviceClient = new ConnexionClientService(infos);
    ConnexionPromoteurService servicePromoteur = new ConnexionPromoteurService(infos);
    Thread threadClient = new Thread(serviceClient);
    Thread threadPromoteur = new Thread(servicePromoteur);
    threadClient.start();
    threadPromoteur.start();

    System.out.println("Le serveur est lancé.");

    //Nos deux services de connexion tournent maintenant.
  }
}
