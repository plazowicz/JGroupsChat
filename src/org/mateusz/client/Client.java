package org.mateusz.client;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: mateusz
 * Date: 5/23/13
 * Time: 9:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class Client extends ReceiverAdapter {

    JChannel channel;
    String userName = System.getProperty("user.name","n/a");

    private void start() throws Exception {
        System.out.println(userName);
        channel = new JChannel();
        channel.setReceiver(this);
        channel.connect("ChatCluster");
        eventLoop();
        channel.close();
    }

    private void eventLoop() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            System.out.print("> "); System.out.flush();

            String line= null;
            try {
                line = br.readLine().toLowerCase();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            if(line.startsWith("quit") || line.startsWith("exit"))

                break;

            line="[" + userName + "] " + line;

            Message msg=new Message(null, null, line);

            try {
                channel.send(msg);
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public void viewAccepted(View view) {
        System.out.println("** view: "+view);
    }

    public void receive(Message msg) {
        System.out.println(msg.getSrc()+":"+msg.getObject());
    }

    public static void main(String[] args) {
        try {
            new Client().start();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
