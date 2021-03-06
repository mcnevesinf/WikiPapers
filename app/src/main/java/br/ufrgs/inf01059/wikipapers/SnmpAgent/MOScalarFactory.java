package br.ufrgs.inf01059.wikipapers.SnmpAgent;

import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import java.util.Date;

public class MOScalarFactory {

    public static MOScalar createReadOnly(OID oid,Object value ){
        return new MOScalar(oid,
                MOAccessImpl.ACCESS_READ_ONLY,
                getVariable(value));
    }

    public static MOScalar createReadWrite(OID oid,Object value ){
        return new MOScalar(oid,
                MOAccessImpl.ACCESS_READ_WRITE,
                getVariable(value));
    }

    private static Variable getVariable(Object value) {
        if(value instanceof String) {
            return new OctetString((String)value);
        }
        if(value instanceof Integer) {
            return new Integer32((Integer)value);
        }
        if (value instanceof Date) {
            return new OctetString("a");
        }
        if (value instanceof Counter64) {
            return (Counter64)value;
        }
        throw new IllegalArgumentException("Unmanaged Type: " + value.getClass());
    }
}