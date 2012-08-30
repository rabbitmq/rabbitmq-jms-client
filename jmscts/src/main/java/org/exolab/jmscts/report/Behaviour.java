/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.4.3</a>, using an XML
 * Schema.
 * $Id$
 */

package org.exolab.jmscts.report;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.jmscts.core.types.DeliveryModeType;
import org.exolab.jmscts.core.types.DestinationType;
import org.exolab.jmscts.core.types.ReceiverType;
import org.xml.sax.ContentHandler;

/**
 * Class Behaviour.
 * 
 * @version $Revision$ $Date$
 */
public class Behaviour implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _deliveryMode
     */
    private org.exolab.jmscts.core.types.DeliveryModeType _deliveryMode;

    /**
     * Field _receiver
     */
    private org.exolab.jmscts.core.types.ReceiverType _receiver;

    /**
     * Field _destination
     */
    private org.exolab.jmscts.core.types.DestinationType _destination;


      //----------------/
     //- Constructors -/
    //----------------/

    public Behaviour() {
        super();
    } //-- org.exolab.jmscts.report.Behaviour()


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
        
        if (obj instanceof Behaviour) {
        
            Behaviour temp = (Behaviour)obj;
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
            if (this._destination != null) {
                if (temp._destination == null) return false;
                else if (!(this._destination.equals(temp._destination))) 
                    return false;
            }
            else if (temp._destination != null)
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
     * Returns the value of field 'receiver'.
     * 
     * @return the value of field 'receiver'.
     */
    public org.exolab.jmscts.core.types.ReceiverType getReceiver()
    {
        return this._receiver;
    } //-- org.exolab.jmscts.core.types.ReceiverType getReceiver() 

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
     * Method marshal
     * 
     * @param out
     */
    public void marshal(java.io.Writer out)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * Method marshal
     * 
     * @param handler
     */
    public void marshal(org.xml.sax.ContentHandler handler)
        throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

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
     * Sets the value of field 'receiver'.
     * 
     * @param receiver the value of field 'receiver'.
     */
    public void setReceiver(org.exolab.jmscts.core.types.ReceiverType receiver)
    {
        this._receiver = receiver;
    } //-- void setReceiver(org.exolab.jmscts.core.types.ReceiverType) 

    /**
     * Method unmarshal
     * 
     * @param reader
     */
    public static org.exolab.jmscts.report.Behaviour unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.exolab.jmscts.report.Behaviour) Unmarshaller.unmarshal(org.exolab.jmscts.report.Behaviour.class, reader);
    } //-- org.exolab.jmscts.report.Behaviour unmarshal(java.io.Reader) 

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
