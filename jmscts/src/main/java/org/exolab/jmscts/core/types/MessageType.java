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
 * Class MessageType.
 * 
 * @version $Revision$ $Date$
 */
public class MessageType implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The BytesMessage type
     */
    public static final int BYTESMESSAGE_TYPE = 0;

    /**
     * The instance of the BytesMessage type
     */
    public static final MessageType BYTESMESSAGE = new MessageType(BYTESMESSAGE_TYPE, "BytesMessage");

    /**
     * The MapMessage type
     */
    public static final int MAPMESSAGE_TYPE = 1;

    /**
     * The instance of the MapMessage type
     */
    public static final MessageType MAPMESSAGE = new MessageType(MAPMESSAGE_TYPE, "MapMessage");

    /**
     * The ObjectMessage type
     */
    public static final int OBJECTMESSAGE_TYPE = 2;

    /**
     * The instance of the ObjectMessage type
     */
    public static final MessageType OBJECTMESSAGE = new MessageType(OBJECTMESSAGE_TYPE, "ObjectMessage");

    /**
     * The StreamMessage type
     */
    public static final int STREAMMESSAGE_TYPE = 3;

    /**
     * The instance of the StreamMessage type
     */
    public static final MessageType STREAMMESSAGE = new MessageType(STREAMMESSAGE_TYPE, "StreamMessage");

    /**
     * The TextMessage type
     */
    public static final int TEXTMESSAGE_TYPE = 4;

    /**
     * The instance of the TextMessage type
     */
    public static final MessageType TEXTMESSAGE = new MessageType(TEXTMESSAGE_TYPE, "TextMessage");

    /**
     * The Message type
     */
    public static final int MESSAGE_TYPE = 5;

    /**
     * The instance of the Message type
     */
    public static final MessageType MESSAGE = new MessageType(MESSAGE_TYPE, "Message");

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

    private MessageType(int type, java.lang.String value) {
        super();
        this.type = type;
        this.stringValue = value;
    } //-- org.exolab.jmscts.core.types.MessageType(int, java.lang.String)


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method enumerateReturns an enumeration of all possible
     * instances of MessageType
     */
    public static java.util.Enumeration enumerate()
    {
        return _memberTable.elements();
    } //-- java.util.Enumeration enumerate() 

    /**
     * Method getTypeReturns the type of this MessageType
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
        members.put("BytesMessage", BYTESMESSAGE);
        members.put("MapMessage", MAPMESSAGE);
        members.put("ObjectMessage", OBJECTMESSAGE);
        members.put("StreamMessage", STREAMMESSAGE);
        members.put("TextMessage", TEXTMESSAGE);
        members.put("Message", MESSAGE);
        return members;
    } //-- java.util.Hashtable init() 

    /**
     * Method toStringReturns the String representation of this
     * MessageType
     */
    public java.lang.String toString()
    {
        return this.stringValue;
    } //-- java.lang.String toString() 

    /**
     * Method valueOfReturns a new MessageType based on the given
     * String value.
     * 
     * @param string
     */
    public static org.exolab.jmscts.core.types.MessageType valueOf(java.lang.String string)
    {
        java.lang.Object obj = null;
        if (string != null) obj = _memberTable.get(string);
        if (obj == null) {
            String err = "'" + string + "' is not a valid MessageType";
            throw new IllegalArgumentException(err);
        }
        return (MessageType) obj;
    } //-- org.exolab.jmscts.core.types.MessageType valueOf(java.lang.String) 

}
