import java.util.ArrayList;
import java.net.*;
import java.io.*;

public class TCPServeurPromoteurService implements Runnable{

  Socket socket;
  ServeurInfos infos;
  DataOutputStream writer;
  DataInputStream reader;

  public TCPServeurPromoteurService(Socket socket, ServeurInfos infos){
    this.socket = socket;
    this.infos = infos;
  }

  public void run(){
    System.out.println("Je lance le thread de communication avec le promoteur");
    try {
      writer = new DataOutputStream(socket.getOutputStream());
      reader = new DataInputStream(socket.getInputStream());

      String type = Utils.getType(reader);
      reader.readByte();
      if(type.equals("PUBL?")){
        String ip = Utils.getIp(reader);
        System.out.println("ip : " + ip);
        reader.readByte();
        String port = String.valueOf(Utils.getNumNoctets(reader, 4));
        System.out.println("port : " + port);
        reader.readByte();
        String message = Utils.getMessage2(reader);

        //On envoie en UDP aux clients
        for(ClientServeur client : infos.clients){
          infos.envoiUDP(client, '5');
          client.flux.add("5 " + ip + " " + Utils.completerDe0Jusqua(port, 4) + " " + message);
          System.out.println("5 " + ip + " " + Utils.completerDe0Jusqua(port, 4) + " " + message);

        }

        //On écrit au promoteur pour dire que c'est bon
        writer.writeBytes("PUBL>+++");
        writer.flush();

        socket.close();

        //si c'est un sondage
      } else if(type.equals("SNDG?")) {

        String ip = Utils.getIp(reader);
        System.out.println("ip : " + ip);

        reader.readByte();

        String port = String.valueOf(Utils.getNumNoctets(reader, 4));
        System.out.println("port : " + port);

        reader.readByte();

        String nbQuestions = String.valueOf(Utils.getNumNoctets(reader, 1));
        System.out.println("Nombre de questions : " + nbQuestions);
        
        System.out.println(reader.readByte());
        String message = Utils.getMessage2(reader);

        //On envoie en UDP aux clients
        for(ClientServeur client : infos.clients){
          infos.envoiUDP(client, '6');
          client.flux.add("6 " + ip + " " + Utils.completerDe0Jusqua(port, 4) + " " + nbQuestions + " " + message);
          System.out.println("6 " + ip + " " + Utils.completerDe0Jusqua(port, 4) + " " + nbQuestions + " " + message);
        }
        //On écrit au promoteur pour dire que c'est bon
        writer.writeBytes("SNDG>+++");
        writer.flush();
        socket.close();

      } else {
        System.out.println("On reçoit une commande inconnue du promoteur");
      }
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();

    }
  }
}
