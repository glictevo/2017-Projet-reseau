import java.nio.*;
import java.net.*;
import java.io.*;

public class UDPPubliciteClientService implements Runnable{

  int port;
  String ip;

  public UDPPubliciteClientService(String ip, int port){
    this.ip = ip;
    this.port = port;
  }

  public void run(){
    //Recevra les notifications et les affichera sur la console
    try{
      System.out.println("On lance le thread qui reçoit la diffusion");

      MulticastSocket mso = new MulticastSocket(port);
      mso.joinGroup(InetAddress.getByName(ip));
      byte[] data = new byte[305];
      DatagramPacket paquet = new DatagramPacket(data, data.length);
      while(true){
        mso.receive(paquet);
        String result = new String(paquet.getData(), 5, 300);
        System.out.println("Publicité : \n" + result);
      }
    } catch (SocketException se){
      System.out.println("L'adresse IP donnée par le promoteur est erroné.");
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }
}
