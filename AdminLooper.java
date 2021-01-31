import java.io.*;
import java.net.*;

public class AdminLooper implements Runnable
{   
   public static boolean isInJokeMode = true;

   public static boolean adminControlSwitch = true;
   private final int QUEUE_LENGTH = 6;
   private int port;
   
   public AdminLooper(boolean isPrimaryServer)
   {
      if(isPrimaryServer)
         port = 5050;
      else
         port = 5051;
   }
   
   
   public void run()
   {      
      Socket socket;
      
      try
      {
         ServerSocket serverSocket = new ServerSocket(port, QUEUE_LENGTH);
         while(adminControlSwitch)
         {
            socket = serverSocket.accept();
            new AdminWorker(socket).start();
         }
      }
      catch(IOException e)
      {
         System.out.println(e);
      }
   }

}