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
 * Class PropertyTypeType.
 * 
 * @version $Revision$ $Date$
 */
public class PropertyTypeType implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The java.lang.Boolean type
     */
    public static final int VALUE_0_TYPE = 0;

    /**
     * The instance of the java.lang.Boolean type
     */
    public static final PropertyTypeType VALUE_0 = new PropertyTypeType(VALUE_0_TYPE, "java.lang.Boolean");

    /**
     * The java.lang.Byte type
     */
    public static final int VALUE_1_TYPE = 1;

    /**
     * The instance of the java.lang.Byte type
     */
    public static final PropertyTypeType VALUE_1 = new PropertyTypeType(VALUE_1_TYPE, "java.lang.Byte");

    /**
     * The java.lang.Short type
     */
    public static final int VALUE_2_TYPE = 2;

    /**
     * The instance of the java.lang.Short type
     */
    public static final PropertyTypeType VALUE_2 = new PropertyTypeType(VALUE_2_TYPE, "java.lang.Short");

    /**
     * The java.lang.Integer type
     */
    public static final int VALUE_3_TYPE = 3;

    /**
     * The instance of the java.lang.Integer type
     */
    public static final PropertyTypeType VALUE_3 = new PropertyTypeType(VALUE_3_TYPE, "java.lang.Integer");

    /**
     * The java.lang.Long type
     */
    public static final int VALUE_4_TYPE = 4;

    /**
     * The instance of the java.lang.Long type
     */
    public static final PropertyTypeType VALUE_4 = new PropertyTypeType(VALUE_4_TYPE, "java.lang.Long");

    /**
     * The java.lang.Float type
     */
    public static final int VALUE_5_TYPE = 5;

    /**
     * The instance of the java.lang.Float type
     */
    public static final PropertyTypeType VALUE_5 = new PropertyTypeType(VALUE_5_TYPE, "java.lang.Float");

    /**
     * The java.lang.Double type
     */
    public static final int VALUE_6_TYPE = 6;

    /**
     * The instance of the java.lang.Double type
     */
    public static final PropertyTypeType VALUE_6 = new PropertyTypeType(VALUE_6_TYPE, "java.lang.Double");

    /**
     * The java.lang.String type
     */
    public static final int VALUE_7_TYPE = 7;

    /**
     * The instance of the java.lang.String type
     */
    public static final PropertyTypeType VALUE_7 = new PropertyTypeType(VALUE_7_TYPE, "java.lang.String");

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

    private PropertyTypeType(int type, java.lang.String value) {
        super();
        this.type = type;
        this.stringValue = value;
    } //-- org.exolab.jmscts.core.types.PropertyTypeType(int, java.lang.String)


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method enumerateReturns an enumeration of all possible
     * instances of PropertyTypeType
     */
    public static java.util.Enumeration enumerate()
    {
        return _memberTable.elements();
    } //-- java.util.Enumeration enumerate() 

    /**
     * Method getTypeReturns the type of this PropertyTypeType
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
        members.put("java.lang.Boolean", VALUE_0);
        members.put("java.lang.Byte", VALUE_1);
        members.put("java.lang.Short", VALUE_2);
        members.put("java.lang.Integer", VALUE_3);
        members.put("java.lang.Long", VALUE_4);
        members.put("java.lang.Float", VALUE_5);
        members.put("java.lang.Double", VALUE_6);
        members.put("java.lang.String", VALUE_7);
        return members;
    } //-- java.util.Hashtable init() 

    /**
     * Method toStringReturns the String representation of this
     * PropertyTypeType
     */
    public java.lang.String toString()
    {
        return this.stringValue;
    } //-- java.lang.String toString() 

    /**
     * Method valueOfReturns a new PropertyTypeType based on the
     * given String value.
     * 
     * @param string
     */
    public static org.exolab.jmscts.core.types.PropertyTypeType valueOf(java.lang.String string)
    {
        java.lang.Object obj = null;
        if (string != null) obj = _memberTable.get(string);
        if (obj == null) {
            String err = "'" + string + "' is not a valid PropertyTypeType";
            throw new IllegalArgumentException(err);
        }
        return (PropertyTypeType) obj;
    } //-- org.exolab.jmscts.core.types.PropertyTypeType valueOf(java.lang.String) 

}
