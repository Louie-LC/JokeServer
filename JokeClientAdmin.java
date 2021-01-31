import java.io.*;
import java.net.*;



public class JokeClientAdmin
{
   public static PrintStream out = System.out;
   
   
   public static String primaryServerAddress;
   public static int primaryServerPort = 5050;
   
   public static String secondaryServerAddress;
   public static int secondaryServerPort = 5051;
   
   static boolean hasSecondaryServer;
   static boolean usingPrimaryServer;
   
   public static void main(String[] args)
   {
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      String userInput = "";
      boolean userQuit = false;
      boolean usingPrimaryServer = true;
      setServerAddresses(args);
      
      printIntro();
      System.out.print("Now communicating with: ");
      if(usingPrimaryServer)
         System.out.println(primaryServerAddress + ", port " + primaryServerPort);
      else
         System.out.println(secondaryServerAddress + ", port " + secondaryServerPort);
      try
      {
         do
         {
            System.out.flush();
            userInput = in.readLine();
            if(userInput.toLowerCase().equals("quit"))
               userQuit = true;
            else if(userInput.toLowerCase().equals("s"))
            {
               usingPrimaryServer = !usingPrimaryServer;
               
               System.out.print("Now communicating with: ");
               if(usingPrimaryServer)
                  System.out.println(primaryServerAddress + ", port " + primaryServerPort);
               else
                  System.out.println(secondaryServerAddress + ", port " + secondaryServerPort);
            }
            else
            {
               if(usingPrimaryServer)
                  changeServerMode(primaryServerAddress, primaryServerPort); 
               else
                  changeServerMode(secondaryServerAddress, secondaryServerPort);
            }

            
         } while(!userQuit);
      
      }
      catch(IOException e)
      {
         System.out.println(e);
      }
      
      

   }
   
   public static void changeServerMode(String serverAddress, int port)
   {
      Socket socket;
      BufferedReader streamFromServer;
      PrintStream streamToServer;
      String textFromServer;
      
      try
      {
         socket = new Socket(serverAddress, port);
         streamFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         String response = streamFromServer.readLine();
         System.out.println(response);
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
   
   private static void printIntro()
   {
      System.out.println("Louis Rivera's JokeClientAdmin.");
      System.out.println("Press enter to switch server to joke or proverb mode. Type quit to stop program.\n");
      System.out.println("Server one: " + primaryServerAddress + ", port " + primaryServerPort);
      if(hasSecondaryServer)
      {
         System.out.println("Server two: " + secondaryServerAddress + ", port " + secondaryServerPort);
         System.out.println("Type s to switch between servers");
      }
   }
   
   private static void printCurrentServerInfo()
   {

   }
}