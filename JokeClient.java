import java.io.*;
import java.net.*;



public class JokeClient
{

   static String username = "";
   static int id = -1;
   
   static String primaryServerAddress;
   static int primaryServerPort = 4545; 
   
   static String secondaryServerAddress;
   static int secondaryServerPort = 4546;
   
   static boolean hasSecondaryServer;
   static boolean usingPrimaryServer;
   
   public static void main(String args[])
   {
      setServerAddresses(args);
      usingPrimaryServer = true;
      printIntro();
      
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      
      try
      {
         System.out.print("Enter a username: " );
         username = in.readLine();
         System.out.println("Hello " + username);
         printCurrentServerInfo();
         String userInput = "";
         boolean userQuit = false;         
         do
         {
            System.out.flush();
            userInput = in.readLine();
            if(userInput.toLowerCase().equals("quit"))
               userQuit = true;
            else if(userInput.toLowerCase().equals("s"))
            {
               usingPrimaryServer = !usingPrimaryServer;
               printCurrentServerInfo();
            }
            else
            {
               if(usingPrimaryServer)
                  getServerSentence(primaryServerAddress, primaryServerPort);
               else
                  getServerSentence(secondaryServerAddress, secondaryServerPort);
            }
            
         }while(!userQuit);
      }
      catch(IOException e)
      {
         System.out.println(e);
      }
   }
   
   private static void getServerSentence(String serverAddress, int serverPort)
   {
      Socket socket;
      BufferedReader streamFromServer;
      PrintStream streamToServer;
      String textFromServer;
      try
      {
         socket = new Socket(serverAddress, serverPort);
         streamFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         streamToServer = new PrintStream(socket.getOutputStream());
         
         streamToServer.println(username);
         streamToServer.println(id);
         
         int idFromServer = Integer.parseInt(streamFromServer.readLine());
                  
         if(id != idFromServer)
         {
            id = idFromServer;
         }
         
         textFromServer = streamFromServer.readLine();
         System.out.println(textFromServer);   
         socket.close();
      }
      catch(IOException e)
      {
         System.out.println(e);
      }
   }
   
   private static void setServerAddresses(String[] commandArgs)
   {
      primaryServerAddress = "localhost";
      hasSecondaryServer = false;
      if(commandArgs.length > 0)
         primaryServerAddress = commandArgs[0];
      if(commandArgs.length > 1)
      {
         secondaryServerAddress = commandArgs[1];
         hasSecondaryServer = true;
      }
   }
   
   private static void printCurrentServerInfo()
   {
      System.out.print("Now communicating with: ");
      if(usingPrimaryServer)
         System.out.println(primaryServerAddress + ", port " + primaryServerPort);
      else
         System.out.println(secondaryServerAddress + ", port " + secondaryServerPort);
   }
   
   private static void printIntro()
   {
      System.out.println("Louis Rivera's Joke Client.");
      System.out.println("Press enter for joke or proverb. Type quit to stop program.\n");
      System.out.println("Server one: " + primaryServerAddress + ", port " + primaryServerPort);
      if(hasSecondaryServer)
      {
         System.out.println("Server two: " + secondaryServerAddress + ", port " + secondaryServerPort);
         System.out.println("Type s to switch between servers");
      }
   }
}