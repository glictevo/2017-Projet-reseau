import java.util.ArrayList;
import java.net.*;
import java.io.*;

public class ConnexionPromoteurService implements Runnable{

  ServeurInfos infos;

  public ConnexionPromoteurService(ServeurInfos infos){
    this.infos = infos;
  }

  public void run(){
    try{
      ServerSocket server = new ServerSocket(this.infos.portTCPPromoteurs);
      while(true){
        Socket socket = server.accept();
        TCPServeurPromoteurService service = new TCPServeurPromoteurService(socket, infos);
        Thread t = new Thread(service);
        t.start();
      }
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }
}
