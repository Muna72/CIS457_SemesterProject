import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.ObjectOutputStream;
import java.util.stream.Stream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
public class Server{
	private Socket socket;
	private ServerSocket server;
	public static ArrayList<ClientHandler> handler = new ArrayList<ClientHandler>();
	public Server(){
		try{
			server = new ServerSocket(9090);
			System.out.println("Running the Central Server on  9090");
		}catch(IOException i){
			System.out.println(i);
		}
		while(true){
			try{
				socket=server.accept();
				handler.add(new ClientHandler(socket));
			}	catch(IOException i){
				System.out.println(i);
			}
		}
	}
	public static void main(String args[]){
		Server server = new Server();
	}
}
class ClientHandler{
	private Socket socket;
	public String uname;
	private Packet packet;
	private ObjectOutputStream oos;
	public ClientHandler(Socket socket){
		try{
			DatagramSocket ds =new DatagramSocket(9091);
		new Thread(()->{
			try {
				InputStream is=socket.getInputStream();
				ObjectInputStream ois= new ObjectInputStream(is);

				OutputStream os = socket.getOutputStream();
				oos = new ObjectOutputStream(os);

				String line ="";
				while(!line.equals("q")){
					Packet p =  (Packet)ois.readObject();
					if(p.type==CommandType.MESSAGE){
						System.out.println(p.uname+": "+p.message);
						Server.handler.forEach(t->{
							try{t.oos.writeObject(p);}catch(IOException e){System.err.println(e);};
						});
					}

				}

			}catch(Exception i){System.out.println(i);};
		}).start();
		//UDP
		new Thread(()->{
			try{
				DatagramSocket s= new DatagramSocket();
				byte[] receive=new byte[256];
				DatagramPacket dpReceive = null;
				DatagramPacket dpSend =null;
				InetAddress address = InetAddress.getByName("233.0.0.1");
				while(true){
					dpReceive = new DatagramPacket(receive, receive.length);
					ds.receive(dpReceive);
					System.out.println(receive.length);
					dpSend=new DatagramPacket(receive,receive.length,address, 9092);

					s.send(dpSend);
					System.out.println("Client : -"+ new String(receive));
					receive= new byte[256];
				}
			}
			catch(Exception e){
				System.err.println(e);}
		}).start();

		}catch(IOException e){}
	}
}
