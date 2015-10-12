package br.ufrgs.inf01059.wikipapers.SnmpAgent;


import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import org.snmp4j.*;
import org.snmp4j.agent.CommandProcessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.MOGroup;
import org.snmp4j.agent.MOServer;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.mo.snmp.SnmpCommunityMIB;
import org.snmp4j.agent.mo.snmp.SnmpNotificationMIB;
import org.snmp4j.agent.mo.snmp.SnmpTargetMIB;
import org.snmp4j.agent.mo.snmp.StorageType;
import org.snmp4j.agent.mo.snmp.VacmMIB;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.USM;
import org.snmp4j.smi.*;
import org.snmp4j.transport.TransportMappings;
import org.snmp4j.agent.BaseAgent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class AgentService extends Service {

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
    protected MOServer server;
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
        //new AgentListener().start();
        try {
            SnmpAgent sa = new SnmpAgent("0.0.0.0/9999");
            //sa.initTransportMappings();
            sa.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private class SnmpAgent extends BaseAgent {

        // not needed but very useful of course
        /*static {
            LogFactory.setLogFactory(new Log4jLogFactory());
        }*/

        private String address;

        public SnmpAgent(String address) throws IOException {

            // These files does not exist and are not used but has to be specified
            // Read snmp4j docs for more info
            super(null, null,
                    new CommandProcessor(
                            new OctetString(MPv3.createLocalEngineID())));
            this.address = address;
        }

        /**
         * We let clients of this agent register the MO they
         * need so this method does nothing
         */
        @Override
        protected void registerManagedObjects() {
        }

        /**
         * Clients can register the MO they need
         */
        public void registerManagedObject(ManagedObject mo) {
            try {
                server.register(mo, null);
            } catch (DuplicateRegistrationException ex) {
                throw new RuntimeException(ex);
            }
        }

        public void unregisterManagedObject(MOGroup moGroup) {
            moGroup.unregisterMOs(server, getContext(moGroup));
        }

        /**
         * Minimal View based Access Control
         *
         * http://www.faqs.org/rfcs/rfc2575.html
         */
        @Override
        protected void addViews(VacmMIB vacm) {

            vacm.addGroup(SecurityModel.SECURITY_MODEL_SNMPv2c, new OctetString(
                            "cpublic"), new OctetString("v1v2group"),
                    StorageType.nonVolatile);

            vacm.addAccess(new OctetString("v1v2group"), new OctetString("public"),
                    SecurityModel.SECURITY_MODEL_ANY, SecurityLevel.NOAUTH_NOPRIV,
                    MutableVACM.VACM_MATCH_EXACT, new OctetString("fullReadView"),
                    new OctetString("fullWriteView"), new OctetString(
                            "fullNotifyView"), StorageType.nonVolatile);

            vacm.addViewTreeFamily(new OctetString("fullReadView"), new OID("1.3"),
                    new OctetString(), VacmMIB.vacmViewIncluded,
                    StorageType.nonVolatile);
        }

        /**
         * User based Security Model, only applicable to
         * SNMP v.3
         *
         */
        @Override
        protected void addUsmUser(USM usm) {
        }

        @Override
        protected void addNotificationTargets(SnmpTargetMIB snmpTargetMIB,
                                              SnmpNotificationMIB snmpNotificationMIB) {

        }

        @Override
        protected void initTransportMappings() throws IOException {
            this.transportMappings = new TransportMapping[1];
            Address addr = GenericAddress.parse(address);
            TransportMapping tm = TransportMappings.getInstance().createTransportMapping(addr);
            this.transportMappings[0] = tm;
        }

        /**
         * Start method invokes some initialization methods needed to
         * start the agent
         * @throws IOException
         */
        public void start() throws IOException {
            init();
            // This method reads some old config from a file and causes
            // unexpected behavior.
            // loadConfig(ImportModes.REPLACE_CREATE);
            addShutdownHook();
            getServer().addContext(new OctetString("public"));
            finishInit();
            run();
            sendColdStartNotification();
        }

        protected void unregisterManagedObjects() {
            // here we should unregister those objects previously registered...
        }

        @Override
        protected void addCommunities(SnmpCommunityMIB communityMIB) {
            /*Variable[] com2sec = new Variable[] {
                    new OctetString("public"), // community name
                    new OctetString("cpublic"), // security name
                    getAgent().getContextEngineID(), // local engine ID
                    new OctetString("public"), // default context name
                    new OctetString(), // transport tag
                    new Integer32(StorageType.nonVolatile), // storage type
                    new Integer32(RowStatus.active) // row status
            };
            MOTableRow row = communityMIB.getSnmpCommunityEntry().createRow(
                    new OctetString("public2public").toSubIndex(true), com2sec);
            communityMIB.getSnmpCommunityEntry().addRow(row);*/
        }
    }

    private class AgentCommandResponder implements CommandResponder {
        @Override
        public synchronized void processPdu(CommandResponderEvent commandResponderEvent) {
            Log.i("LocalService", "processPdu");
            PDU command = (PDU) commandResponderEvent.getPDU().clone();
            if (command != null) {
                lastRequestReceived = command.toString() + " " + commandResponderEvent.getPeerAddress();
                //sendMessageToClients(MSG_SNMP_REQUEST_RECEIVED);
                if (command.getType() == PDU.GET) {
                    handleGetRequest(command);
                } else if (command.getType() == PDU.GETNEXT) {
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
            for (int i = 0; i < command.size(); i++) {
                varBind = command.get(i);
                //command.set(i, answerForGetNext(varBind.getOid()));
            }
        }

        //private VariableBinding answerForGetNext(OID oid) {
        //MIBTree MIB_MAP = MIBTree.getInstance();
        //return MIB_MAP.getNext(oid);
        //}

        private void handleGetRequest(PDU command) {
            VariableBinding varBind;
            for (int i = 0; i < command.size(); i++) {
                varBind = command.get(i);
                //varBind.setVariable(answerForGet(varBind.getOid()));
            }
        }

        //private Variable answerForGet(OID oid) {
        //MIBTree MIB_MAP = MIBTree.getInstance();
        //VariableBinding vb = MIB_MAP.get(oid);
        //return vb.getVariable();
        //}
    }
}
