import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
	public class UDPListener implements Runnable{
		int port;
		Server myServer;
		int len = 1024;
		public UDPListener(){
		}
		@Override
		public void run() {
			try {
				while (true){
					byte[] buf  = new byte[len];
					DatagramPacket datapacket = new DatagramPacket(buf, buf.length);
					udpSocket.receive(datapacket);
					String req = new String (datapacket.getData());
					String resp = Server.this.processRequest(req);
					byte[] respBytes = resp.getBytes();
					DatagramPacket returnpacket = new DatagramPacket (
							respBytes,
							respBytes.length,
							datapacket.getAddress () ,
							datapacket.getPort());
					udpSocket.send(returnpacket);
				}
				
			} catch (Exception exc){
				System.out.println("Exception: " + exc.getMessage());
			}
				
		}
	}

	private Map<String, String> bookMap; 
	private int tcpPort;
	DatagramSocket udpSocket;
	private static final int INVALID = -1;
	private final String RESERVE = "reserve";
	private final String RETURN = "return";
	private final String FAIL = "fail ";
	private final String AVAILABLE = "available";
	private final String FREE = "free ";
	public Server(){
		bookMap = new HashMap<String, String>();
		tcpPort = INVALID;
	}
	
	public void parseConfig(String config) {
		// input: config is a line formatted as: <z> <Pudp> <Ptcp>
		String[] configList = config.split(" ");
		int numBooks = Integer.parseInt(configList[0]);
		for (int i = 0; i < numBooks; ++i){
			bookMap.put("b" + i,  AVAILABLE);
		}
		
		int udpPort = Integer.parseInt(configList[1]);
		tcpPort = Integer.parseInt(configList[2]);
		try {
			udpSocket = new DatagramSocket(udpPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	public void listen(){
		UDPListener udp = new UDPListener();
		ExecutorService threadpool = Executors.newCachedThreadPool();
		threadpool.submit(udp);
		while (true);
	}
	public synchronized String processRequest(String request){
		// expects "<clientid> <bookid> <directive>"... e.g. "c1 b8 reserve"
		System.out.println(request);
		String[] requestSplit = request.split(" ");
		String client = requestSplit[0].trim();
		String book = requestSplit[1].trim();
		String directive = requestSplit[2].trim();
		System.out.println("Directive: " + directive);
		String status = bookMap.get(book);
		String prefix = "";
		if (status == null){
			// book not listed
			System.out.println("Book not listed.");
			prefix = FAIL;
		}
		else if (directive.equals(RETURN)){
			// did the client have the book?
			if (!status.equals(client)){
				System.out.println("Status was " + status + " but client was " + client);
				prefix = FAIL;
			} else {
				bookMap.put(book,  AVAILABLE);
				prefix = FREE;
			}
			
		} else {
			// is the book available?
			if (!status.equals(AVAILABLE)){
				System.out.println("Book was unavailable. Status: " + status);
				prefix = FAIL;
			} else {
				bookMap.put(book,  client);
				// prefix already blank, which is correct
			}
		}
		return prefix + client + " " + book;
	}

	public static void main(String[] args){
		Server s = new Server();
		Scanner sc = new Scanner(System.in);
		s.parseConfig(sc.nextLine());
		s.listen();
	}
}
