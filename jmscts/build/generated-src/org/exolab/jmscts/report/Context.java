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
import org.exolab.jmscts.core.types.MessageType;
import org.exolab.jmscts.core.types.SessionType;
import org.xml.sax.ContentHandler;

/**
 * This element specifies the context of a test case.
 *  
 * 
 * @version $Revision$ $Date$
 */
public class Context implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _factory
     */
    private org.exolab.jmscts.report.Factory _factory;

    /**
     * Field _session
     */
    private org.exolab.jmscts.core.types.SessionType _session;

    /**
     * Field _message
     */
    private org.exolab.jmscts.core.types.MessageType _message;

    /**
     * Field _behaviour
     */
    private org.exolab.jmscts.report.Behaviour _behaviour;


      //----------------/
     //- Constructors -/
    //----------------/

    public Context() {
        super();
    } //-- org.exolab.jmscts.report.Context()


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
        
        if (obj instanceof Context) {
        
            Context temp = (Context)obj;
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
            if (this._message != null) {
                if (temp._message == null) return false;
                else if (!(this._message.equals(temp._message))) 
                    return false;
            }
            else if (temp._message != null)
                return false;
            if (this._behaviour != null) {
                if (temp._behaviour == null) return false;
                else if (!(this._behaviour.equals(temp._behaviour))) 
                    return false;
            }
            else if (temp._behaviour != null)
                return false;
            return true;
        }
        return false;
    } //-- boolean equals(java.lang.Object) 

    /**
     * Returns the value of field 'behaviour'.
     * 
     * @return the value of field 'behaviour'.
     */
    public org.exolab.jmscts.report.Behaviour getBehaviour()
    {
        return this._behaviour;
    } //-- org.exolab.jmscts.report.Behaviour getBehaviour() 

    /**
     * Returns the value of field 'factory'.
     * 
     * @return the value of field 'factory'.
     */
    public org.exolab.jmscts.report.Factory getFactory()
    {
        return this._factory;
    } //-- org.exolab.jmscts.report.Factory getFactory() 

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
     * Returns the value of field 'session'.
     * 
     * @return the value of field 'session'.
     */
    public org.exolab.jmscts.core.types.SessionType getSession()
    {
        return this._session;
    } //-- org.exolab.jmscts.core.types.SessionType getSession() 

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
     * Sets the value of field 'behaviour'.
     * 
     * @param behaviour the value of field 'behaviour'.
     */
    public void setBehaviour(org.exolab.jmscts.report.Behaviour behaviour)
    {
        this._behaviour = behaviour;
    } //-- void setBehaviour(org.exolab.jmscts.report.Behaviour) 

    /**
     * Sets the value of field 'factory'.
     * 
     * @param factory the value of field 'factory'.
     */
    public void setFactory(org.exolab.jmscts.report.Factory factory)
    {
        this._factory = factory;
    } //-- void setFactory(org.exolab.jmscts.report.Factory) 

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
     * Sets the value of field 'session'.
     * 
     * @param session the value of field 'session'.
     */
    public void setSession(org.exolab.jmscts.core.types.SessionType session)
    {
        this._session = session;
    } //-- void setSession(org.exolab.jmscts.core.types.SessionType) 

    /**
     * Method unmarshal
     * 
     * @param reader
     */
    public static org.exolab.jmscts.report.Context unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.exolab.jmscts.report.Context) Unmarshaller.unmarshal(org.exolab.jmscts.report.Context.class, reader);
    } //-- org.exolab.jmscts.report.Context unmarshal(java.io.Reader) 

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
