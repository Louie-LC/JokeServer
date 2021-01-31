import java.io.*;
import java.net.*;

public class AdminWorker extends Thread
{
   private Socket socket;

   public AdminWorker(Socket socket)
   {
      this.socket = socket;
   }
   
   public void run()
   {   
      AdminLooper.isInJokeMode = !AdminLooper.isInJokeMode;
      
      try
      {
         PrintStream streamToClient = new PrintStream(socket.getOutputStream());
         
         String response = JokeServer.isPrimaryServer ? "" : "<S2> ";
         response += "Server in ";
         response += AdminLooper.isInJokeMode ? "joke mode" : "proverb mode";
         
         streamToClient.println(response);
         JokeServer.printServerMode();
      }
      catch(IOException e)
      {
         System.out.println(e);
      }

   }

}