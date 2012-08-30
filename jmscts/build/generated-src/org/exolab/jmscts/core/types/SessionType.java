/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.4.3</a>, using an XML
 * Schema.
 * $Id$
 */

package org.exolab.jmscts.core.types;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class SessionType.
 * 
 * @version $Revision$ $Date$
 */
public class SessionType implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The TRANSACTED type
     */
    public static final int TRANSACTED_TYPE = 0;

    /**
     * The instance of the TRANSACTED type
     */
    public static final SessionType TRANSACTED = new SessionType(TRANSACTED_TYPE, "TRANSACTED");

    /**
     * The CLIENT_ACKNOWLEDGE type
     */
    public static final int CLIENT_ACKNOWLEDGE_TYPE = 1;

    /**
     * The instance of the CLIENT_ACKNOWLEDGE type
     */
    public static final SessionType CLIENT_ACKNOWLEDGE = new SessionType(CLIENT_ACKNOWLEDGE_TYPE, "CLIENT_ACKNOWLEDGE");

    /**
     * The DUPS_OK_ACKNOWLEDGE type
     */
    public static final int DUPS_OK_ACKNOWLEDGE_TYPE = 2;

    /**
     * The instance of the DUPS_OK_ACKNOWLEDGE type
     */
    public static final SessionType DUPS_OK_ACKNOWLEDGE = new SessionType(DUPS_OK_ACKNOWLEDGE_TYPE, "DUPS_OK_ACKNOWLEDGE");

    /**
     * The AUTO_ACKNOWLEDGE type
     */
    public static final int AUTO_ACKNOWLEDGE_TYPE = 3;

    /**
     * The instance of the AUTO_ACKNOWLEDGE type
     */
    public static final SessionType AUTO_ACKNOWLEDGE = new SessionType(AUTO_ACKNOWLEDGE_TYPE, "AUTO_ACKNOWLEDGE");

    /**
     * Field _memberTable
     */
    private static java.util.Hashtable _memberTable = init();

    /**
     * Field type
     */
    private int type = -1;

    /**
     * Field stringValue
     */
    private java.lang.String stringValue = null;


      //----------------/
     //- Constructors -/
    //----------------/

    private SessionType(int type, java.lang.String value) {
        super();
        this.type = type;
        this.stringValue = value;
    } //-- org.exolab.jmscts.core.types.SessionType(int, java.lang.String)


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method enumerateReturns an enumeration of all possible
     * instances of SessionType
     */
    public static java.util.Enumeration enumerate()
    {
        return _memberTable.elements();
    } //-- java.util.Enumeration enumerate() 

    /**
     * Method getTypeReturns the type of this SessionType
     */
    public int getType()
    {
        return this.type;
    } //-- int getType() 

    /**
     * Method init
     */
    private static java.util.Hashtable init()
    {
        Hashtable members = new Hashtable();
        members.put("TRANSACTED", TRANSACTED);
        members.put("CLIENT_ACKNOWLEDGE", CLIENT_ACKNOWLEDGE);
        members.put("DUPS_OK_ACKNOWLEDGE", DUPS_OK_ACKNOWLEDGE);
        members.put("AUTO_ACKNOWLEDGE", AUTO_ACKNOWLEDGE);
        return members;
    } //-- java.util.Hashtable init() 

    /**
     * Method toStringReturns the String representation of this
     * SessionType
     */
    public java.lang.String toString()
    {
        return this.stringValue;
    } //-- java.lang.String toString() 

    /**
     * Method valueOfReturns a new SessionType based on the given
     * String value.
     * 
     * @param string
     */
    public static org.exolab.jmscts.core.types.SessionType valueOf(java.lang.String string)
    {
        java.lang.Object obj = null;
        if (string != null) obj = _memberTable.get(string);
        if (obj == null) {
            String err = "'" + string + "' is not a valid SessionType";
            throw new IllegalArgumentException(err);
        }
        return (SessionType) obj;
    } //-- org.exolab.jmscts.core.types.SessionType valueOf(java.lang.String) 

}
