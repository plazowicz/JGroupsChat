package org.mateusz.client;

import org.jgroups.ReceiverAdapter;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: mateusz
 * Date: 5/23/13
 * Time: 9:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class Client extends ReceiverAdapter {


    public static final Logger logger = Logger.getLogger(Client.class.getSimpleName());

//    JChannel channel;
//    String userName = System.getProperty("user.name","n/a");
//
//    private void start() throws Exception {
//        System.out.println(userName);
//        channel = new JChannel();
//        ProtocolStack stack=new ProtocolStack(); // (2)
//
//        channel.setProtocolStack(stack);
//        channel.setName(userName);
//
//        stack.addProtocol(new UDP().setValue("mcast_group_addr",InetAddress.getByName("224.0.0.251")))
//                .addProtocol(new PING())
//                .addProtocol(new MERGE2())
//                .addProtocol(new FD_SOCK())
//                .addProtocol(new FD_ALL().setValue("timeout", 12000).setValue("interval", 3000))
//                .addProtocol(new VERIFY_SUSPECT())
//                .addProtocol(new BARRIER())
//                .addProtocol(new NAKACK())
//                .addProtocol(new UNICAST2())
//                .addProtocol(new STABLE())
//                .addProtocol(new GMS())
//                .addProtocol(new UFC())
//                .addProtocol(new MFC())
//                .addProtocol(new FRAG2())
//                .addProtocol(new STATE_TRANSFER())
//                .addProtocol(new FLUSH());
//        stack.init();
//
//        channel.setReceiver(this);
//        channel.connect("ChatCluster");
//        eventLoop();
//        channel.close();
//    }
//
//    private void eventLoop() {
//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//        while(true) {
//            System.out.print("> "); System.out.flush();
//
//            String line= null;
//            try {
//                line = br.readLine().toLowerCase();
//            } catch (IOException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
//
//            if(line.startsWith("quit") || line.startsWith("exit"))
//
//                break;
//
//            line="[" + userName + "] " + line;
//
//            Message msg=new Message(null, null, line);
//
//            try {
//                channel.send(msg);
//            } catch (Exception e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
//        }
//    }
//
//    public void viewAccepted(View view) {
//        System.out.println("** view: "+view);
//    }
//
//    public void receive(Message msg) {
//        System.out.println(msg.getSrc()+":"+msg.getObject());
//    }

    public static void main(String[] args) {
        if( args.length != 2 ) {
            logger.info("Usage: java Talker MULTICAST_ADDRESS NICK");
            System.exit(-1);
        }
        String multicast = args[0];
        String nick = args[1];
        Talker t = new Talker(multicast,nick);
        Scanner in = new Scanner(System.in);
        boolean running = true;
        String channelName;
        while( running ) {
            System.out.println("Please choose one of options: (c)reate channel, " +
                    "(j)oin channel, (l)ist available channels with members,(s)end message, lea(v)e channel or (q)uit");
            try {
                switch (System.in.read()) {
                    case 'c':
                        System.in.read();
                        System.out.println("Please give channel name");
                        channelName = in.nextLine();
                        t.createChannel(channelName);
                        break;
                    case 'j':
                        System.in.read();
                        System.out.println("Please give channel name");
                        channelName = in.nextLine();
                        t.joinChannel(channelName);
                        break;
                    case 'l':
                        System.in.read();
                        t.listChannels();
                        break;
                    case 's':
                        System.in.read();
                        System.out.println("Please give channel name");
                        channelName = in.nextLine();
                        System.out.println("Please give message content");
                        String msg = in.nextLine();
                        t.sendMessage(channelName,msg);
                        break;
                    case 'v':
                        System.in.read();
                        System.out.println("Please give channel name");
                        channelName = in.nextLine();
                        t.leave(channelName);
                        break;
                    case 'q':
                        System.out.println("Leaving chat...");
                        System.exit(0);
                    default:
                        System.out.println("Wrong option!");
                        break;
                }
            } catch (IOException e) {
                logger.severe(e.getMessage());
            }

        }
    }
}
