/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.4.3</a>, using an XML
 * Schema.
 * $Id$
 */

package org.exolab.jmscts.requirements;

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
 * This element specifies a requirement reference.
 *  
 * 
 * @version $Revision$ $Date$
 */
public class Reference implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _referenceId
     */
    private java.lang.String _referenceId;

    /**
     * Field _section
     */
    private Section _section;

    /**
     * Field _table
     */
    private java.lang.String _table;

    /**
     * Field _url
     */
    private java.lang.String _url;


      //----------------/
     //- Constructors -/
    //----------------/

    public Reference() {
        super();
    } //-- org.exolab.jmscts.requirements.Reference()


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
        
        if (obj instanceof Reference) {
        
            Reference temp = (Reference)obj;
            if (this._referenceId != null) {
                if (temp._referenceId == null) return false;
                else if (!(this._referenceId.equals(temp._referenceId))) 
                    return false;
            }
            else if (temp._referenceId != null)
                return false;
            if (this._section != null) {
                if (temp._section == null) return false;
                else if (!(this._section.equals(temp._section))) 
                    return false;
            }
            else if (temp._section != null)
                return false;
            if (this._table != null) {
                if (temp._table == null) return false;
                else if (!(this._table.equals(temp._table))) 
                    return false;
            }
            else if (temp._table != null)
                return false;
            if (this._url != null) {
                if (temp._url == null) return false;
                else if (!(this._url.equals(temp._url))) 
                    return false;
            }
            else if (temp._url != null)
                return false;
            return true;
        }
        return false;
    } //-- boolean equals(java.lang.Object) 

    /**
     * Returns the value of field 'referenceId'.
     * 
     * @return the value of field 'referenceId'.
     */
    public java.lang.String getReferenceId()
    {
        return this._referenceId;
    } //-- java.lang.String getReferenceId() 

    /**
     * Returns the value of field 'section'.
     * 
     * @return the value of field 'section'.
     */
    public Section getSection()
    {
        return this._section;
    } //-- Section getSection() 

    /**
     * Returns the value of field 'table'.
     * 
     * @return the value of field 'table'.
     */
    public java.lang.String getTable()
    {
        return this._table;
    } //-- java.lang.String getTable() 

    /**
     * Returns the value of field 'url'.
     * 
     * @return the value of field 'url'.
     */
    public java.lang.String getUrl()
    {
        return this._url;
    } //-- java.lang.String getUrl() 

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
     * Sets the value of field 'referenceId'.
     * 
     * @param referenceId the value of field 'referenceId'.
     */
    public void setReferenceId(java.lang.String referenceId)
    {
        this._referenceId = referenceId;
    } //-- void setReferenceId(java.lang.String) 

    /**
     * Sets the value of field 'section'.
     * 
     * @param section the value of field 'section'.
     */
    public void setSection(Section section)
    {
        this._section = section;
    } //-- void setSection(Section) 

    /**
     * Sets the value of field 'table'.
     * 
     * @param table the value of field 'table'.
     */
    public void setTable(java.lang.String table)
    {
        this._table = table;
    } //-- void setTable(java.lang.String) 

    /**
     * Sets the value of field 'url'.
     * 
     * @param url the value of field 'url'.
     */
    public void setUrl(java.lang.String url)
    {
        this._url = url;
    } //-- void setUrl(java.lang.String) 

    /**
     * Method unmarshal
     * 
     * @param reader
     */
    public static org.exolab.jmscts.requirements.Reference unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.exolab.jmscts.requirements.Reference) Unmarshaller.unmarshal(org.exolab.jmscts.requirements.Reference.class, reader);
    } //-- org.exolab.jmscts.requirements.Reference unmarshal(java.io.Reader) 

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
