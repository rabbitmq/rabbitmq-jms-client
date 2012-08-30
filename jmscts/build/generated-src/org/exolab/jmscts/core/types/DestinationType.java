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
 * Class DestinationType.
 * 
 * @version $Revision$ $Date$
 */
public class DestinationType implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The administered type
     */
    public static final int ADMINISTERED_TYPE = 0;

    /**
     * The instance of the administered type
     */
    public static final DestinationType ADMINISTERED = new DestinationType(ADMINISTERED_TYPE, "administered");

    /**
     * The temporary type
     */
    public static final int TEMPORARY_TYPE = 1;

    /**
     * The instance of the temporary type
     */
    public static final DestinationType TEMPORARY = new DestinationType(TEMPORARY_TYPE, "temporary");

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

    private DestinationType(int type, java.lang.String value) {
        super();
        this.type = type;
        this.stringValue = value;
    } //-- org.exolab.jmscts.core.types.DestinationType(int, java.lang.String)


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method enumerateReturns an enumeration of all possible
     * instances of DestinationType
     */
    public static java.util.Enumeration enumerate()
    {
        return _memberTable.elements();
    } //-- java.util.Enumeration enumerate() 

    /**
     * Method getTypeReturns the type of this DestinationType
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
        members.put("administered", ADMINISTERED);
        members.put("temporary", TEMPORARY);
        return members;
    } //-- java.util.Hashtable init() 

    /**
     * Method toStringReturns the String representation of this
     * DestinationType
     */
    public java.lang.String toString()
    {
        return this.stringValue;
    } //-- java.lang.String toString() 

    /**
     * Method valueOfReturns a new DestinationType based on the
     * given String value.
     * 
     * @param string
     */
    public static org.exolab.jmscts.core.types.DestinationType valueOf(java.lang.String string)
    {
        java.lang.Object obj = null;
        if (string != null) obj = _memberTable.get(string);
        if (obj == null) {
            String err = "'" + string + "' is not a valid DestinationType";
            throw new IllegalArgumentException(err);
        }
        return (DestinationType) obj;
    } //-- org.exolab.jmscts.core.types.DestinationType valueOf(java.lang.String) 

}
