package br.ufrgs.inf01059.wikipapers.SnmpAgent;


import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

import org.snmp4j.smi.*;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.agent.*;
import org.snmp4j.agent.mo.*;
import org.snmp4j.agent.mo.snmp.*;
import org.snmp4j.agent.mo.snmp.smi.*;
import org.snmp4j.agent.request.*;
import org.snmp4j.log.LogFactory;
import org.snmp4j.log.LogAdapter;
import org.snmp4j.agent.mo.snmp.tc.*;

import java.io.OptionalDataException;
import java.util.*;


public class NotesMIB implements MOGroup {

    /*public static final OID SYS_ANDROID_VERSION_OID = new OID(new int[] {1,3,6,1,4,1,12619,1,1,2});
    public static final OID SYS_UPTIME_OID = new OID(new int[] {1,3,6,1,4,1,12619,1,1,3});
    public static final OID SRVC_NUMBER_OID = new OID(new int[] {1,3,6,1,4,1,12619,1,2,1});
    public static final OID SRVC_TABLE_OID = new OID(new int[] {1,3,6,1,4,1,12619,1,2,2});
    public static final OID SRVC_ENTRY_OID = new OID(new int[] {1,3,6,1,4,1,12619,1,2,2,1});
    public static final OID SRVC_INDEX_OID = new OID(new int[] {1,3,6,1,4,1,12619,1,2,2,1,1});
    public static final OID SRVC_DESCR_OID = new OID(new int[] {1,3,6,1,4,1,12619,1,2,2,1,2});
    public static final OID SRVC_RUNNING_TIME_OID = new OID(new int[] {1,3,6,1,4,1,12619,1,2,2,1,3});
    public static final OID SRVC_MEMORY_USED_OID = new OID(new int[] {1,3,6,1,4,1,12619,1,2,2,1,4});
    public static final OID HW_BATTERY_STATUS_OID = new OID(new int[] {1,3,6,1,4,1,12619,1,3,1});
    public static final OID HW_BATTERY_LEVEL_OID = new OID(new int[] {1,3,6,1,4,1,12619,1,3,2});
    public static final OID HW_GPS_STATUS_OID = new OID(new int[] {1,3,6,1,4,1,12619,1,3,3});
    public static final OID HW_BLUETOOTH_STATUS_OID = new OID(new int[] {1,3,6,1,4,1,12619,1,3,4});
    public static final OID HW_NETWOK_STATUS_OID = new OID(new int[] {1,3,6,1,4,1,12619,1,3,5});
    public static final OID HW_CAMERA_STATUS_OID = new OID(new int[] {1,3,6,1,4,1,12619,1,3,6});
    public static final OID MNG_MANAGER_MESSAGE_OID = new OID(new int[] {1,3,6,1,4,1,12619,1,4,1});*/

    // Factory
    private MOFactory moFactory = DefaultMOFactory.getInstance();

    // Constants

    /**
     * OID of this MIB module for usage which can be
     * used for its identification.
     */
    public static final OID NOTES_MIB = new OID(new int[] {1,3,6,1,4,1,31337});

    // Identities
    // Scalars
    public static final OID oidNotesTotalCount = new OID(new int[] {1,3,6,1,4,1,31337,1});
    // Tables
    // Notifications
    // Enumerations

    // TextualConventions
    private static final String TC_MODULE_DEMO_SCALAR_RW_MIB = "NOTES-MIB";
    private static final String TC_MODULE_SNMPV2_TC = "SNMPv2-TC";
    private static final String TC_DISPLAYSTRING = "DisplayString";

    // Scalars
    private MOScalar<Integer32> notesTotalCount;

    // Tables

    /**
     * Constructs a DemoScalarRwMib instance without actually creating its
     * <code>ManagedObject</code> instances. This has to be done in a
     * sub-class constructor or after construction by calling
     * {@link #createMO(MOFactory moFactory)}.
     */
    protected NotesMIB() {
    }

    /**
     * Constructs a DemoScalarRwMib instance and actually creates its
     * <code>ManagedObject</code> instances using the supplied
     * <code>MOFactory</code> (by calling
     * {@link #createMO(MOFactory moFactory)}).
     * @param moFactory
     *    the <code>MOFactory</code> to be used to create the
     *    managed objects for this module.
     */
    public NotesMIB(MOFactory moFactory) {
        this();
        createMO(moFactory);
    }

    /**
     * Create the ManagedObjects defined for this MIB module
     * using the specified {@link MOFactory}.
     * @param moFactory
     *    the <code>MOFactory</code> instance to use for object
     *    creation.
     */
    protected void createMO(MOFactory moFactory) {
        addTCsToFactory(moFactory);
        notesTotalCount =
                new NotesTotalCount(oidNotesTotalCount,
                        moFactory.createAccess(MOAccessImpl.ACCESSIBLE_FOR_READ_WRITE));
        notesTotalCount.addMOValueValidationListener(new NotesTotalCountValidator());
    }

    public MOScalar<Integer32> getNotesTotalCount() {
        return notesTotalCount;
    }

    public void registerMOs(MOServer server, OctetString context)
            throws DuplicateRegistrationException
    {
        // Scalar Objects
        server.register(this.notesTotalCount, context);
    }

    public void unregisterMOs(MOServer server, OctetString context) {
        // Scalar Objects
        server.unregister(this.notesTotalCount, context);
    }

    // Notifications

    // Scalars

    public class NotesTotalCount extends MOScalar<Integer32> {
        NotesTotalCount(OID oid, MOAccess access) {
            super(oid, access, new Integer32());
        }

        public int isValueOK(SubRequest request) {
            Variable newValue =
                    request.getVariableBinding().getVariable();
            int valueOK = super.isValueOK(request);
            if (valueOK != SnmpConstants.SNMP_ERROR_SUCCESS) {
                return valueOK;
            }
            return valueOK;
        }

        public Integer32 getValue() {
            return super.getValue();
        }

        public int setValue(Integer32 newValue) {
            return super.setValue(newValue);
        }
    }

    // Value Validators
    static class NotesTotalCountValidator implements MOValueValidationListener {

        public void validate(MOValueValidationEvent validationEvent) {
            Variable newValue = validationEvent.getNewValue();
        }
    }

    // Rows and Factories

    // Textual Definitions of MIB module DemoScalarRwMib
    protected void addTCsToFactory(MOFactory moFactory) {
        //moFactory.addTextualConvention(new DemoImageFormatTC());
    }

    // Textual Definitions of other MIB modules
    public void addImportedTCsToFactory(MOFactory moFactory) {
    }
}