import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
public class Client{
	private Socket socket;
	private BufferedReader br;
	private String uname;

	private ObjectInputStream ois;
	public Client(String ip, String uname){
		try{
			socket = new Socket(ip, 9090);
			OutputStream os = socket.getOutputStream();			
			ObjectOutputStream oos = new ObjectOutputStream(os);

			InputStream is= socket.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(is);

			br = new BufferedReader(new InputStreamReader(System.in));
			String line="";
			//gets name
			new Thread(()->{
				Packet p = new Packet(CommandType.MESSAGE);
				try{
					
					while(p != null ||p.type==CommandType.MESSAGE){
						p = (Packet)ois.readObject();
						if(p.type==CommandType.MESSAGE){
						System.out.println(p.uname+": "+p.message);

					}}
				}catch(Exception e){
					System.err.println(e);}
			}).start();

			while(!line.equals("q")){
				try{
					line=br.readLine();
					Packet p = new Packet(CommandType.MESSAGE);
					p.uname=uname;

					p.message=line;
					oos.writeObject(p);

				}
				catch(IOException i){
					System.out.println(i);
				}
			}
			socket.close();
		}
		catch(Exception i){
			System.out.println(i);
		}

	}
	public static void main(String args[]){
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter ip port username");
		StringTokenizer s = new StringTokenizer(sc.nextLine());
		Client client = new Client(s.nextToken(), s.nextToken());

	}
}
