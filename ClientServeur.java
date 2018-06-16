import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;
import java.net.*;

//Un client représenté côté serveur, le serveur en a un ArrayList
public class ClientServeur{

  String nom;
  int motDePasse;
  InetAddress ip;
  int portUDP;

  CopyOnWriteArrayList<ClientServeur> amis;
  CopyOnWriteArrayList<String> flux;

  /**
   * Constructeur de ClientServeur
   * @param nom, le nom du Client
   * @param motDePasse, le password du Client
   * @param portUDP, le portUDP d'écoute
   * @param ip
   */
  public ClientServeur(String nom, int motDePasse, int portUDP, InetAddress ip){
    this.nom = nom;
    this.portUDP = portUDP;
    this.motDePasse = motDePasse;
    this.ip = ip;
    this.amis = new CopyOnWriteArrayList<ClientServeur>();
    this.flux = new CopyOnWriteArrayList<String>();
  }

}
