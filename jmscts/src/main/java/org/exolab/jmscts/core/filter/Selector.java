/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.4.3</a>, using an XML
 * Schema.
 * $Id$
 */

package org.exolab.jmscts.core.filter;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.jmscts.core.types.DeliveryModeType;
import org.exolab.jmscts.core.types.DestinationType;
import org.exolab.jmscts.core.types.FactoryType;
import org.exolab.jmscts.core.types.MessageType;
import org.exolab.jmscts.core.types.ReceiverType;
import org.exolab.jmscts.core.types.SessionType;

/**
 * Class Selector.
 * 
 * @version $Revision$ $Date$
 */
public abstract class Selector implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _factory
     */
    private org.exolab.jmscts.core.types.FactoryType _factory;

    /**
     * Field _session
     */
    private org.exolab.jmscts.core.types.SessionType _session;

    /**
     * Field _destination
     */
    private org.exolab.jmscts.core.types.DestinationType _destination;

    /**
     * Field _deliveryMode
     */
    private org.exolab.jmscts.core.types.DeliveryModeType _deliveryMode;

    /**
     * Field _receiver
     */
    private org.exolab.jmscts.core.types.ReceiverType _receiver;

    /**
     * Field _message
     */
    private org.exolab.jmscts.core.types.MessageType _message;

    /**
     * Field _test
     */
    private java.lang.String _test;


      //----------------/
     //- Constructors -/
    //----------------/

    public Selector() {
        super();
    } //-- org.exolab.jmscts.core.filter.Selector()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Note: hashCode() has not been overriden
     * 
     * @param obj
     */
    public boolean equals(java.lang.Object obj)
    {
        if ( this == obj )
            return true;
        
        if (obj instanceof Selector) {
        
            Selector temp = (Selector)obj;
            if (this._factory != null) {
                if (temp._factory == null) return false;
                else if (!(this._factory.equals(temp._factory))) 
                    return false;
            }
            else if (temp._factory != null)
                return false;
            if (this._session != null) {
                if (temp._session == null) return false;
                else if (!(this._session.equals(temp._session))) 
                    return false;
            }
            else if (temp._session != null)
                return false;
            if (this._destination != null) {
                if (temp._destination == null) return false;
                else if (!(this._destination.equals(temp._destination))) 
                    return false;
            }
            else if (temp._destination != null)
                return false;
            if (this._deliveryMode != null) {
                if (temp._deliveryMode == null) return false;
                else if (!(this._deliveryMode.equals(temp._deliveryMode))) 
                    return false;
            }
            else if (temp._deliveryMode != null)
                return false;
            if (this._receiver != null) {
                if (temp._receiver == null) return false;
                else if (!(this._receiver.equals(temp._receiver))) 
                    return false;
            }
            else if (temp._receiver != null)
                return false;
            if (this._message != null) {
                if (temp._message == null) return false;
                else if (!(this._message.equals(temp._message))) 
                    return false;
            }
            else if (temp._message != null)
                return false;
            if (this._test != null) {
                if (temp._test == null) return false;
                else if (!(this._test.equals(temp._test))) 
                    return false;
            }
            else if (temp._test != null)
                return false;
            return true;
        }
        return false;
    } //-- boolean equals(java.lang.Object) 

    /**
     * Returns the value of field 'deliveryMode'.
     * 
     * @return the value of field 'deliveryMode'.
     */
    public org.exolab.jmscts.core.types.DeliveryModeType getDeliveryMode()
    {
        return this._deliveryMode;
    } //-- org.exolab.jmscts.core.types.DeliveryModeType getDeliveryMode() 

    /**
     * Returns the value of field 'destination'.
     * 
     * @return the value of field 'destination'.
     */
    public org.exolab.jmscts.core.types.DestinationType getDestination()
    {
        return this._destination;
    } //-- org.exolab.jmscts.core.types.DestinationType getDestination() 

    /**
     * Returns the value of field 'factory'.
     * 
     * @return the value of field 'factory'.
     */
    public org.exolab.jmscts.core.types.FactoryType getFactory()
    {
        return this._factory;
    } //-- org.exolab.jmscts.core.types.FactoryType getFactory() 

    /**
     * Returns the value of field 'message'.
     * 
     * @return the value of field 'message'.
     */
    public org.exolab.jmscts.core.types.MessageType getMessage()
    {
        return this._message;
    } //-- org.exolab.jmscts.core.types.MessageType getMessage() 

    /**
     * Returns the value of field 'receiver'.
     * 
     * @return the value of field 'receiver'.
     */
    public org.exolab.jmscts.core.types.ReceiverType getReceiver()
    {
        return this._receiver;
    } //-- org.exolab.jmscts.core.types.ReceiverType getReceiver() 

    /**
     * Returns the value of field 'session'.
     * 
     * @return the value of field 'session'.
     */
    public org.exolab.jmscts.core.types.SessionType getSession()
    {
        return this._session;
    } //-- org.exolab.jmscts.core.types.SessionType getSession() 

    /**
     * Returns the value of field 'test'.
     * 
     * @return the value of field 'test'.
     */
    public java.lang.String getTest()
    {
        return this._test;
    } //-- java.lang.String getTest() 

    /**
     * Method isValid
     */
    public boolean isValid()
    {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    } //-- boolean isValid() 

    /**
     * Sets the value of field 'deliveryMode'.
     * 
     * @param deliveryMode the value of field 'deliveryMode'.
     */
    public void setDeliveryMode(org.exolab.jmscts.core.types.DeliveryModeType deliveryMode)
    {
        this._deliveryMode = deliveryMode;
    } //-- void setDeliveryMode(org.exolab.jmscts.core.types.DeliveryModeType) 

    /**
     * Sets the value of field 'destination'.
     * 
     * @param destination the value of field 'destination'.
     */
    public void setDestination(org.exolab.jmscts.core.types.DestinationType destination)
    {
        this._destination = destination;
    } //-- void setDestination(org.exolab.jmscts.core.types.DestinationType) 

    /**
     * Sets the value of field 'factory'.
     * 
     * @param factory the value of field 'factory'.
     */
    public void setFactory(org.exolab.jmscts.core.types.FactoryType factory)
    {
        this._factory = factory;
    } //-- void setFactory(org.exolab.jmscts.core.types.FactoryType) 

    /**
     * Sets the value of field 'message'.
     * 
     * @param message the value of field 'message'.
     */
    public void setMessage(org.exolab.jmscts.core.types.MessageType message)
    {
        this._message = message;
    } //-- void setMessage(org.exolab.jmscts.core.types.MessageType) 

    /**
     * Sets the value of field 'receiver'.
     * 
     * @param receiver the value of field 'receiver'.
     */
    public void setReceiver(org.exolab.jmscts.core.types.ReceiverType receiver)
    {
        this._receiver = receiver;
    } //-- void setReceiver(org.exolab.jmscts.core.types.ReceiverType) 

    /**
     * Sets the value of field 'session'.
     * 
     * @param session the value of field 'session'.
     */
    public void setSession(org.exolab.jmscts.core.types.SessionType session)
    {
        this._session = session;
    } //-- void setSession(org.exolab.jmscts.core.types.SessionType) 

    /**
     * Sets the value of field 'test'.
     * 
     * @param test the value of field 'test'.
     */
    public void setTest(java.lang.String test)
    {
        this._test = test;
    } //-- void setTest(java.lang.String) 

    /**
     * Method validate
     */
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
