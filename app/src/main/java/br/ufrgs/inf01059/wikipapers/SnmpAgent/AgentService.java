package br.ufrgs.inf01059.wikipapers.SnmpAgent;


import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import br.ufrgs.inf01059.wikipapers.SnmpAgent.MIBTree;
import org.snmp4j.*;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.transport.TransportMappings;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class AgentService extends Service implements CommandResponder {

    /** Keeps track of all current registered clients. */
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    /** Holds last value set by a client. */
    int mValue = 0;

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SET_VALUE = 3;
    public static final int MSG_SNMP_REQUEST_RECEIVED = 4;
    public static final int MSN_SEND_DANGER_TRAP = 5;
    public static final int MSG_MANAGER_MESSAGE_RECEIVED = 6;

    public static String lastRequestReceived = "";

    private Snmp snmp;
    private static final int SNMP_PORT = 32150;

    //private MIBTree MIB_MAP;
    private Timer timer;

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SET_VALUE:
                    mValue = msg.arg1;
                    sendMessageToClients(MSG_SET_VALUE);
                    break;
                case MSN_SEND_DANGER_TRAP:
                    //new SendTrap().execute();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void sendMessageToClients(int msgCode) {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                mClients.get(i).send(Message.obtain(null,
                        msgCode, 0, 0));
            } catch (RemoteException e) {
                // The client is dead.  Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }


    @Override
    public void onCreate() {
        //timer = new Timer();
        //timer.scheduleAtFixedRate(new RefreshMIBData(), 0, 50000);
        new AgentListener().start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        try {
            snmp.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class AgentListener extends Thread {
        public void run() {
            try {
                initSnmp();
                snmp.listen();
                Log.i("LocalService", "Listening on" + snmp.toString());
            }  catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void initSnmp(){
            try {
                Address targetAddress = GenericAddress.parse("tcp:0.0.0.0/9999");
                TransportMapping tm = TransportMappings.getInstance().createTransportMapping(targetAddress);
                //TransportMapping transport = new DefaultTcpTransportMapping();
                snmp = new Snmp(tm);
                SecurityProtocols.getInstance().addDefaultProtocols();
                MessageDispatcher disp = snmp.getMessageDispatcher();
                disp.addMessageProcessingModel(new MPv1());
                disp.addMessageProcessingModel(new MPv2c());
                snmp.addTransportMapping(tm);
                OctetString localEngineID = new OctetString(
                        MPv3.createLocalEngineID());
                // For command generators, you may use the following code to avoid
                // engine ID clashes:
                // MPv3.createLocalEngineID(
                //   new OctetString("MyUniqueID"+System.currentTimeMillis())));
                USM usm = new USM(SecurityProtocols.getInstance(), localEngineID, 0);
                disp.addMessageProcessingModel(new MPv3(usm));
                //snmp.listen();
                snmp.addCommandResponder(AgentService.this);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void processPdu(CommandResponderEvent commandResponderEvent) {
        Log.i("LocalService", "processPdu");
        PDU command = (PDU) commandResponderEvent.getPDU().clone();
        if (command != null) {
            lastRequestReceived = command.toString() + " " + commandResponderEvent.getPeerAddress();
            //sendMessageToClients(MSG_SNMP_REQUEST_RECEIVED);
            if (command.getType() == PDU.GET){
                handleGetRequest(command);
            } else if(command.getType() == PDU.GETNEXT){
                handleGetNextRequest(command);
            }
            Address address = commandResponderEvent.getPeerAddress();
            sendResponse(address, command);
        }
    }

    private void sendResponse(Address address, PDU command) {
        command.setType(PDU.RESPONSE);
        System.out.println(command.toString());
        // Specify receiver
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setVersion(SnmpConstants.version2c);
        target.setAddress(address);
        target.setRetries(1);
        target.setTimeout(1500);

        try {
            snmp.send(command, target);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGetNextRequest(PDU command) {
        VariableBinding varBind;
        for(int i = 0; i < command.size(); i++){
            varBind = command.get(i);
            command.set(i, answerForGetNext(varBind.getOid()));
        }
    }

    private VariableBinding answerForGetNext(OID oid) {
        MIBTree MIB_MAP = MIBTree.getInstance();
        return MIB_MAP.getNext(oid);
    }

    private void handleGetRequest(PDU command) {
        VariableBinding varBind;
        for(int i = 0; i < command.size(); i++){
            varBind = command.get(i);
            varBind.setVariable(answerForGet(varBind.getOid()));

        }
    }

    private Variable answerForGet(OID oid) {
        MIBTree MIB_MAP = MIBTree.getInstance();
        VariableBinding vb = MIB_MAP.get(oid);
        return vb.getVariable();
    }
}
