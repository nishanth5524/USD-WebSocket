import javax.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

@ServerEndpoint("/end-point")
public class USDEndPoint {

	private static Queue<Session> queue = new ConcurrentLinkedQueue<Session>();
	private static Thread usdThread;

	static {
		usdThread = new Thread() {
			public void run() {
				DecimalFormat df = new DecimalFormat("#.####");
				while (true) {
					double d = 70 + Math.random();
					if (queue != null)
						sendAll("USD Rate: " + df.format(d));
					try {
						sleep(2000);
					} catch (InterruptedException e) {
					}
				}
			};
		};
		usdThread.start();
	}

	@OnMessage
	public void onMessage(Session session, String msg) {
		try {
			System.out.println("received msg " + msg + " from " + session.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@OnOpen
	public void open(Session session) {
		queue.add(session);
		System.out.println("New session opened: " + session.getId());
	}

	@OnError
	public void error(Session session, Throwable t) {
		queue.remove(session);
		System.err.println("Error on session " + session.getId());
	}

	@OnClose
	public void closedConnection(Session session) {
		queue.remove(session);
		System.out.println("session closed: " + session.getId());
	}

	private static void sendAll(String msg) {
		try {

			for (Session session : queue) {
				if (!session.isOpen()) {
					System.err.println("Closed session: " + session.getId());

				} else {
					session.getBasicRemote().sendText(msg);
				}
			}

			System.out.println("Sending " + msg + " to " + queue.size() + " clients");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
