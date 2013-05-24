package org.mateusz.client;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
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

    private JChannel managementChannel;
    private ChannelFactory channelFactory;
    private String nick;
    private ChatOperationProtos.ChatState state;
    private Map<String,List<String>> cache;
    private Map<String,JChannel> channels = new HashMap<String,JChannel>();

    public Talker(String nick) {
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
        Runtime.getRuntime().addShutdownHook(new ShutDownHookThread());
        try {
            managementChannel.getState(null,1000);
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    public void createChannel(final String channelName) {
        Map<String,List<String>> cache = getCache();
        if( cache.keySet().contains(channelName)) {
            System.out.println("There is already channel: "+channelName);
        }
        else {
            if( createOrJoin(channelName))
                System.out.println("Create succeeded");
            else
                System.out.println("Create failed");
        }
    }

    public void joinChannel(final String channelName) {
       Map<String,List<String>> cache = getCache();
       if( !cache.keySet().contains(channelName)) {
            System.out.println("There is no channel: "+channelName);
       }
       else {
           if( createOrJoin(channelName) )
                System.out.println("Join succeeded");
           else
                System.out.println("Join failed");
       }
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
            try {
                channel.connect(channelName);
            } catch (Exception e) {
                logger.severe(e.getMessage());
            }
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
            List<String> members;
            cache = new HashMap<String, List<String>>();
            for(ChatOperationProtos.ChatAction ca : state.getStateList()) {
                  if( ca.getAction() == ChatOperationProtos.ChatAction.ActionType.JOIN ) {
                      if( cache.containsKey(ca.getChannel())) {
                          members = cache.get(ca.getChannel());
                          members.add(ca.getNickname());
                          cache.put(ca.getChannel(), members);
                      }
                      else {
                          members = new ArrayList<String>();
                          members.add(ca.getNickname());
                          cache.put(ca.getChannel(), members);
                      }
                  }
                  else {
                      members = cache.get(ca.getChannel());
                      if( members.size() == 0 )
                          cache.remove(ca.getChannel());
                      else {
                          members.remove(ca.getNickname());
                          cache.put(ca.getChannel(),members);
                      }
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

    public void leave(String channelName) {
        if( channels.containsKey(channelName )) {
            sendChatAction(channelName,nick, ChatOperationProtos.ChatAction.ActionType.LEAVE);
            channels.get(channelName).close();
            channels.remove(channelName);
        }
        else
            System.out.println("You cant live chat if you're not in");
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

        public void run() {
            System.out.println("Shut down hook executing...");
            for(String channelName : channels.keySet() ) {
                leave(channelName);
            }
        }

    }
}
