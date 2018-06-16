import java.nio.*;
import java.net.*;
import java.io.*;
import java.nio.charset.*;

public class Utils{

  public static byte[] conversionUTF8(String s){
    return s.getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Cette fonction rajoute des zéros à la string jusqu'à ce que la string ait n de taille
   * @param num la string étudiée, un entier sous forme de string
   * @param n la taille souhaitée de la string
   * @return la string remplie de zéros, de taille n
   */
  public static String completerDe0Jusqua(String num, int n){
    int diff = n - num.length();
    String result = "";

    for(int i = 0; i < diff; i++){
      result += "0";
    }

    return result + num;
  }

  /**
   * Cette fonction permet de compléter une adresse ip de # jusqu'à obtenir une adresse ip de taille 15
   * @param ip l'adresse ip à compléter
   * @return l'adresse ip complétée de #
   */
  public static String completionIp(String ip){
    for(int i = ip.length(); i < 15; i++){
      ip += "#";
    }
    return ip;
  }

  /**
   * Cette fonction renvoie l'adresse ip sans #
   * @param ip l'adresse ip à nettoyer
   * @return l'adresse ip sans #
   */
  public static String getCleanIp(String ip){
    String cleanIp = "";
    int index = 0;

    while(index < 15 && ip.charAt(index) != '#'){
      cleanIp += ip.charAt(index);
      index++;
    }

    System.out.println("clean ip : " + cleanIp);
    return cleanIp;
  }

  /**
   * Cette fonction renvoie le type (sur 5 bytes) du message
   * @param reader
   * @return le type sous forme de string
   * @throws IOException
   */
  public static String getType(DataInputStream reader) throws IOException{
    byte[] buf = new byte[5];
    reader.readFully(buf);
    String type = new String(buf, "UTF-8");
    return type;
  }

  /**
   * Cette fonction récupère l'identifiant du client
   * @param reader
   * @return l'identifiant du client sous forme de string
   * @throws IOException
   */
  public static String getId(DataInputStream reader) throws IOException{
    byte[] buf = new byte[8];
    reader.readFully(buf);
    String id = new String(buf, "UTF-8");
    return id;
  }

  /**
   * Cette fonction récupère l'adresse ip
   * @param reader
   * @return l'adresse ip sous forme de string
   * @throws IOException
   */
  public static String getIp(DataInputStream reader) throws IOException{
    byte[] buf = new byte[15];
    reader.readFully(buf);
    String ip = new String(buf);
    return ip;
  }

  /**
   * Cette fonction lit n octets et retourne le numéro lu
   * @param reader
   * @param n le nombre d'octets à lire
   * @return le numéro lu
   * @throws IOException
   */
  public static int getNumNoctets(DataInputStream reader, int n) throws IOException{
    byte[] buf = new byte[n];
    reader.readFully(buf);
    int num = Integer.parseInt(new String(buf));
    return num;
  }

  /**
   * Cette fonction récupère le mot de passe d'un client
   * @param reader
   * @return le mot de passe sous forme d'entier
   * @throws IOException
   */
  public static int getMotDePasse(DataInputStream reader) throws IOException{
    byte[] buf = new byte[2];
    reader.readFully(buf);

    ByteBuffer bb = ByteBuffer.allocate(4);
    //bb.order(ByteOrder.LITTLE_ENDIAN);
    bb.put((byte) 0);
    bb.put((byte) 0);
    bb.put(buf[0]);
    bb.put(buf[1]);
    int motDePasse = bb.getInt(0);

    return motDePasse;
  }

  /**
   *
   * @param reader
   * @return
   * @throws IOException
   */
  public static String getMessage2(DataInputStream reader) throws IOException{
    byte[] bufTotal = new byte[1024];

    for(int i = 0; i < 203; i++){
      byte caractere = reader.readByte();
      //System.out.print((char)caractere);
      if((char) caractere == '+'){
        //System.out.println("Je repère le 1er +");
        byte caractere2 = reader.readByte();
        if((char) caractere2 == '+'){
          //System.out.println("Je repère le deuxième +");
          byte caractere3 = reader.readByte();
          if((char) caractere3 == '+'){
            //System.out.println("Je repère le troisième +");
            byte[] bufTexte = new byte[i];
            System.arraycopy(bufTotal, 0, bufTexte, 0, bufTexte.length);
            return new String(bufTexte, "UTF-8");
          }
          bufTotal[i] = caractere3;
          i++;
        }
        bufTotal[i] = caractere2;
        i++;
      }
      bufTotal[i] = caractere;
    }
    //System.out.println("Avant de renvoyer null, j'ai : " + message);

    return null;
  }

  /**
   *
   * @param reader
   * @param nombreDeMessages
   * @return
   * @throws IOException
   */
  public static String getMessage3(DataInputStream reader, int nombreDeMessages) throws IOException{
    byte[] bufTotal = new byte[200*nombreDeMessages];

    int index = 0;
    for(int j = 0; j < nombreDeMessages; j++){
      Utils.getType(reader);
      reader.readByte();
      Utils.getNumNoctets(reader, 4);
      reader.readByte();

      int i = 0;
      byte[] buf = new byte[1024];
      while(true){
        byte caractere = reader.readByte();
        //System.out.print((char)caractere);
        if((char) caractere == '+'){
          //System.out.println("Je repère le 1er +");
          byte caractere2 = reader.readByte();
          if((char) caractere2 == '+'){
            //System.out.println("Je repère le deuxième +");
            byte caractere3 = reader.readByte();
            if((char) caractere3 == '+'){
              //System.out.println("Je repère le troisième +");
              byte[] bufTexte = new byte[i];
              System.arraycopy(buf, 0, bufTexte, 0, bufTexte.length);
              System.arraycopy(bufTexte, 0, bufTotal, index, bufTexte.length);
              index += bufTexte.length;
              break;
            }
            buf[i] = caractere3;
            i++;
          }
          buf[i] = caractere2;
          i++;
        }
        buf[i] = caractere;
        i++;
      }
      //System.out.println("Avant de renvoyer null, j'ai : " + message);
    }

    return new String(bufTotal, "UTF-8");
  }

  /**
   * Cette fonction ote les trois + présents dans l'input
   * @param reader
   * @throws IOException
   */
  public static void enlever3Plus(DataInputStream reader) throws IOException{
    byte[] buf = new byte[3];
    reader.readFully(buf);
  }

}
