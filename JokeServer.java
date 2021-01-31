import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.Random;

public class JokeServer
{

   public static PrintStream out = System.out;

   // All of the jokes were taken from this site:
   // https://howtodoinjava.com/for-fun-only/some-java-programmers-jokes/
   public static String[] jokes = 
   {
      "There are 10 kinds of people in the world: Those that know binary & those that dont",
      "A SQL query goes into a bar, walks up to two tables and asks, Can I join you?",
      "If Java is the answer, it must have been a really verbose question.",
      "Unix is user friendly. It’s just very particular about who its friends are."
   };
   
   // All of the proverbs were taken from this site:
   // https://lemongrad.com/proverbs-with-meanings-and-examples/
   public static String[] proverbs = 
   {
      "Don’t put the cart before the horse.",
      "Good things come to those who wait.",
      "Hope for the best, prepare for the worst.",
      "Practice makes perfect."
   };
   
   public static final int CONCURRENCY_LIMIT = 50000;
   
   public static ConcurrentHashMap<String, int[]> trackSentenceTable;
   public static LinkedBlockingQueue<String> connectionTimeoutQueue;
   
   public static boolean isPrimaryServer;
   
   public static void main(String args[]) throws IOException
   {
      int port = getServerPort(args);
      trackSentenceTable = new ConcurrentHashMap<String, int[]>();
      connectionTimeoutQueue = new LinkedBlockingQueue<String>();
      
      AdminLooper adminLooper = new AdminLooper(isPrimaryServer);
      Thread adminThread = new Thread(adminLooper);
      adminThread.start();
      
      String ipAdress = "localhost";
      
      int queueLength = 6;
      ServerSocket serverSocket = new ServerSocket(port, queueLength, InetAddress.getByName(ipAdress));
      Socket socket;
      System.out.println("Louis Rivera's Joke Server starting up at port " + port + ".\n");
      printServerMode();
      
      while(true)
      {
         socket = serverSocket.accept();
         new ServerWorker(socket).start();
      }
   }
   
   public static void trackClientRequest(String username, int clientID)
   {      
      String uniqueID = username + clientID;
      // remove() will check to see if uniqueID is already in the queue. If it is, it's removed from wherever it is and placed at the end of the queue.
      // This ensures that the uniqueID that made the most recent request is placed at the back of the timeout queue.
      // The connectionTimeoutQueue queue is used to remove data from the trackSentenceTable. If a connection has gone too long without making a request,
      // It'll move closer to the front of the connectionTimeoutQueue. If the connectionTimeoutQueue contains more items than the CONCURRENCY_LIMIT,
      // the next time a unique item has to be added to the connectionTimeoutQueue, the front item is removed permanently to keep memory costs down.
      
      // This would probably be better handled by an additional thread and using times attached to each access. If a time ends up being too high, then
      // a user would be considered idle and would then be removed from being tracked. This is sort of a less elegant way of achieving a similar result.
         
      boolean connectionFound = connectionTimeoutQueue.remove(uniqueID);
      
      try
      {
         connectionTimeoutQueue.put(uniqueID);
      }
      catch(InterruptedException e)
      {
         System.out.println(e);
      }
      if(connectionTimeoutQueue.size() > CONCURRENCY_LIMIT)
         removeIdleClient();
      
      if(!connectionFound)
      {
         // Getting here means the user isn't currently being tracked.
         addNewClient(uniqueID);
      }
      
   }
   
   public static void removeIdleClient()
   {
      // Removes the oldest used client from being tracked.
      String clientForRemoval = connectionTimeoutQueue.poll();
      
      // Remove that same client from the data pool.
      trackSentenceTable.remove(clientForRemoval);
      
   }
   
   public static void addNewClient(String uniqueID)
   {
      int[] sentenceData = new int[]{0, 0};
      trackSentenceTable.put(uniqueID, sentenceData);
      
   }
   
   public static void printTracker()
   {
      System.out.println("Printing Tracker");
      for(String key : trackSentenceTable.keySet())
      {
         System.out.println(key);
      }
      System.out.println("\n");
   }
   
   public static void printServerMode()
   {
      String response = AdminLooper.isInJokeMode ? "Joke Mode" : "Proverb Mode";
      System.out.println(response);
   }
   
   public static int getServerPort(String[] commandArgs)
   {
      final int primaryPort = 4545;
      final int secondaryPort = 4546;
      if(commandArgs.length != 0 && commandArgs[0].toLowerCase().equals("secondary"))
      {
         isPrimaryServer = false;
         return secondaryPort;
      }
      isPrimaryServer = true;
      return primaryPort;
   }
   
}


class ServerWorker extends Thread
{
   private Socket socket;  
   
   public ServerWorker(Socket socket)
   {
      this.socket = socket;
   }
   
   public void run()
   {
      PrintStream streamToClient = null;
      BufferedReader streamFromClient = null;
      try
      {
         streamToClient = new PrintStream(socket.getOutputStream());
         streamFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         
         String clientUsername = streamFromClient.readLine();
         int clientID = Integer.parseInt(streamFromClient.readLine());
            
         clientID = verifyAndSendClientID(streamToClient, clientUsername, clientID);
         JokeServer.trackClientRequest(clientUsername, clientID);
         int position = retrieveNextSentencePosition(clientUsername, clientID);
         
         streamToClient.println(craftResponse(clientUsername, position));

      }
      catch(IOException e)
      {
         System.out.println(e);
      }
   }
   
   
   private int verifyAndSendClientID(PrintStream streamToClient, String username, int clientID)
   {  
      if(!JokeServer.trackSentenceTable.containsKey(username + clientID))
      {
         clientID = new Random().nextInt(Integer.MAX_VALUE);
      }
      streamToClient.println(clientID);
      
      return clientID;
   }
   
   private static int retrieveNextSentencePosition(String username, int clientID)
   {
      int[] sentenceData = JokeServer.trackSentenceTable.get(username + clientID);      
      int sentencePosition = 0;
      if(AdminLooper.isInJokeMode)
      {
         int jokeInt = sentenceData[0];       
         boolean validSentenceFound = false;
         Random rand = new Random();
         
         while(!validSentenceFound)
         {
            sentencePosition = rand.nextInt(4);
            
            int bitValue = (int)Math.pow(2, sentencePosition);
            
            
            if((bitValue & jokeInt) == 0)
            {
               JokeServer.out.println("Joke " + (char)('A' + sentencePosition) + " sent to " + username + clientID);
               sentenceData[0] += bitValue;
               if(sentenceData[0] == 15)
               {
                  sentenceData[0] = 0;
                  JokeServer.out.println("JOKE CYCLE COMPLETED");
               }
               validSentenceFound = true;
            }
         }  
      }
      else
      {
         int proverbInt = sentenceData[1];
         boolean validSentenceFound = false;
         Random rand = new Random();
         
         while(!validSentenceFound)
         {
            sentencePosition = rand.nextInt(4);
            int bitValue = (int)Math.pow(2, sentencePosition);
            
            if((bitValue & proverbInt) == 0)
            {
               JokeServer.out.println("Proverb " + (char)('A' + sentencePosition) + " sent to " + username + clientID);
               sentenceData[1] += bitValue;
               if(sentenceData[1] == 15)
               {
                  sentenceData[1] = 0;
                  JokeServer.out.println("PROVERB CYCLE COMPLETED");
               }
               validSentenceFound = true;
            }
         }  
      }
      return sentencePosition;
   
   }
   
   private static String craftResponse(String clientUsername, int sentenceIndex)
   {
      String response = JokeServer.isPrimaryServer ? "" : "<S2> ";
      response += AdminLooper.isInJokeMode ? "J" : "P";
      response += (char)('A' + sentenceIndex) + " " + clientUsername + ": ";
      response += AdminLooper.isInJokeMode ? JokeServer.jokes[sentenceIndex] : JokeServer.proverbs[sentenceIndex];
      return response;
   }
}



class AdminLooper implements Runnable
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



class AdminWorker extends Thread
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

