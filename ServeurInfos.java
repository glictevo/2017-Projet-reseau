import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;
import java.nio.charset.*;
import java.io.*;
import java.nio.*;
import java.net.*;

public class ServeurInfos{

  CopyOnWriteArrayList<ClientServeur> clients;

  //infos TCP
  int portTCPClients;
  int portTCPPromoteurs;

  CopyOnWriteArrayList<String> sondages;

  /**
   * Constructeur de ServeurInfos
   * @param portTCPClients, le port TCP des clients
   * @param portTCPPromoteurs, le port TCP des promoteurs
   */
  public ServeurInfos(int portTCPClients, int portTCPPromoteurs){
    this.portTCPClients = portTCPClients;
    this.portTCPPromoteurs = portTCPPromoteurs;
    this.clients = new CopyOnWriteArrayList<ClientServeur>();
    this.sondages = new CopyOnWriteArrayList<String>();
    //NOTE : faire ici la récupération de clients sérialisé
  }

  /**
   * Envoie un paquet au port UDP du client.
   * @param client
   * @param code, le code de la notification à envoyer
   * @return
   */
  public static boolean envoiUDP(ClientServeur client, char code){
    try{
      DatagramSocket dso = new DatagramSocket();
      byte[] data = new byte[3];
      data[0] = (byte) code;

      byte[] numNotif = ByteBuffer.allocate(4).putInt(client.flux.size() + 1).order(ByteOrder.LITTLE_ENDIAN).array();
      data[1] = numNotif[2];
      data[2] = numNotif[3];

      DatagramPacket paquet = new DatagramPacket(data, data.length, client.ip, client.portUDP);
      dso.send(paquet);

      return true;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
      return false;
    }
  }


}
