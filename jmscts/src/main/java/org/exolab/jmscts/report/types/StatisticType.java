/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.4.3</a>, using an XML
 * Schema.
 * $Id$
 */

package org.exolab.jmscts.report.types;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class StatisticType.
 * 
 * @version $Revision$ $Date$
 */
public class StatisticType implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The send type
     */
    public static final int SEND_TYPE = 0;

    /**
     * The instance of the send type
     */
    public static final StatisticType SEND = new StatisticType(SEND_TYPE, "send");

    /**
     * The receive type
     */
    public static final int RECEIVE_TYPE = 1;

    /**
     * The instance of the receive type
     */
    public static final StatisticType RECEIVE = new StatisticType(RECEIVE_TYPE, "receive");

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

    private StatisticType(int type, java.lang.String value) {
        super();
        this.type = type;
        this.stringValue = value;
    } //-- org.exolab.jmscts.report.types.StatisticType(int, java.lang.String)


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method enumerateReturns an enumeration of all possible
     * instances of StatisticType
     */
    public static java.util.Enumeration enumerate()
    {
        return _memberTable.elements();
    } //-- java.util.Enumeration enumerate() 

    /**
     * Method getTypeReturns the type of this StatisticType
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
        members.put("send", SEND);
        members.put("receive", RECEIVE);
        return members;
    } //-- java.util.Hashtable init() 

    /**
     * Method toStringReturns the String representation of this
     * StatisticType
     */
    public java.lang.String toString()
    {
        return this.stringValue;
    } //-- java.lang.String toString() 

    /**
     * Method valueOfReturns a new StatisticType based on the given
     * String value.
     * 
     * @param string
     */
    public static org.exolab.jmscts.report.types.StatisticType valueOf(java.lang.String string)
    {
        java.lang.Object obj = null;
        if (string != null) obj = _memberTable.get(string);
        if (obj == null) {
            String err = "'" + string + "' is not a valid StatisticType";
            throw new IllegalArgumentException(err);
        }
        return (StatisticType) obj;
    } //-- org.exolab.jmscts.report.types.StatisticType valueOf(java.lang.String) 

}
