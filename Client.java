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
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFileFormat;
public class Client{
	private Socket socket;
	private BufferedReader br;
	private String uname;
	private String ip;

	private ObjectInputStream ois;
	AudioCapture myCap = new AudioCapture();


	public Client(String ip, String uname, boolean term){
		try{
			this.ip = ip;
			socket = new Socket(ip, 9090);
			OutputStream os = socket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);

			InputStream is= socket.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(is);

			br = new BufferedReader(new InputStreamReader(System.in));
			new Thread(()->{
				Packet p = new Packet(CommandType.MESSAGE);
				try{

					while(p != null ||p.type==CommandType.MESSAGE){
						p = (Packet)ois.readObject();
						if(p.type==CommandType.MESSAGE){

							if(term)
								System.out.println(p.uname+": "+p.message);
							else
								VoipGUI.chat.append(p.uname + ": " + p.message);
						}
					}
				}catch(Exception e){
					System.err.println(e);}
			}).start();
			//UDP reseives data
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
							//if(!packet.getAddress().equals(InetAddress.getByName(getIP()))){
						System.out.println(new String(packet.getData()));
						//myCap.playAudio(packet.getData());
							}
					//}
				}catch(Exception e){System.out.println("Error "+e);}
			}).start();
//sends udp
			new Thread(()->{
				DatagramPacket packet;
				byte[] buffer;
				InetAddress address;
				DatagramSocket socket;
				String testing[]={"This","is an example of a ", "udp","String being ","broadcasted "};

				try{
					address=InetAddress.getByName("233.0.0.1");
					socket= new DatagramSocket();
					for(int i =0; i<testing.length;i++){
						buffer=testing[i].getBytes();

						packet = new DatagramPacket(buffer, buffer.length, address, 9092);
						socket.send(packet);
					}

					DatagramSocket s= new DatagramSocket();
				/*	myCap.start(t->{
						try{
							s.send(new DatagramPacket(t,t.length,address, 9092));}
						catch(Exception e){System.out.println(e);};});

				*/}catch(Exception e){
				}
			}).start();



			if(term){
				Scanner sc = new Scanner(System.in);
				while(true){
					try{

						Packet p = new Packet(CommandType.MESSAGE);
						p.uname=uname;
						p.message=sc.nextLine();
						oos.writeObject(p);
					}
					catch(IOException i){
						System.out.println(i);
					}
				}}
			else
				VoipGUI.messageInput.addActionListener(e->{
					try{
						Packet p = new Packet(CommandType.MESSAGE);
						p.uname=uname;

						p.message=VoipGUI.messageInput.getText()+"\n";
						oos.writeObject(p);

					}
					catch(IOException i){
						System.out.println(i);
					}
				});

            VoipGUI.disconnect.addActionListener(e->{
                try{
                    socket.close();
                    VoipGUI.chat.append("Connection Terminated");
                }
                catch(Exception i){
                    System.out.println(i);
                }
            });

			VoipGUI.record.addActionListener(e->{
				try{
					myCap.start("audioFiles/"+uname+".wav");
				}
				catch(Exception i){
					System.out.println(i);
				}
			});

			VoipGUI.stop.addActionListener(e->{
				try{
					myCap.finish();
				}
				catch(Exception i){
					System.out.println(i);
				}
			});
//sending audio
			VoipGUI.sendAudio.addActionListener(e->{
				try{
					byte b[] = new byte[1024];
					InetAddress address = InetAddress.getByName("233.0.0.2");
					//TODO still need to send it
					DatagramSocket socket= new DatagramSocket();
					FileInputStream file = new FileInputStream("audioCapture/"+uname+".wave");
					for(int i=0; file.available()!=0; i++){
						b[i]=(byte)file.read();
					}
					file.close();
					DatagramPacket packet = new DatagramPacket(b,b.length, address, 9093);
					socket.send(packet);
					socket.close();
				}
				catch(Exception i){
					System.out.println(i);
				}
			});
			// receives audio
			new Thread(()->{
				try{
					byte[] buffer;
					ByteArrayOutputStream output = new ByteArrayOutputStream();
					DatagramPacket packet;
					MulticastSocket socket = new MulticastSocket(9093);
					InetAddress address=  InetAddress.getByName("233.0.0.2");
					socket.joinGroup(address);
					Boolean flag = false;
					while(true){
						buffer= new byte[1024];
						packet= new DatagramPacket(buffer, buffer.length);
						socket.receive(packet);
						//packet.getData();
						if(packet.getLength()>0){
								output.write(packet.getData());
//							totalPacket.add(packet.getData());
							  flag = true;
	//						ByteArrayInputStream baiss = new ByteArrayInputStream(packet.getData());

			//
						}else{
						if(flag) {
							byte[] out = output.toByteArray();
							ByteArrayInputStream baiss = new ByteArrayInputStream(out);
							AudioInputStream ais = new AudioInputStream(baiss, myCap.getAudioFormat(), out.length);
							AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(address.getHostAddress()+".wav"));
							flag = false;
						}}
										}

				}catch(Exception e){System.out.println("Error "+e);}
			}).start();

		}

		catch(Exception i){
			System.out.println(i);
		}}
	public String getIP(){
		try{
			DatagramSocket socket = new DatagramSocket();
			socket.connect(InetAddress.getByName("8.8.8.8"),10002);
			return socket.getLocalAddress().getHostAddress();
		}catch(Exception e){
			System.err.println(e);
		}
		return null;}


	public static void main(String args[]){
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter IP Username");
		StringTokenizer s = new StringTokenizer(sc.nextLine());
		Client client = new Client(s.nextToken(), s.nextToken(),true);

	}
}
