# JokeServer
JokeServer is a project that was created for a Distributed Systems class. The project is broken up into three parts: a client, a server, and an administrator. The client connects to the server and the server sends back either a joke or a proverb. The server starts off sending only jokes. If the adminstrator is running, it can connect to the server which tells the server to toggle between joke and proverb mode. Multiple clients can be communicating with the server at the same time. The server remembers which client has seen which jokes/provebs so that it can ensure that no joke or proverb is repeated for a given client until all jokes/proverbs have been seen by that client.

## Running the Project
AdminLooper.java, AdminWorker.java, JokeClient.java, JokeClientAdmin.java, and JokeServer.java each have to be compiled using the java complier of your choice. Then, open up three seperate console windows and run the following commands, one in each window. The order you enter each command in doesn't matter.

    java JokeServer [secondary]
    java JokeClient [IPAddress] [IPAddress]
    java JokeClientAdmin [IPAddress] [IPAddress]

    

From the JokeClient console, hitting enter will cause the JokeClient to connect to the JokeServer and request a joke or a proverb. The JokeServer will lookup the client information and send the appropriate response back.
From the JokeClientAdmin console, hitting enter will connect the JokeClientAdmin to the JokeServer and toggle the server between sending jokes and sending proverbs.

Multiple JokeClients can be started in seperate console windows and each one will have its own sessions which is tracked by the JokeServer.

A second JokeServer can be started in a seperate window, and the JokeClients and JokeClientAdmin will be able to switch between the main JokeServer and the secondary JokeServer. To start a secondary JokeServer, the word "secondary" should be passed as an argument to the JokeServer. Additionaly, the JokeClinet and JokeClientAdmin will each need to have the IP adrresses of the primary and secondary server supplied as arguments so that these processes know that a secondary server exists. During testing of the project, JokeClient and JokClient admin were always run on the same machine as JokeServer making it so that "localhost" was used for each IP address.

Running a secondary JokeServer works similarly to running a single server. The main difference is that from the JokeClient or JokeClientAdmin console, an 'S' can be entered to cause the process to switch to talking to a different server. Each server has it's own Joke and Proverb mode, and each JokeServer is keeping track of its own set of users as well as each conversation that server is having with that user.
