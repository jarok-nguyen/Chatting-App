# Chatting-App
Chatting Application with simple GUI to practice Socket Programming using Java.

The system consists of two chat servers connected together. Each chat server can handle multiple clients and is connected to the other server. Half the list of clients is maintained at each server.

There are 2 scenarios:

* Two users connected to the same server chatting with each other. In this case the message will be directed through their local chat server, no routing is needed.
* Two users connected to two different servers chatting with each other. In this case, the server of the initiator client must forward the message to the second chat server.
