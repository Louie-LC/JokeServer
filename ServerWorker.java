import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.Random;

public class ServerWorker extends Thread
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