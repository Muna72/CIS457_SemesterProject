import java.net.*;
import java.io.*;
import java.util.Arrays;
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
import java.io.File;
import java.nio.file.Files;
import java.io.FileOutputStream;
public class Client{
	private Socket socket;
	private BufferedReader br;
	private String uname;
	private String ip;

	private Thread startRecording;
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
						System.out.println(new String(packet.getData()));
							}
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
				}catch(Exception e){
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
					startRecording = new Thread(()->{
						AudioCapture myCap = new AudioCapture();
						try{
						myCap.start("audioCapture/"+InetAddress.getLocalHost().getHostName()+".wav");
						}catch(IOException x){}
						});
					startRecording.start();
				}
				catch(Exception i){
					System.out.println(i);
				}
			});

			VoipGUI.stop.addActionListener(e->{
				try{
				
					myCap.finish();
					startRecording.interrupt();
				}
				catch(Exception i){
					System.out.println(i);
				}
			});
//sending audio
			VoipGUI.sendAudio.addActionListener(e->{
				
				System.out.println("166");
				try{
				DatagramSocket socket = new DatagramSocket();
				InetAddress address = InetAddress.getByName("233.0.0.2");
				byte[] array = Files.readAllBytes(new File("audioCapture/"+InetAddress.getLocalHost().getHostName()+".wav").toPath());
				byte twoArray[][]=chunkArray(array, array.length/1024); 
				DatagramPacket packet;
				System.out.println("173");
				for(int i =0; i<twoArray.length;i++){
					packet = new DatagramPacket(twoArray[i],twoArray[i].length,address, 9093);
					System.out.println(packet.getData());
				System.out.println("177"+i+","+twoArray.length);
					socket.send(packet);
				} 
					socket.close();
				}
				catch(Exception i){
					System.out.println(i+"ERROR");
				}
			});

			// receives audio
			new Thread(()->{
				System.out.println("189");
				try{
					
					InetAddress address=  InetAddress.getByName("233.0.0.2");
					byte[] buffer = new byte[1024];
				System.out.println("195");
					ByteArrayOutputStream output = new ByteArrayOutputStream();
					File file= new File(InetAddress.getLocalHost().getHostName()+".wav");
					if(!file.exists()){
					file.createNewFile();}
					FileOutputStream ostream = new FileOutputStream(file);
					InputStream inputfile = new FileInputStream(file);
					DatagramPacket packet;
					MulticastSocket socket = new MulticastSocket(9093);
					socket.joinGroup(address);
					while(true){
				System.out.print("204");
					packet = new DatagramPacket(buffer,buffer.length);
					socket.receive(packet);
					System.out.println(packet.getData()+"data");
					ostream.write(packet.getData());
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

	public static byte[][] chunkArray(byte[] array, int chunkSize) {
        int numOfChunks = (int)Math.ceil((double)array.length / chunkSize);
        byte[][] output = new byte[numOfChunks][];

        for(int i = 0; i < numOfChunks; ++i) {
            int start = i * chunkSize;
            int length = Math.min(array.length - start, chunkSize);

            byte[] temp = new byte[length];
            System.arraycopy(array, start, temp, 0, length);
            output[i] = temp;
        }

        return output;
    }


	public static void main(String args[]){
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter IP Username");
		StringTokenizer s = new StringTokenizer(sc.nextLine());
		Client client = new Client(s.nextToken(), s.nextToken(),true);

	}
}
