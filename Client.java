import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
public class Client{
    private Socket socket;
    private BufferedReader br;
    private String uname;
    private String ip;

    private ObjectInputStream ois;

    public Client(String ip, String uname){
	 try{
            this.ip = ip;
            socket = new Socket(ip, 9090);
            OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);

            InputStream is= socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);

            //br = new BufferedReader(new InputStreamReader(System.in));
            String line="";
            new Thread(()->{
                Packet p = new Packet(CommandType.MESSAGE);
                try{

                    while(p != null ||p.type==CommandType.MESSAGE){
                        p = (Packet)ois.readObject();
                        if(p.type==CommandType.MESSAGE){
                            //System.out.println(p.uname+": "+p.message);
                            VoipGUI.chat.append(p.uname + ": " + p.message);
                        }}
                }catch(Exception e){
                    System.err.println(e);}
            }).start();
            //UDP Sending
	    new Thread(()->{
                try{
                    byte[] buffer;
                    DatagramPacket packet;
                    MulticastSocket socket = new MulticastSocket(9092);
                    InetAddress address=  InetAddress.getByName("233.0.0.1");
                    socket.joinGroup(address);
                    while(true){
                        buffer= new byte[1024];
                        packet= new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        System.out.println(new String(packet.getData()));
                    }
                }catch(Exception e){System.out.println("Error "+e);}
            }).start();
	
	    	    new Thread(()->{
		    DatagramPacket packet; 
		    byte[] buffer;
		    InetAddress address;
		    DatagramSocket socket;
                    String testing[]={"This","is an example of a ", "udp","String being ","broadcasted "};
		    for(int i =0; i<testing.length;i++){
		    try{
			    socket= new DatagramSocket();
			    buffer=testing[i].getBytes();
		    address=InetAddress.getByName("233.0.0.1");
		    packet = new DatagramPacket(buffer, buffer.length, address, 9092);
		    socket.send(packet);		
		    }catch(IOException e){
		    }}
	    }).start();

	    
	 
	    VoipGUI.messageInput.addActionListener(e->{
		    try{
			    Packet p = new Packet(CommandType.MESSAGE);
			    p.uname=uname;

			    p.message=VoipGUI.messageInput.getText()+"\n\n";
			    oos.writeObject(p);

		    }
		    catch(IOException i){
			    System.out.println(i);
		    }
	    });

	 }

        catch(Exception i){
            System.out.println(i);
        }

    }
    public static void main(String args[]){
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter IP Username");
        StringTokenizer s = new StringTokenizer(sc.nextLine());
        Client client = new Client(s.nextToken(), s.nextToken());

    }
}
