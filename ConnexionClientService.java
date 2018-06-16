import java.util.ArrayList;
import java.net.*;
import java.io.*;

public class ConnexionClientService implements Runnable{

  ServeurInfos infos;

  public ConnexionClientService(ServeurInfos infos){
    this.infos = infos;
  }

  public void run(){
    try{
      ServerSocket server = new ServerSocket(this.infos.portTCPClients);
      while(true){
        Socket socket = server.accept();
        System.out.println("J'ai accepté un client");
        TCPServeurClientService service = new TCPServeurClientService(socket, infos);
        Thread t = new Thread(service);
        t.start();
      }
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }
}
