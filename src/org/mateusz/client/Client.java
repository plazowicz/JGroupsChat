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

    public static void main(String[] args) {
        if( args.length != 1 ) {
            logger.info("Usage: java Talker NICK");
            System.exit(-1);
        }
        String nick = args[0];
        Talker t = new Talker(nick);
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
