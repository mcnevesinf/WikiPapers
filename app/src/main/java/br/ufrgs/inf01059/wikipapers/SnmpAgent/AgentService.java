package br.ufrgs.inf01059.wikipapers.SnmpAgent;


import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import br.ufrgs.inf01059.wikipapers.SnmpAgent.MIBTree;
import org.snmp4j.*;
import org.snmp4j.mp.SnmpConstants;
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
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //return mMessenger.getBinder();
        return null;
    }

    @Override
    public void onDestroy() {
    }

    private class AgentListener extends Thread {
        public void run() {
            try {

                initSnmp();

                snmp.listen();

            }  catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void initSnmp(){
            try {
                TransportMapping transport;
                transport = new DefaultUdpTransportMapping(new UdpAddress("0.0.0.0/" + SNMP_PORT));

                snmp = new Snmp(transport);
                snmp.addCommandResponder(AgentService.this);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void processPdu(CommandResponderEvent commandResponderEvent) {
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
        target.setVersion(SnmpConstants.version1);
        target.setAddress(address);
        target.setRetries(0);
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
