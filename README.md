# JokeServer
JokeServer is a project that was created for a Distributed Systems class. The project is broken up into three parts: a client, a server, and an administrator. The client connects to the server and the server sends back either a joke or a proverb. The server starts off sending only jokes. If the adminstrator is running, it can connect to the server which tells the server to toggle between joke and proverb mode. Multiple clients can be communicating with the server at the same time. The server remembers which client has seen which jokes/provebs so that it can ensure that no joke or proverb is repeated for a given client until all jokes/proverbs have been seen by that client.

    function fancyAlert(arg) {
      if(arg) {
        $.facebox({div:'#foo'})
      }
    }
