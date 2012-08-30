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
import org.xml.sax.ContentHandler;

/**
 * This element describes a test case failure.
 *  
 * 
 * @version $Revision$ $Date$
 */
public class Failure implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _description
     */
    private java.lang.String _description;

    /**
     * Field _cause
     */
    private Cause _cause;

    /**
     * Field _rootCause
     */
    private RootCause _rootCause;


      //----------------/
     //- Constructors -/
    //----------------/

    public Failure() {
        super();
    } //-- org.exolab.jmscts.report.Failure()


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
        
        if (obj instanceof Failure) {
        
            Failure temp = (Failure)obj;
            if (this._description != null) {
                if (temp._description == null) return false;
                else if (!(this._description.equals(temp._description))) 
                    return false;
            }
            else if (temp._description != null)
                return false;
            if (this._cause != null) {
                if (temp._cause == null) return false;
                else if (!(this._cause.equals(temp._cause))) 
                    return false;
            }
            else if (temp._cause != null)
                return false;
            if (this._rootCause != null) {
                if (temp._rootCause == null) return false;
                else if (!(this._rootCause.equals(temp._rootCause))) 
                    return false;
            }
            else if (temp._rootCause != null)
                return false;
            return true;
        }
        return false;
    } //-- boolean equals(java.lang.Object) 

    /**
     * Returns the value of field 'cause'.
     * 
     * @return the value of field 'cause'.
     */
    public Cause getCause()
    {
        return this._cause;
    } //-- Cause getCause() 

    /**
     * Returns the value of field 'description'.
     * 
     * @return the value of field 'description'.
     */
    public java.lang.String getDescription()
    {
        return this._description;
    } //-- java.lang.String getDescription() 

    /**
     * Returns the value of field 'rootCause'.
     * 
     * @return the value of field 'rootCause'.
     */
    public RootCause getRootCause()
    {
        return this._rootCause;
    } //-- RootCause getRootCause() 

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
     * Sets the value of field 'cause'.
     * 
     * @param cause the value of field 'cause'.
     */
    public void setCause(Cause cause)
    {
        this._cause = cause;
    } //-- void setCause(Cause) 

    /**
     * Sets the value of field 'description'.
     * 
     * @param description the value of field 'description'.
     */
    public void setDescription(java.lang.String description)
    {
        this._description = description;
    } //-- void setDescription(java.lang.String) 

    /**
     * Sets the value of field 'rootCause'.
     * 
     * @param rootCause the value of field 'rootCause'.
     */
    public void setRootCause(RootCause rootCause)
    {
        this._rootCause = rootCause;
    } //-- void setRootCause(RootCause) 

    /**
     * Method unmarshal
     * 
     * @param reader
     */
    public static org.exolab.jmscts.report.Failure unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.exolab.jmscts.report.Failure) Unmarshaller.unmarshal(org.exolab.jmscts.report.Failure.class, reader);
    } //-- org.exolab.jmscts.report.Failure unmarshal(java.io.Reader) 

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
