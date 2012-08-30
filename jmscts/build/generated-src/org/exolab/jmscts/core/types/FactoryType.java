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
 * Class FactoryType.
 * 
 * @version $Revision$ $Date$
 */
public class FactoryType implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The QueueConnectionFactory type
     */
    public static final int QUEUECONNECTIONFACTORY_TYPE = 0;

    /**
     * The instance of the QueueConnectionFactory type
     */
    public static final FactoryType QUEUECONNECTIONFACTORY = new FactoryType(QUEUECONNECTIONFACTORY_TYPE, "QueueConnectionFactory");

    /**
     * The TopicConnectionFactory type
     */
    public static final int TOPICCONNECTIONFACTORY_TYPE = 1;

    /**
     * The instance of the TopicConnectionFactory type
     */
    public static final FactoryType TOPICCONNECTIONFACTORY = new FactoryType(TOPICCONNECTIONFACTORY_TYPE, "TopicConnectionFactory");

    /**
     * The XAQueueConnectionFactory type
     */
    public static final int XAQUEUECONNECTIONFACTORY_TYPE = 2;

    /**
     * The instance of the XAQueueConnectionFactory type
     */
    public static final FactoryType XAQUEUECONNECTIONFACTORY = new FactoryType(XAQUEUECONNECTIONFACTORY_TYPE, "XAQueueConnectionFactory");

    /**
     * The XATopicConnectionFactory type
     */
    public static final int XATOPICCONNECTIONFACTORY_TYPE = 3;

    /**
     * The instance of the XATopicConnectionFactory type
     */
    public static final FactoryType XATOPICCONNECTIONFACTORY = new FactoryType(XATOPICCONNECTIONFACTORY_TYPE, "XATopicConnectionFactory");

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

    private FactoryType(int type, java.lang.String value) {
        super();
        this.type = type;
        this.stringValue = value;
    } //-- org.exolab.jmscts.core.types.FactoryType(int, java.lang.String)


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method enumerateReturns an enumeration of all possible
     * instances of FactoryType
     */
    public static java.util.Enumeration enumerate()
    {
        return _memberTable.elements();
    } //-- java.util.Enumeration enumerate() 

    /**
     * Method getTypeReturns the type of this FactoryType
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
        members.put("QueueConnectionFactory", QUEUECONNECTIONFACTORY);
        members.put("TopicConnectionFactory", TOPICCONNECTIONFACTORY);
        members.put("XAQueueConnectionFactory", XAQUEUECONNECTIONFACTORY);
        members.put("XATopicConnectionFactory", XATOPICCONNECTIONFACTORY);
        return members;
    } //-- java.util.Hashtable init() 

    /**
     * Method toStringReturns the String representation of this
     * FactoryType
     */
    public java.lang.String toString()
    {
        return this.stringValue;
    } //-- java.lang.String toString() 

    /**
     * Method valueOfReturns a new FactoryType based on the given
     * String value.
     * 
     * @param string
     */
    public static org.exolab.jmscts.core.types.FactoryType valueOf(java.lang.String string)
    {
        java.lang.Object obj = null;
        if (string != null) obj = _memberTable.get(string);
        if (obj == null) {
            String err = "'" + string + "' is not a valid FactoryType";
            throw new IllegalArgumentException(err);
        }
        return (FactoryType) obj;
    } //-- org.exolab.jmscts.core.types.FactoryType valueOf(java.lang.String) 

}
