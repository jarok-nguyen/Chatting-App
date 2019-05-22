import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerA
{
	private static ArrayList<String> members = new ArrayList<>();
	private static ArrayList<Socket> membersSocket = new ArrayList<>();
	private static String clientName;
	private static Socket clientSocket;
	private static DataInputStream inFromClient;
	private static DataOutputStream outToClient;
	private static ArrayList<DataInputStream> inFromClientList = new ArrayList<>();
	private static ArrayList<DataOutputStream> outToClientList =  new ArrayList<>();
	//private static DataInputStream inFromServer;
	//private static DataOutputStream outToServer;
	private static int clientNumber;
	private static ArrayList<String> otherServerMembers;
		
	public static void main(String[] args) throws IOException 
	{
        ExecutorService pool = Executors.newFixedThreadPool(20);
        clientNumber = 0;
        try (ServerSocket listener = new ServerSocket(9898))
        {
    		System.out.println("Server A is running.");
            while (true) 
            {
            	clientSocket = listener.accept();
            	if(joinResponse()) 
            	{
                    pool.execute(new Thread(clientSocket,clientNumber++,clientName,inFromClient,outToClient));
            	}
            }
        }
	}
	
	public static ArrayList<String> otherServerMembers() throws UnknownHostException, IOException
	{
		Socket socket = new Socket("Omar-PC", 8989);
		DataInputStream inFromServer=new DataInputStream(socket.getInputStream());
		DataOutputStream outToServer= new DataOutputStream(socket.getOutputStream());
		outToServer.writeUTF("Server");
		outToServer.flush();
		outToServer.writeUTF("#");
		outToServer.flush();
		ArrayList<String> x = new ArrayList<>(Arrays.asList(inFromServer.readUTF().split("\n")));
		socket.close();
		inFromServer.close();
		inFromServer=null;
		outToServer.close();
		outToServer=null;
		return x;
	}
		
	private static boolean joinResponse () throws IOException 
	{
		inFromClient = new DataInputStream(clientSocket.getInputStream());
		outToClient = new DataOutputStream(clientSocket.getOutputStream());
		//System.out.println(inFromClient+"	BB");
		//System.out.println(outToClient+"	BB");
		String id = inFromClient.readUTF();
    	clientName=id;
    	if(id.equals("Server"))
    	{
    		return true;
    	}
    	otherServerMembers=otherServerMembers();
    	int size;
    	if(otherServerMembers.get(0).equals("empty"))
    		size=0;
    	else
    		size=otherServerMembers.size();
    	if(members.contains(id) || otherServerMembers.contains(id) || members.size()>size) 
    	{
    		outToClient.writeUTF("false");
    		outToClient.flush();
    		clientSocket.close();
    		inFromClient.close();
    		outToClient.close();
    		clientName=null;
    		clientSocket=null;
    		inFromClient=null;
    		outToClient=null;
    		return false;
    	}
    	else 
    	{
    		members.add(id);
    		membersSocket.add(clientSocket);
    		inFromClientList.add(inFromClient);
    		outToClientList.add(outToClient);
    		outToClient.writeUTF("true");
    		outToClient.flush();
    		return true;
    	}
	}
		
	private static class Thread implements Runnable 
	{
        private Socket socket;
        private int clientNumber;
        private String clientName;
        private DataInputStream inFromClient;
        private DataOutputStream outToClient;

        public Thread(Socket socket, int clientNumber, String clientName, DataInputStream inFromClient, DataOutputStream outToClient) 
        {
            this.socket = socket;
            this.clientNumber = clientNumber;
            this.clientName=clientName;
            this.inFromClient=inFromClient;
            this.outToClient=outToClient;
            if(clientName.equals("Server"))
            {
            	//System.out.println("New Server Client #" + "A" + " connected at " + socket + " to Server B");
            	clientNumber--;
            }
            else
            	System.out.println("New client #" + clientNumber + " connected at " + socket + " to Server A");
        }
        
        public void serverMembersResponse() throws IOException
        {
    		if(members.size()==0)
        	{
        		outToClient.writeUTF("empty"+"\n");
	            outToClient.flush();
        	}
        	else
        	{
	        	String s="";
	            for(int i=0;i<members.size();i++) {
	            	s+=members.get(i)+"\n";
	            }
	            outToClient.writeUTF(s);
	            outToClient.flush();
        	}
        }
        
        public void memberListResponse() throws IOException, InterruptedException
        {
        	String s="Server A"+"\n";
            for(int i=0;i<members.size();i++) {
            	s+=members.get(i)+"\n";
            }
            otherServerMembers=otherServerMembers();
	    	if(!otherServerMembers.get(0).equals("empty"))
	    	{
	    		s+="Server B"+"\n";
	    		for(int i=0;i<otherServerMembers.size();i++)
	    		{
	    			s+=otherServerMembers.get(i)+"\n";
	    		}
	    	}
            outToClient.writeUTF(s);
            outToClient.flush(); 	    	
    	}
        
        public void route(String Destination, String Message) throws IOException
        {
        	boolean founded = false;
        	boolean sameServer = false;
        	String []x=new String[2];
        	x=Destination.split(" ",2);
        	///////////////////////////////////////////////////////////
        	int ttl = Character.getNumericValue(Message.charAt(0));
        	ttl--;
        	if(ttl<0)
        	{
        		outToClient.writeUTF("&");
				outToClient.flush();
				return;
        	}
        	////////////////////////////////////////////////////////
        	for(int i=0;i<members.size();i++) 
        	{
        		if(members.get(i).equals(x[0]))
        		{
        			founded=true;
        			sameServer=true;
        			break;
        		}
        	}
        	if(!sameServer)
        	{
        		otherServerMembers=otherServerMembers();
        		for(int i=0;i<otherServerMembers.size();i++) 
            	{
            		if(otherServerMembers.get(i).equals(x[0]))
            		{
            			founded=true;
            			sameServer=false;
            			break;
            		}
            	}
        	}
			if(!founded) 
			{
				outToClient.writeUTF("false");
				outToClient.flush();
			}
			else 
			{
				if(sameServer)
				{
					DataOutputStream destinationStream = null;
        			for(int i=0;i<members.size();i++) 
                	{
                		if(members.get(i).equals(x[0]))
                		{
                			destinationStream=outToClientList.get(i);
                			break;
                		}
                	}
        			if(clientName.equals("Server"))
        			{
        				destinationStream.writeUTF("*From "+x[1]+" to You: "+Message.substring(1)); ////////////////////
        				destinationStream.flush();
        			}
        			else
        			{
        				destinationStream.writeUTF("*From "+clientName+" to You: "+Message.substring(1)); ////////////////
                        destinationStream.flush();
        			}
                    outToClient.writeUTF("true");
                    outToClient.flush();
				}
				else
				{
					Socket socket = new Socket("Omar-PC", 8989);
					DataInputStream inFromServer=new DataInputStream(socket.getInputStream());
					DataOutputStream outToServer= new DataOutputStream(socket.getOutputStream());
					outToServer.writeUTF("Server");
					outToServer.flush();
					outToServer.writeUTF(Destination+" "+clientName+" "+ttl+Message.substring(1)); ////////////////////////////
					outToServer.flush();
					String in = inFromServer.readUTF();
					if(in.equals("true"))
					{
						outToClient.writeUTF("true");
						outToClient.flush();
					}
					else if(in.equals("&"))
					{
						outToClient.writeUTF("&");
						outToClient.flush();
					}
					else
					{
						outToClient.writeUTF("false");
						outToClient.flush();
					}
					socket.close();
				}
				
			}
        }

        public void run() 
        {
            try 
            {
            	while(true) 
            	{
        			String sentence = inFromClient.readUTF();
	                if(sentence !=null)
	                {
	                	if (sentence.equals("quit")) 
		                {
		                	break;
		    			}
		                else if(sentence.equals("getMemberList")) 
		                {
		                	memberListResponse();
		                }
		                else if(sentence.equals("#"))
		                {
		                	serverMembersResponse();
		                }
		                else
		                {
		                	if(clientName.equals("Server"))
		                	{
		                		String x[]=new String[3];
		                		x=sentence.split(" ",3);
		                		route(x[0]+" "+x[1],x[2]);
		                	}
		                	else
		                	{
		                		String x[]=new String[2];
			            		x=sentence.split(" ",2);
			            		if(x[0].equals(clientName))
			            		{
			            			outToClient.writeUTF("false");
			            			outToClient.flush();
			            			continue;
			            		}
			            		route(x[0],x[1]);
		                	}
		                }
	                }
	                if(clientName.equals("Server"))
	                {
	                	break;
	                	/*socket.close();
	                	inFromServer.close();
	            		inFromServer=null;
	            		outToServer.close();
	            		outToServer=null;
	            		*/
	                }
        		}
            } 
            catch (Exception e) 
            {
            	System.out.println(e.getMessage());
            	e.printStackTrace();
                System.out.println("Error handling client #" + clientNumber);
            } 
            finally
            {
                try 
                {
                	for(int i=0;i<members.size();i++) 
                	{
                		if(members.get(i).equals(clientName)) 
                		{
                			outToClient.writeUTF("#");
                			outToClient.flush();
                        	members.remove(i);
                        	membersSocket.remove(i);
                        	outToClientList.remove(i);
                        	inFromClientList.remove(i);
                        	inFromClient.close();
                        	outToClient.close();
                			break;
                		}
                	}
                	socket.close(); 
                } 
                catch (IOException e) {
                	
                }
                if(!clientName.equals("Server"))
                	//System.out.println("Connection with client # " + clientNumber + " closed");
                clientName=null;
            }
        }
    }
}