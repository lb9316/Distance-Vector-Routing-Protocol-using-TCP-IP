/**
 * Name: Lakhan Bhojwani
 * Subject: FCN
 * Project 1
 * 
 * Implementing Distance vector Routing using TCP/Ip protocol.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
 * This class takes the Ip from the client and then convert it to network prefix using subnet mask.
 * It stores the Ip, next hop, and the distance. 
 * 
 */
class node {

    String Ip;
    String SubnetMask = "255.255.255.0";
    String nextHop;
    int distance;

    node(String Ip, String nextHop, int distance) {

        String[] arr = Ip.split("\\.");

        String[] arr2 = SubnetMask.split("\\.");
        int ans[] = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            int x = ((Integer.parseInt(arr[i])) & (Integer.parseInt(arr2[i])));
            ans[i] = x;
        }
        String NetworkPrefix = ans[0] + "." + ans[1] + "." + ans[2] + "."
                + ans[3];

        this.Ip = NetworkPrefix;
        this.nextHop = nextHop;
        this.distance = distance;
    }
}

/*
 * This class works as the TCP - server of the router
 */
class ServerSide extends Thread {
    static String serverAddress;
    static int serverPort;
    static int proxyPort = 4089;
    static String proxyAddress;
    int counter;

    public ServerSide() {

    }

    /*
     * This function is a constructor and starts the thread for the server.
     */
    public ServerSide(String currentAddress, int serverConnectPort) {
        this.counter = 0;
        this.start();

    }

    @SuppressWarnings("unchecked")
    public void run() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(proxyPort);
            // creating the socket.
            Socket server;
            while (true) {

                server = serverSocket.accept();
                ObjectInputStream ois = new ObjectInputStream(
                        server.getInputStream());
                InetSocketAddress sockaddr = (InetSocketAddress) server
                        .getRemoteSocketAddress();
                InetAddress inaddr = sockaddr.getAddress();
                String client = inaddr.toString();
                client = client.substring(1, client.length());
                Map<String, Integer> comingToServer = new HashMap<String, Integer>();
                // geting the incoming HashMap
                comingToServer = (Map<String, Integer>) ois.readObject();

                // Updating the values.
                if (TCP.LocalTable.containsKey(client)) {
                    int cost = TCP.LocalTable.get(client);
                    Iterator it = comingToServer.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        int currentcost = (Integer) pair.getValue();

                        if ((Integer) pair.getValue() == Integer.MAX_VALUE) {
                            if (TCP.LocalTable.containsKey(pair.getKey())) {
                                node updatenode = TCP.DistanceRouting.get(pair
                                        .getKey());
                                updatenode.distance = Integer.MAX_VALUE;
                                updatenode.nextHop = "Failure Node";
                                TCP.DistanceRouting.put((String) pair.getKey(),
                                        updatenode);

                            }
                        }

                        if (TCP.LocalTable.containsKey(pair.getKey())
                                && (TCP.LocalTable.get(pair.getKey())) > (cost + currentcost)
                                && !pair.getKey().equals(
                                        InetAddress.getLocalHost()
                                                .getHostAddress())
                                && (Integer) pair.getValue() != Integer.MAX_VALUE) {
                            TCP.LocalTable.put((String) pair.getKey(), cost
                                    + currentcost);
                            node updatenode = TCP.DistanceRouting.get(pair
                                    .getKey());
                            updatenode.distance = cost + currentcost;
                            updatenode.nextHop = client;
                            TCP.DistanceRouting.put((String) pair.getKey(),
                                    updatenode);
                        }
                        if (TCP.LocalTable.containsKey(pair.getKey()) == false
                                && !pair.getKey().equals(
                                        InetAddress.getLocalHost()
                                                .getHostAddress())
                                && (Integer) pair.getValue() != Integer.MAX_VALUE) {
                            TCP.LocalTable.put((String) pair.getKey(), cost
                                    + currentcost);
                            node dvr = new node((String) pair.getKey(), client,
                                    cost + currentcost);
                            TCP.DistanceRouting
                                    .put((String) pair.getKey(), dvr);
                        }
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        } catch (IOException e) {

            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

/*
 * This class works as the client side of the router.
 */
class ClientSide extends Thread {

    String serverIp;
    int counter;
    int linkcost;

    public ClientSide() {
        // TODO Auto-generated constructor stub
    }

    public ClientSide(String ServerAddress, int linkcost) {
        this.serverIp = ServerAddress;

        this.linkcost = linkcost;
        this.start();
    }

    public ClientSide(String ip, int linkcost, int tt) {

        TCP.LocalTable.put(ip, linkcost);
    }

    public void run() {

        try {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            while (true) {

                Socket clientSocket = new Socket(serverIp, 4089);
                ObjectOutputStream outputforMap = new ObjectOutputStream(
                        clientSocket.getOutputStream());
                outputforMap.writeObject(TCP.LocalTable);
                outputforMap.reset();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        } catch (IOException e) {
            System.out.println(serverIp + " is not reachable");
        } finally {
            System.exit(0);

        }
    }

}

/*
 * This class prints the routing table
 */
class print extends Thread {

    print() {
        this.start();

    }

    public void run() {

        while (true) {

            if (TCP.DistanceRouting.size() != 0) {

                System.out
                        .println("Destination        Next Hop           Subnet mask        Distance");
                Iterator it = TCP.DistanceRouting.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    node dvr = (node) pair.getValue();
                    if (dvr.Ip.length() == 13) {
                        if (dvr.nextHop.length() == 13) {
                            System.out.println(dvr.Ip + "      " + dvr.nextHop
                                    + "      " + dvr.SubnetMask + "       "
                                    + dvr.distance);
                        } else {
                            System.out.println(dvr.Ip + "      " + dvr.nextHop
                                    + "       " + dvr.SubnetMask + "       "
                                    + dvr.distance);
                        }
                    } else {
                        if (dvr.nextHop.length() == 13) {
                            System.out.println(dvr.Ip + "       " + dvr.nextHop
                                    + "      " + dvr.SubnetMask + "       "
                                    + dvr.distance);
                        } else {
                            System.out.println(dvr.Ip + "       " + dvr.nextHop
                                    + "       " + dvr.SubnetMask + "       "
                                    + dvr.distance);
                        }

                    }

                }
                System.out
                        .println("-------------------------------------------------------------------------");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

/*
 * This class is the main class where user gives his inputs. And then call the
 * server and client.
 */
public class TCP {

    static Map<String, Integer> LocalTable = new HashMap<String, Integer>();
    static Map<String, node> DistanceRouting = new HashMap<String, node>();
    static int serverConnectPort;
    static int counterfor20 = 0;

    public static void main(String[] args) throws IOException,
            InterruptedException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String currentAddress = InetAddress.getLocalHost().getHostAddress();
        ServerSide proxy = null;
        proxy = new ServerSide(currentAddress, serverConnectPort);
        // If you are in glados.
        if (currentAddress.equals("129.21.22.196")) {

            System.out.println("You are in Glados.");
            int numberOfConnections;
            while (true) {
                System.out
                        .println("Enter the number of connections from here. Max is 2");
                numberOfConnections = Integer.parseInt(br.readLine());
                if (numberOfConnections > 0 && numberOfConnections < 3) {
                    break;
                } else {
                    System.out.println("Please Enter between either 1 or 2");
                }
            }
            for (int i = 0; i < numberOfConnections; i++) {
                System.out.println("Select");
                System.out.println("1. comet, 2. rhea, 3. queeg");
                int choice = Integer.parseInt(br.readLine());
                if (choice == 1) {
                    System.out.println("Connecting to comet");
                    System.out.println("Enter Distance");
                    int dist = Integer.parseInt(br.readLine());
                    LocalTable.put("129.21.34.80", dist);
                    node dvr = new node("129.21.34.80", "129.21.34.80", dist);
                    DistanceRouting.put("129.21.34.80", dvr);

                    ClientSide client = new ClientSide("129.21.34.80", dist);
                } else if (choice == 2) {
                    System.out.println("Connecting to rhea");
                    System.out.println("Enter Distance");
                    int dist = Integer.parseInt(br.readLine());
                    LocalTable.put("129.21.37.49", dist);
                    node dvr = new node("129.21.37.49", "129.21.37.49", dist);
                    DistanceRouting.put("129.21.37.49", dvr);
                    ClientSide client = new ClientSide("129.21.37.49", dist);
                } else if (choice == 3) {
                    System.out.println("Connecting to queeg");
                    System.out.println("Enter Distance");
                    int dist = Integer.parseInt(br.readLine());
                    LocalTable.put("129.21.30.37", dist);
                    node dvr = new node("129.21.30.37", "129.21.30.37", dist);
                    DistanceRouting.put("129.21.30.37", dvr);
                    ClientSide client = new ClientSide("129.21.30.37", dist);
                }
            }

        }
        // If you are in comet.
        else if (currentAddress.equals("129.21.34.80")) {

            System.out.println("You are in comet.");
            // System.out.println("Enter the number of connections from here. Max is 2");
            int numberOfConnections;
            while (true) {
                System.out
                        .println("Enter the number of connections from here. Max is 2");
                numberOfConnections = Integer.parseInt(br.readLine());
                if (numberOfConnections > 0 && numberOfConnections < 3) {
                    break;
                } else {
                    System.out.println("Please Enter between either 1 or 2");
                }
            }

            for (int i = 0; i < numberOfConnections; i++) {
                System.out.println("Select");
                System.out.println("1. glados, 2. rhea, 3. queeg");
                int choice = Integer.parseInt(br.readLine());
                if (choice == 1) {
                    System.out.println("Connecting to glados");
                    System.out.println("Enter Distance");
                    int dist = Integer.parseInt(br.readLine());
                    LocalTable.put("129.21.22.196", dist);
                    node dvr = new node("129.21.22.196", "129.21.22.196", dist);
                    DistanceRouting.put("129.21.22.196", dvr);
                    ClientSide client = new ClientSide("129.21.22.196", dist);
                }
                if (choice == 2) {
                    System.out.println("Connecting to rhea");
                    System.out.println("Enter Distance");
                    int dist = Integer.parseInt(br.readLine());
                    LocalTable.put("129.21.37.49", dist);
                    node dvr = new node("129.21.37.49", "129.21.37.49", dist);
                    DistanceRouting.put("129.21.37.49", dvr);
                    ClientSide client = new ClientSide("129.21.37.49", dist);
                }
                if (choice == 3) {
                    System.out.println("Connecting to queeg");
                    System.out.println("Enter Distance");
                    int dist = Integer.parseInt(br.readLine());
                    LocalTable.put("129.21.30.37", dist);
                    node dvr = new node("129.21.30.37", "129.21.30.37", dist);
                    DistanceRouting.put("129.21.30.37", dvr);
                    ClientSide client = new ClientSide("129.21.30.37", dist);
                }
            }

        }
        // If you are in Rhea.
        else if (currentAddress.equals("129.21.37.49")) {

            System.out.println("You are in Rhea.");
            // System.out.println("Enter the number of connections from here. Max is 2");
            int numberOfConnections;
            while (true) {
                System.out
                        .println("Enter the number of connections from here. Max is 2");
                numberOfConnections = Integer.parseInt(br.readLine());
                if (numberOfConnections > 0 && numberOfConnections < 3) {
                    break;
                } else {
                    System.out.println("Please Enter between either 1 or 2");
                }
            }

            for (int i = 0; i < numberOfConnections; i++) {
                System.out.println("Select");
                System.out.println("1. glados, 2. comet, 3. queeg");
                int choice = Integer.parseInt(br.readLine());
                if (choice == 1) {
                    System.out.println("Connecting to glados");
                    System.out.println("Enter Distance");
                    int dist = Integer.parseInt(br.readLine());
                    LocalTable.put("129.21.22.196", dist);
                    node dvr = new node("129.21.22.196", "129.21.22.196", dist);
                    DistanceRouting.put("129.21.22.196", dvr);
                    ClientSide client = new ClientSide("129.21.22.196", dist);
                }
                if (choice == 2) {
                    System.out.println("Connecting to comet");
                    System.out.println("Enter Distance");
                    int dist = Integer.parseInt(br.readLine());
                    LocalTable.put("129.21.34.80", dist);
                    node dvr = new node("129.21.34.80", "129.21.34.80", dist);
                    DistanceRouting.put("129.21.34.80", dvr);
                    ClientSide client = new ClientSide("129.21.34.80", dist);
                }
                if (choice == 3) {
                    System.out.println("Connecting to queeg");
                    System.out.println("Enter Distance");
                    int dist = Integer.parseInt(br.readLine());
                    LocalTable.put("129.21.30.37", dist);
                    node dvr = new node("129.21.30.37", "129.21.30.37", dist);
                    DistanceRouting.put("129.21.30.37", dvr);
                    ClientSide client = new ClientSide("129.21.30.37", dist);
                }
            }

        }
        // If you are in queeg.
        else if (currentAddress.equals("129.21.30.37")) {

            System.out.println("You are in queeg.");

            int numberOfConnections;
            while (true) {
                System.out
                        .println("Enter the number of connections from here. Max is 2");
                numberOfConnections = Integer.parseInt(br.readLine());
                if (numberOfConnections > 0 && numberOfConnections < 3) {
                    System.out.println("you here");
                    break;
                } else {
                    System.out.println("Please Enter between either 1 or 2");
                }
            }

            for (int i = 0; i < numberOfConnections; i++) {
                System.out.println("Select");
                System.out.println("1. glados, 2. comet, 3. rhea");
                int choice = Integer.parseInt(br.readLine());
                if (choice == 1) {
                    System.out.println("Connecting to glados");
                    System.out.println("Enter Distance");
                    int dist = Integer.parseInt(br.readLine());
                    LocalTable.put("129.21.22.196", dist);
                    node dvr = new node("129.21.22.196", "129.21.22.196", dist);
                    DistanceRouting.put("129.21.22.196", dvr);
                    ClientSide client = new ClientSide("129.21.22.196", dist);
                }
                if (choice == 2) {
                    System.out.println("Connecting to comet");
                    System.out.println("Enter Distance");
                    int dist = Integer.parseInt(br.readLine());
                    LocalTable.put("129.21.34.80", dist);
                    node dvr = new node("129.21.34.80", "129.21.34.80", dist);
                    DistanceRouting.put("129.21.34.80", dvr);
                    ClientSide client = new ClientSide("129.21.34.80", dist);
                }
                if (choice == 3) {
                    System.out.println("Connecting to queeg");
                    System.out.println("Enter Distance");
                    int dist = Integer.parseInt(br.readLine());
                    LocalTable.put("129.21.37.49", dist);
                    node dvr = new node("129.21.37.49", "129.21.37.49", dist);
                    DistanceRouting.put("129.21.37.49", dvr);
                    ClientSide client = new ClientSide("129.21.37.49", dist);
                }
            }

        }
        print printtable = new print();
    }

}
