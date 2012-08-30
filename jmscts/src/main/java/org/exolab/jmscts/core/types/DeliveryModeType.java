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
 * Class DeliveryModeType.
 * 
 * @version $Revision$ $Date$
 */
public class DeliveryModeType implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The PERSISTENT type
     */
    public static final int PERSISTENT_TYPE = 0;

    /**
     * The instance of the PERSISTENT type
     */
    public static final DeliveryModeType PERSISTENT = new DeliveryModeType(PERSISTENT_TYPE, "PERSISTENT");

    /**
     * The NON_PERSISTENT type
     */
    public static final int NON_PERSISTENT_TYPE = 1;

    /**
     * The instance of the NON_PERSISTENT type
     */
    public static final DeliveryModeType NON_PERSISTENT = new DeliveryModeType(NON_PERSISTENT_TYPE, "NON_PERSISTENT");

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

    private DeliveryModeType(int type, java.lang.String value) {
        super();
        this.type = type;
        this.stringValue = value;
    } //-- org.exolab.jmscts.core.types.DeliveryModeType(int, java.lang.String)


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method enumerateReturns an enumeration of all possible
     * instances of DeliveryModeType
     */
    public static java.util.Enumeration enumerate()
    {
        return _memberTable.elements();
    } //-- java.util.Enumeration enumerate() 

    /**
     * Method getTypeReturns the type of this DeliveryModeType
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
        members.put("PERSISTENT", PERSISTENT);
        members.put("NON_PERSISTENT", NON_PERSISTENT);
        return members;
    } //-- java.util.Hashtable init() 

    /**
     * Method toStringReturns the String representation of this
     * DeliveryModeType
     */
    public java.lang.String toString()
    {
        return this.stringValue;
    } //-- java.lang.String toString() 

    /**
     * Method valueOfReturns a new DeliveryModeType based on the
     * given String value.
     * 
     * @param string
     */
    public static org.exolab.jmscts.core.types.DeliveryModeType valueOf(java.lang.String string)
    {
        java.lang.Object obj = null;
        if (string != null) obj = _memberTable.get(string);
        if (obj == null) {
            String err = "'" + string + "' is not a valid DeliveryModeType";
            throw new IllegalArgumentException(err);
        }
        return (DeliveryModeType) obj;
    } //-- org.exolab.jmscts.core.types.DeliveryModeType valueOf(java.lang.String) 

}
