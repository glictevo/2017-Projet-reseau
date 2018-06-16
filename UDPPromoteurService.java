import java.nio.*;
import java.net.*;
import java.io.*;

public class UDPPromoteurService implements Runnable {

  //Il s'agit du port d'écoute, celui sur lequel ProcessSondage va envoyer les résultats du sondage
  int port;
  int nbRepoMax;
  int[] votes;
  String iDSondage;

  public UDPPromoteurService (int port, int nbQuestions, int nbRepoMax) {
    this.port = port;
    this.votes = new int[nbQuestions];
    for(int i = 0; i < nbQuestions; i++) {
      this.votes[i] = 0;
    }
    this.nbRepoMax = nbRepoMax;
  }

  public void run () {
    try{
      System.out.println("On lance le thread qui reçoit les résultats du sondage.");

      DatagramSocket dso = new DatagramSocket(port);
      byte[] reponse = new byte[4];
      DatagramPacket paquet = new DatagramPacket(reponse, reponse.length);
      int i = 0;
      while(i < nbRepoMax) {
          dso.receive(paquet);
          System.out.println("received paquet");
          String resultat = new String(paquet.getData(), 0, paquet.getLength());
          System.out.println("Resultat : "+ resultat);
          this.traiterReponse(resultat);
          i++;
      }
      this.displayResults();
      //ferme la socket pour permettre sa reutilisation par un nouveau sondage du meme promoteur
      dso.close();

    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }

  /**
   * Cette fonction traite les résultats reçus au fur et à mesure, c'est à dire qu'elle incrémente le tableau de résultats possibles
   * @param resultat, la string réponse du client (correspondant à une possibilité du sondage)
   */
  public void traiterReponse (String resultat) {
    try {
      int reponse = Integer.parseInt(resultat);
      this.votes[reponse - 1] ++;
    } catch (Exception e) {
      System.out.println("Le résultat reçu ne correspond pas au format requis. Il ne sera pas pris en compte.");
      return ;
    }

  }

  /**
   * Cette fonction affiche, dans le terminal du promoteur, les résultats de son sondage, une fois les votes reçus.
   */
  public void displayResults() {
    for(int i = 0; i < this.votes.length; i++) {
      System.out.println("La réponse " + (i+1) + " a reçu " + votes[i] + "votes !");
    }
  }
}
