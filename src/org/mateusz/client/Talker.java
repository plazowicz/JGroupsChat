package org.mateusz.client;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.mateusz.chat.protos.ChatOperationProtos;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: mateusz
 * Date: 5/24/13
 * Time: 8:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class Talker extends ReceiverAdapter {

    public static final Logger logger = Logger.getLogger(Talker.class.getSimpleName());

    private String multicast;
    private JChannel managementChannel;
    private ChannelFactory channelFactory;
    private String nick;
    private ChatOperationProtos.ChatState state;
    private Map<String,List<String>> cache;
    private Map<String,JChannel> channels = new HashMap<String,JChannel>();

    public Talker(String multicast, String nick) {
        this.multicast = multicast;
        this.nick = nick;
        cache = new HashMap<String, List<String>>();
        state = ChatOperationProtos.ChatState.newBuilder().build();
        channelFactory = ChannelFactory.getChannelFactory();
        try {
            managementChannel = channelFactory.getNewChannel(null,nick);
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
        managementChannel.setReceiver(this);
        try {
            managementChannel.connect("ChatManagement768264");
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
        addShutDownHook(managementChannel);
        try {
            managementChannel.getState(null,10000);
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    public void createChannel(final String channelName) {
        Map<String,List<String>> cache = getCache();
        if( cache.keySet().contains(channelName)) {
            System.out.println("There is already channel: "+channelName);
        }
        if( createOrJoin(channelName))
            System.out.println("Create succeeded");
        else
            System.out.println("Create failed");

    }

    public void joinChannel(final String channelName) {
       Map<String,List<String>> cache = getCache();
       if( !cache.keySet().contains(channelName))
            System.out.println("There is no channel: "+channelName);
        if( createOrJoin(channelName) )
            System.out.println("Join succeeded");
        else
            System.out.println("Join failed");

    }

    private boolean createOrJoin(final String channelName) {
        JChannel channel = null;
        try {
            channel = channelFactory.getNewChannel(channelName,nick);
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
        if( channel != null ) {
            channel.setReceiver(new ReceiverAdapter() {
                public void receive(Message msg) {
                    try {
                        ChatOperationProtos.ChatMessage chatMsg = ChatOperationProtos.ChatMessage.parseFrom(msg.getRawBuffer());
                        System.out.println("$"+msg.getSrc().toString()+"<["+channelName+"]"+" "+chatMsg.getMessage());
                    } catch (InvalidProtocolBufferException e) {
                        logger.severe(e.getMessage());
                    }
                }
            });
            channel.setName(nick);
            sendChatAction(channelName,nick, ChatOperationProtos.ChatAction.ActionType.JOIN);
            channels.put(channelName, channel);
            return true;
        }
        else {
            return false;
        }
    }

    public void listChannels() {
        Map<String,List<String>> cache = getCache();
        for(String channelName : cache.keySet()) {
            System.out.println("Channel name: "+channelName+". Members: ");
            for(String member : cache.get(channelName)) {
                System.out.println(cache.get(channelName).indexOf(member)+") "+member);
            }
            System.out.println();
        }
    }

    private Map<String,List<String>> getCache() {
        if( cache == null )
            updateCache();
        return cache;
    }

    private void updateCache() {
        synchronized (state) {
            cache = new HashMap<String, List<String>>();
            for(ChatOperationProtos.ChatAction ca : state.getStateList()) {
                  if( cache.containsKey(ca.getChannel())) {
                      List<String> members = cache.get(ca.getChannel());
                      members.add(ca.getNickname());
                      cache.put(ca.getChannel(), members);
                  }
                  else {
                      cache.put(ca.getChannel(), Arrays.asList(ca.getNickname()));
                  }
            }
        }
    }

    public void sendMessage(String channelName, String message) {
        ChatOperationProtos.ChatMessage msg = ChatOperationProtos.ChatMessage.newBuilder().setMessage(message).build();
        JChannel channel = channels.get(channelName);
        try {
            channel.send(new Message(null,null,msg.toByteArray()));
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }

    }

    public void leave(String channelName, String nick) {
        sendChatAction(channelName,nick, ChatOperationProtos.ChatAction.ActionType.LEAVE);
        channels.remove(channelName);
    }

    private void sendChatAction(String channelName, String nick, ChatOperationProtos.ChatAction.ActionType type) {
        ChatOperationProtos.ChatAction action = ChatOperationProtos.ChatAction.newBuilder().
                setAction(type).
                setChannel(channelName).
                setNickname(nick).build();
        Message message = new Message(null,null,action.toByteArray());
        try {
            managementChannel.send(message);
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    public void addShutDownHook(JChannel channel) {
        Runtime.getRuntime().addShutdownHook(new ShutDownHookThread(channel));
    }

    public void receive(Message message) {
        synchronized (state) {
            ChatOperationProtos.ChatAction action = null;
            try {
                action = ChatOperationProtos.ChatAction.parseFrom(message.getRawBuffer());
            } catch (InvalidProtocolBufferException e) {
                logger.severe(e.getMessage());
            }
            state = ChatOperationProtos.ChatState.newBuilder(state).addState(action).build();
            cache = null;
        }
    }


    public void getState(OutputStream output) throws Exception {
        synchronized (state) {
            output.write(state.toByteArray());
        }
    }

    public void setState(InputStream input) throws Exception {
        synchronized (state) {
            state = ChatOperationProtos.ChatState.parseFrom(input);
        }
    }



    private class ShutDownHookThread extends Thread {

        private JChannel channel;

        public ShutDownHookThread(JChannel channel) {
            this.channel = channel;
        }

        public void run() {
            channel.close();
        }

    }
}
