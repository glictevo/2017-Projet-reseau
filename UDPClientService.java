import java.nio.*;
import java.net.*;
import java.io.*;

public class UDPClientService implements Runnable{

  int portUDP;

  public UDPClientService(int portUDP){
    this.portUDP = portUDP;
  }

  public void run(){
    //Recevra les notifications et les affichera sur la console
    try{
      DatagramSocket dso = new DatagramSocket(portUDP);
      byte[] notification = new byte[3];
      DatagramPacket paquet = new DatagramPacket(notification, notification.length);
      while(true){
        dso.receive(paquet);
        byte[] data = paquet.getData();

        char code = (char) data[0];

        ByteBuffer bb = ByteBuffer.allocate(4);
        //bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put((byte) 0);
        bb.put((byte) 0);
        bb.put(data[1]);
        bb.put(data[2]);
        int numeroNotification = bb.getInt(0);

        System.out.println("--------------------");
        System.out.print("Notification (" + numeroNotification + " en attente): ");

        switch(code){
          case '0':
            System.out.println("Vous avez une nouvelle demande d'amitié.");
            break;
          case '1':
            System.out.println("Une de vos demande d'amitié a été acceptée.");
            break;
          case '2':
            System.out.println("Une de vos demande d'amitié a été refusée.");
            break;
          case '3':
            System.out.println("Vous avez reçu un nouveau message privé.");
            break;
          case '4':
            System.out.println("Un de vos amis a posté un nouveau message public.");
            break;
          case '5':
            System.out.println("Publicité");
            break;
          case '6':
            System.out.println("Un sondage est disponible.");
            break;
        }

        System.out.println("--------------------");
      }
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }
}
