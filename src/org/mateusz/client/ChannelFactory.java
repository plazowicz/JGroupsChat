package org.mateusz.client;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.ProtocolStack;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created with IntelliJ IDEA.
 * User: mateusz
 * Date: 5/24/13
 * Time: 9:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChannelFactory {

    private static ChannelFactory factory;

    public static ChannelFactory getChannelFactory() {
        if( factory == null ) {
            factory = new ChannelFactory();
        }
        return factory;
    }

    public JChannel getNewChannel(String addr,String nick) throws Exception {
        JChannel channel = new JChannel(false);
        ProtocolStack stack=new ProtocolStack(); // (2)

        channel.setProtocolStack(stack);
        channel.setName(nick);
        UDP udp = new UDP();
        if( addr != null )
            udp.setValue("mcast_group_addr", InetAddress.getByName(addr));
        stack.addProtocol(udp)
                .addProtocol(new PING())
                .addProtocol(new MERGE2())
                .addProtocol(new FD_SOCK())
                .addProtocol(new FD_ALL().setValue("timeout", 12000).setValue("interval", 3000))
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK())
                .addProtocol(new UNICAST2())
                .addProtocol(new STABLE())
                .addProtocol(new GMS())
                .addProtocol(new UFC())
                .addProtocol(new MFC())
                .addProtocol(new FRAG2())
                .addProtocol(new STATE_TRANSFER())
                .addProtocol(new FLUSH());
        stack.init();
        return channel;
    }
}
