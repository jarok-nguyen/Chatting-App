import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class Client extends JFrame implements ActionListener {
	
	private ExecutorService pool;
	static Socket clientSocket;
	static DataInputStream inFromServer;
	static DataOutputStream outToServer;
	private static String id;
	private JFrame mainWindow;
	private JPanel textMembersSend;
	private JButton sendButton;
	private JTextField textField;
	private static JComboBox<String> membersList;
	private static JTextArea feed;
	private JLabel welcomeText;
	private JPanel listAndRefresh;
	private JButton refreshButton;
	private JPanel welcomeAndQuit;
	private JButton quitButton;
	private JScrollPane scrollPane;
	private JButton clearButton;
	
	public Client() throws IOException
	{
    	super();
		//BufferedReader inFromUser=new BufferedReader(new InputStreamReader(System.in));
        //System.out.println("Enter your name to be identified with");
		id=(String) JOptionPane.showInputDialog(null, "Enter your name to be identified with", "Chatting App", JOptionPane.INFORMATION_MESSAGE, null, null, null);
        if(id==null)
        	return;
		if (id.isEmpty())
        {
    		JOptionPane.showMessageDialog(null, "Please enter name first before connecting");
    		return;
        }
		join(id);
        if(clientSocket!=null) 
        {
        	//System.out.println("If you want to get the memeber list type getMemberList."+'\n'+"Send MSG Format is: Name MSG");
        	pool = Executors.newFixedThreadPool(20);
        	pool.execute(new liveMsgRead(inFromServer));
        	mainWindow = new JFrame();
        	mainWindow.setLayout(new BorderLayout());
    		//mainWindow.setUndecorated(true);
    		mainWindow.setTitle("Chatting App");
    		//mainWindow.setDefaultCloseOperation(EXIT_ON_CLOSE);
    		mainWindow.setBounds(400, 0, 600, 600);
    		mainWindow.setBackground(Color.BLACK);
    		
    		textMembersSend = new JPanel();
    		sendButton = new JButton();sendButton.setText("Send");sendButton.setToolTipText("Send");sendButton.setActionCommand("send");sendButton.addActionListener(this);
    		sendButton.setPreferredSize(new Dimension(100, 50));
    		textField = new JTextField();
    		textField.setPreferredSize(new Dimension(600, 50));
    		listAndRefresh = new JPanel();
    		listAndRefresh.setLayout(new BorderLayout());
    		membersList = new JComboBox<>();
    		membersList=new JComboBox<>();
    		membersList.setToolTipText("Online Users");
    		membersList.addActionListener(this);
    		membersList.setPreferredSize(new Dimension(100, 25));
    		refreshButton = new JButton();refreshButton.setText("Refresh");refreshButton.setToolTipText("Refresh Online Users");refreshButton.setActionCommand("refresh");refreshButton.addActionListener(this);
    		refreshButton.setPreferredSize(new Dimension(100, 25));
    		listAndRefresh.add(membersList,BorderLayout.NORTH);
    		listAndRefresh.add(refreshButton,BorderLayout.SOUTH);
    		textMembersSend.add(textField);
    		textMembersSend.add(listAndRefresh);
    		textMembersSend.add(sendButton);
    		mainWindow.add(textMembersSend,BorderLayout.SOUTH);
    		
    		welcomeAndQuit = new JPanel();
    		welcomeAndQuit.setPreferredSize(new Dimension(800, 50));
    		welcomeText = new JLabel("Welcome "+id);
    		quitButton = new JButton();quitButton.setText("Disconnect");quitButton.setToolTipText("Disconnect");quitButton.setActionCommand("disconnect");quitButton.addActionListener(this);
    		clearButton = new JButton();clearButton.setText("Clear");clearButton.setToolTipText("Clear Chat");clearButton.setActionCommand("clear");clearButton.addActionListener(this);
    		welcomeAndQuit.add(welcomeText);
    		welcomeAndQuit.add(clearButton);
    		welcomeAndQuit.add(quitButton);
    		mainWindow.add(welcomeAndQuit, BorderLayout.NORTH);
    		
    		feed = new JTextArea();
    		feed.setEditable(false);
    		//feed.setPreferredSize(new Dimension(400,400));
    		//feed.setRows(100);
    		//feed.setColumns(100);
    		feed.setLineWrap(false);
    		scrollPane = new JScrollPane(feed);
    		scrollPane.setPreferredSize(new Dimension(400, 400));
    		scrollPane.setViewportView(feed);
    		mainWindow.add(scrollPane,BorderLayout.CENTER);
        	mainWindow.setResizable(false);
    		mainWindow.setVisible(true);
    		mainWindow.pack();
    		mainWindow.addWindowListener(new java.awt.event.WindowAdapter() 
    		{
    		    @Override
    		    public void windowClosing(java.awt.event.WindowEvent windowEvent) 
    		    {
    		        try {
						quit();
						System.exit(0);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    		    }
    		}
    		);
    		getMemberList();
        }
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException 
	{
		Client x = new Client();
    }
	public static class liveMsgRead implements Runnable
	{
		private DataInputStream inFromServer;
		public liveMsgRead (DataInputStream inFromServer) {
			this.inFromServer=inFromServer;
		}
		@Override
		public void run() {
			try
			{
				while(true)
				{
					String msg = inFromServer.readUTF();
					char c = msg.charAt(0);
					if(c=='*') 
					{
						String last = feed.getText();
						if(last.isEmpty())
							feed.setText(msg.substring(1));
						else
							feed.setText(last+"\n"+msg.substring(1));
					}
					else if(c=='#') 
					{
						inFromServer.close();
				        outToServer.close();
				        clientSocket.close();
						break;
					}
					else if(msg.equals("&"))
					{
						String last = feed.getText();
						if(last.isEmpty())
							feed.setText("TTL Exceeded");
						else
							feed.setText(last+"\n"+"TTL Exceeded");
					}
					else if(msg.equals("true")) 
					{
						String last = feed.getText();
						if(last.isEmpty())
							feed.setText("Message sent Successfully");
						else
							feed.setText(last+"\n"+"Message sent Successfully");
					}
					else if(msg.equals("false"))
					{
						String last = feed.getText();
						if(last.isEmpty())
							feed.setText("Entered username is not online or incorrect or it is yourself");
						else
							feed.setText(last+"\n"+"Entered username is not online or incorrect or it is yourself");
					}
					else
					{
						membersList.removeAllItems();
						ArrayList<String> x = new ArrayList<>(Arrays.asList(msg.split("\n")));
						for(int i=0;i<x.size();i++)
						{
							if(!x.get(i).equals("Server A") && !x.get(i).equals("Server B") && !x.get(i).equals(id))
							{
								membersList.addItem(x.get(i));
							}
						}
					}
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	public static void join(String name) throws IOException 
	{
		Socket socket = new Socket("Omar-PC", 9898);
		inFromServer = new DataInputStream(socket.getInputStream());
		outToServer = new DataOutputStream(socket.getOutputStream());
		outToServer.writeUTF(name);
		outToServer.flush();
		if(inFromServer.readUTF().equals("true"))
        {
            //System.out.println("I am connected at " + socket +" with unique name "+name+" at Server A");
			JOptionPane.showMessageDialog(null, "You are now connected at "+socket+" with Unique Name "+name+" at Server A"+"\n"+"Welcome!");
    		clientSocket=socket;
        }
        else 
        {
        	socket.close();
        	clientSocket=null;
        	inFromServer.close();
        	inFromServer=null;
        	outToServer.close();
        	outToServer=null;
        	Socket socket2 = new Socket("Omar-PC", 8989);
        	inFromServer = new DataInputStream(socket2.getInputStream());
    		outToServer = new DataOutputStream(socket2.getOutputStream());
    		outToServer.writeUTF(name);
    		outToServer.flush();
    		if(inFromServer.readUTF().equals("true"))
            {
                //System.out.println("I am connected at " + socket2 +" with unique name "+name+" at Server B");
                JOptionPane.showMessageDialog(null, "You are now connected at "+socket2+" with Unique Name "+name+" at Server B"+"\n"+"Welcome!");
        		clientSocket=socket2;
            }
    		else
    		{
    			socket2.close();
            	clientSocket=null;
            	inFromServer.close();
            	inFromServer=null;
            	outToServer.close();
            	outToServer=null;
    			//System.out.println("Name is already used");
        		JOptionPane.showMessageDialog(null, "Name is already used, Please reconnect with a different Name");
    		}
        }
	}
	public static void getMemberList() throws UnknownHostException, IOException
	{
		//System.out.println("Online Users");
		outToServer.writeUTF("getMemberList");
		outToServer.flush();
	}
	public static void quit() throws UnknownHostException, IOException 
	{
		outToServer.writeUTF("quit");
		outToServer.flush();
        //System.out.println("bye");
	}
	public static void chat (Socket Source, String Destination, int TTL, String Message) throws UnknownHostException, IOException 
	{
		outToServer.writeUTF(Destination+" "+TTL+Message);
		outToServer.flush();
	}
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getActionCommand().equals("send"))
		{
			if(!textField.getText().isEmpty())
    		{
        		String message = textField.getText();
        		String destination=(String) membersList.getSelectedItem();
        		if(destination ==null || destination.equals(id)) 
        		{
        			JOptionPane.showMessageDialog(null, "Please choose a user to send to first!");
        		}
        		else
        		{
        			try {
						chat(clientSocket,destination,1,message);
					} catch (UnknownHostException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
        		}
            }
    	}
		else if(e.getActionCommand().equals("refresh"))
		{
			try {
				getMemberList();
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else if(e.getActionCommand().equals("disconnect"))
		{
			try {
				quit();
				System.exit(0);
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			pool.shutdown();
		}
		else if(e.getActionCommand().equals("clear"))
		{
			feed.setText("");
		}
	}
}
