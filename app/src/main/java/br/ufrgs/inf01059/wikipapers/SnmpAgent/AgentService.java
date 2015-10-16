package br.ufrgs.inf01059.wikipapers.SnmpAgent;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import org.snmp4j.*;
import org.snmp4j.agent.CommandProcessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.MOGroup;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.mo.snmp.RowStatus;
import org.snmp4j.agent.mo.snmp.SnmpCommunityMIB;
import org.snmp4j.agent.mo.snmp.SnmpNotificationMIB;
import org.snmp4j.agent.mo.snmp.SnmpTargetMIB;
import org.snmp4j.agent.mo.snmp.StorageType;
import org.snmp4j.agent.mo.snmp.VacmMIB;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.USM;
import org.snmp4j.smi.*;
import org.snmp4j.transport.TransportMappings;
import org.snmp4j.agent.BaseAgent;
import org.snmp4j.agent.mo.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import br.ufrgs.inf01059.wikinotes.R;
import br.ufrgs.inf01059.wikipapers.model.Note;
import br.ufrgs.inf01059.wikipapers.model.NotesDAO;

public class AgentService extends Service {

    private SnmpAgent snmp_agent;

    private MOTable notesTable = null;
    private MOScalar username;
    private MOScalar numberOfNotes;
    private MOScalar numberOfNotesSynced;
    private MOScalar lastSync;

    private static OID usernameOid = new OID(new int[] {1,3,6,1,3,1,1,0});
    private static OID numberOfNotesOid = new OID(new int[] {1,3,6,1,3,1,2,0});
    private static OID numberOfNotesSyncedOid = new OID(new int[] {1,3,6,1,3,1,3,0});
    private static OID lastSyncOid = new OID(new int[] {1,3,6,1,3,1,4,0});
    private static OID notesTableOid = new OID(new int[] {1,3,6,1,3,1,5});

    private Timer timer;

    @Override
    public void onCreate() {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            File file1 = File.createTempFile("temp1", null, this.getApplicationContext().getCacheDir());
            File file2 = File.createTempFile("temp2", null, this.getApplicationContext().getCacheDir());
            snmp_agent = new SnmpAgent("0.0.0.0/9999", file1, file2);

            //sa.initTransportMappings();
            snmp_agent.start();
            snmp_agent.unregisterManagedObject(snmp_agent.getSnmpv2MIB());

        } catch (IOException e) {
            e.printStackTrace();
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new RefreshMIBData(), 0, 10000);
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
        //return mMessenger.getBinder();
        return null;
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        timer.purge();
        snmp_agent.stop();
    }

    class RefreshMIBData extends TimerTask {

        public RefreshMIBData(){
        }

        public void run() {
            Context context = getApplicationContext();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences stats_prefs = context.getSharedPreferences(getString(R.string.preference_file_id), Context.MODE_PRIVATE);
            List < Note > Notes = NotesDAO.getNotes(context);

            if (notesTable != null){
                snmp_agent.getServer().unregister(notesTable, null);
                username.setValue(new OctetString(prefs.getString("username", "Not set")));
                numberOfNotes.setValue(new Integer32(Notes.size()));
                SharedPreferences.Editor editor = stats_prefs.edit();
                editor.putInt("nSyncNotes", numberOfNotesSynced.getValue().toInt());
                editor.putLong("syncDate", lastSync.getValue().toLong());
                editor.commit();
            }
            else{
                username = MOScalarFactory.createReadOnly(usernameOid,  prefs.getString("username", "Not Set"));
                numberOfNotesSynced =  MOScalarFactory.createReadWrite(numberOfNotesSyncedOid, prefs.getInt("nSyncNotes", 0));
                lastSync =  MOScalarFactory.createReadWrite(lastSyncOid, new Counter64(prefs.getLong("syncDate", 0)));
                numberOfNotes =  MOScalarFactory.createReadOnly(numberOfNotesOid, Notes.size());
                snmp_agent.registerManagedObject(username);
                snmp_agent.registerManagedObject(numberOfNotes);
                snmp_agent.registerManagedObject(numberOfNotesSynced);
                snmp_agent.registerManagedObject(lastSync);
            }

            numberOfNotes.setValue(new Integer32(Notes.size()));
            MOTableBuilder notesTableBuilder = new MOTableBuilder(notesTableOid)
                    .addColumnType(SMIConstants.SYNTAX_INTEGER,MOAccessImpl.ACCESS_READ_ONLY)
                    .addColumnType(SMIConstants.SYNTAX_OCTET_STRING, MOAccessImpl.ACCESS_READ_ONLY)
                    .addColumnType(SMIConstants.SYNTAX_OCTET_STRING, MOAccessImpl.ACCESS_READ_ONLY)
                    .addColumnType(SMIConstants.SYNTAX_COUNTER64, MOAccessImpl.ACCESS_READ_ONLY);
            for (Note n : Notes) {
                notesTableBuilder.addRowValue(new Integer32(Integer.parseInt(n.id)));
                notesTableBuilder.addRowValue(new OctetString(n.title));
                notesTableBuilder.addRowValue(new OctetString(n.content));
                notesTableBuilder.addRowValue(new Counter64(n.creationDate.getTime() / 1000));
            }
            notesTable = notesTableBuilder.build();
            snmp_agent.registerManagedObject(notesTable);
        }
    }

    private class SnmpAgent extends BaseAgent {

        // not needed but very useful of course
        /*static {
            LogFactory.setLogFactory(new Log4jLogFactory());
        }*/

        private String address;

        public SnmpAgent(String address, File temp1, File temp2) throws IOException {

            // These files does not exist and are not used but has to be specified
            // Read snmp4j docs for more info
            super(temp1, temp2,
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
            /*NotesMIB nm = new NotesMIB();
            try {
                nm.registerMOs(server, new OctetString("public"));
            } catch (DuplicateRegistrationException e) {
                e.printStackTrace();
            }*/
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

            vacm.addGroup(SecurityModel.SECURITY_MODEL_SNMPv2c, new OctetString(
                            "cpublic"), new OctetString("v1v2group"),
                    StorageType.nonVolatile);

            vacm.addAccess(new OctetString("v1v2group"), new OctetString("private"),
                    SecurityModel.SECURITY_MODEL_ANY, SecurityLevel.AUTH_PRIV,
                    MutableVACM.VACM_MATCH_PREFIX, new OctetString("fullReadView"),
                    new OctetString("fullWriteView"), new OctetString(
                            "fullNotifyView"), StorageType.nonVolatile);

            vacm.addViewTreeFamily(new OctetString("fullReadView"), new OID("1.3.6.1.3"),
                    new OctetString(), VacmMIB.vacmViewIncluded,
                    StorageType.nonVolatile);
            vacm.addViewTreeFamily(new OctetString("fullWriteView"), new OID("1.3.6.1.3"),
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
            Variable[] com2sec = new Variable[] {
                    new OctetString("public"), // community name
                    new OctetString("cpublic"), // security name
                    getAgent().getContextEngineID(), // local engine ID
                    new OctetString("public"), // default context name
                    new OctetString(), // transport tag
                    new Integer32(StorageType.nonVolatile), // storage type
                    new Integer32(RowStatus.active) // row status
            };
            SnmpCommunityMIB.SnmpCommunityEntryRow row = communityMIB.getSnmpCommunityEntry().createRow(
                    new OctetString("public").toSubIndex(true), com2sec);
            communityMIB.getSnmpCommunityEntry().addRow(row);

            Variable[] com2sec2 = new Variable[] {
                    new OctetString("private"), //community name
                    new OctetString("cprivate"), // security name
                    getAgent().getContextEngineID(), // local engine ID
                    new OctetString("private"), // default context name
                    new OctetString(), // transport tag
                    new Integer32(StorageType.nonVolatile), // storage type
                    new Integer32(RowStatus.active) // row status
            };

            SnmpCommunityMIB.SnmpCommunityEntryRow row2 = communityMIB.getSnmpCommunityEntry().createRow(
                    new OctetString("private").toSubIndex(true), com2sec);
            communityMIB.getSnmpCommunityEntry().addRow(row2);
        }
    }
}
