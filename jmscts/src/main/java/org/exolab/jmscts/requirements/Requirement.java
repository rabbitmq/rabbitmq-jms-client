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
import java.util.ArrayList;
import java.util.Enumeration;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

/**
 * This element specifies a requirement, defined by the JMS
 * specification
 *  or associated API documentation. A requirement may be optional.
 *  
 * 
 * @version $Revision$ $Date$
 */
public class Requirement implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _requirementId
     */
    private java.lang.String _requirementId;

    /**
     * Field _optional
     */
    private boolean _optional;

    /**
     * keeps track of state for field: _optional
     */
    private boolean _has_optional;

    /**
     * Field _description
     */
    private Description _description;

    /**
     * Field _requirementChoiceList
     */
    private java.util.ArrayList _requirementChoiceList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Requirement() {
        super();
        _requirementChoiceList = new ArrayList();
    } //-- org.exolab.jmscts.requirements.Requirement()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addRequirementChoice
     * 
     * @param vRequirementChoice
     */
    public void addRequirementChoice(org.exolab.jmscts.requirements.RequirementChoice vRequirementChoice)
        throws java.lang.IndexOutOfBoundsException
    {
        _requirementChoiceList.add(vRequirementChoice);
    } //-- void addRequirementChoice(org.exolab.jmscts.requirements.RequirementChoice) 

    /**
     * Method addRequirementChoice
     * 
     * @param index
     * @param vRequirementChoice
     */
    public void addRequirementChoice(int index, org.exolab.jmscts.requirements.RequirementChoice vRequirementChoice)
        throws java.lang.IndexOutOfBoundsException
    {
        _requirementChoiceList.add(index, vRequirementChoice);
    } //-- void addRequirementChoice(int, org.exolab.jmscts.requirements.RequirementChoice) 

    /**
     * Method clearRequirementChoice
     */
    public void clearRequirementChoice()
    {
        _requirementChoiceList.clear();
    } //-- void clearRequirementChoice() 

    /**
     * Method deleteOptional
     */
    public void deleteOptional()
    {
        this._has_optional= false;
    } //-- void deleteOptional() 

    /**
     * Method enumerateRequirementChoice
     */
    public java.util.Enumeration enumerateRequirementChoice()
    {
        return new org.exolab.castor.util.IteratorEnumeration(_requirementChoiceList.iterator());
    } //-- java.util.Enumeration enumerateRequirementChoice() 

    /**
     * Note: hashCode() has not been overriden
     * 
     * @param obj
     */
    public boolean equals(java.lang.Object obj)
    {
        if ( this == obj )
            return true;
        
        if (obj instanceof Requirement) {
        
            Requirement temp = (Requirement)obj;
            if (this._requirementId != null) {
                if (temp._requirementId == null) return false;
                else if (!(this._requirementId.equals(temp._requirementId))) 
                    return false;
            }
            else if (temp._requirementId != null)
                return false;
            if (this._optional != temp._optional)
                return false;
            if (this._has_optional != temp._has_optional)
                return false;
            if (this._description != null) {
                if (temp._description == null) return false;
                else if (!(this._description.equals(temp._description))) 
                    return false;
            }
            else if (temp._description != null)
                return false;
            if (this._requirementChoiceList != null) {
                if (temp._requirementChoiceList == null) return false;
                else if (!(this._requirementChoiceList.equals(temp._requirementChoiceList))) 
                    return false;
            }
            else if (temp._requirementChoiceList != null)
                return false;
            return true;
        }
        return false;
    } //-- boolean equals(java.lang.Object) 

    /**
     * Returns the value of field 'description'.
     * 
     * @return the value of field 'description'.
     */
    public Description getDescription()
    {
        return this._description;
    } //-- Description getDescription() 

    /**
     * Returns the value of field 'optional'.
     * 
     * @return the value of field 'optional'.
     */
    public boolean getOptional()
    {
        return this._optional;
    } //-- boolean getOptional() 

    /**
     * Method getRequirementChoice
     * 
     * @param index
     */
    public org.exolab.jmscts.requirements.RequirementChoice getRequirementChoice(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _requirementChoiceList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.exolab.jmscts.requirements.RequirementChoice) _requirementChoiceList.get(index);
    } //-- org.exolab.jmscts.requirements.RequirementChoice getRequirementChoice(int) 

    /**
     * Method getRequirementChoice
     */
    public org.exolab.jmscts.requirements.RequirementChoice[] getRequirementChoice()
    {
        int size = _requirementChoiceList.size();
        org.exolab.jmscts.requirements.RequirementChoice[] mArray = new org.exolab.jmscts.requirements.RequirementChoice[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.exolab.jmscts.requirements.RequirementChoice) _requirementChoiceList.get(index);
        }
        return mArray;
    } //-- org.exolab.jmscts.requirements.RequirementChoice[] getRequirementChoice() 

    /**
     * Method getRequirementChoiceCount
     */
    public int getRequirementChoiceCount()
    {
        return _requirementChoiceList.size();
    } //-- int getRequirementChoiceCount() 

    /**
     * Returns the value of field 'requirementId'.
     * 
     * @return the value of field 'requirementId'.
     */
    public java.lang.String getRequirementId()
    {
        return this._requirementId;
    } //-- java.lang.String getRequirementId() 

    /**
     * Method hasOptional
     */
    public boolean hasOptional()
    {
        return this._has_optional;
    } //-- boolean hasOptional() 

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
     * Method removeRequirementChoice
     * 
     * @param vRequirementChoice
     */
    public boolean removeRequirementChoice(org.exolab.jmscts.requirements.RequirementChoice vRequirementChoice)
    {
        boolean removed = _requirementChoiceList.remove(vRequirementChoice);
        return removed;
    } //-- boolean removeRequirementChoice(org.exolab.jmscts.requirements.RequirementChoice) 

    /**
     * Sets the value of field 'description'.
     * 
     * @param description the value of field 'description'.
     */
    public void setDescription(Description description)
    {
        this._description = description;
    } //-- void setDescription(Description) 

    /**
     * Sets the value of field 'optional'.
     * 
     * @param optional the value of field 'optional'.
     */
    public void setOptional(boolean optional)
    {
        this._optional = optional;
        this._has_optional = true;
    } //-- void setOptional(boolean) 

    /**
     * Method setRequirementChoice
     * 
     * @param index
     * @param vRequirementChoice
     */
    public void setRequirementChoice(int index, org.exolab.jmscts.requirements.RequirementChoice vRequirementChoice)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _requirementChoiceList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _requirementChoiceList.set(index, vRequirementChoice);
    } //-- void setRequirementChoice(int, org.exolab.jmscts.requirements.RequirementChoice) 

    /**
     * Method setRequirementChoice
     * 
     * @param requirementChoiceArray
     */
    public void setRequirementChoice(org.exolab.jmscts.requirements.RequirementChoice[] requirementChoiceArray)
    {
        //-- copy array
        _requirementChoiceList.clear();
        for (int i = 0; i < requirementChoiceArray.length; i++) {
            _requirementChoiceList.add(requirementChoiceArray[i]);
        }
    } //-- void setRequirementChoice(org.exolab.jmscts.requirements.RequirementChoice) 

    /**
     * Sets the value of field 'requirementId'.
     * 
     * @param requirementId the value of field 'requirementId'.
     */
    public void setRequirementId(java.lang.String requirementId)
    {
        this._requirementId = requirementId;
    } //-- void setRequirementId(java.lang.String) 

    /**
     * Method unmarshal
     * 
     * @param reader
     */
    public static org.exolab.jmscts.requirements.Requirement unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.exolab.jmscts.requirements.Requirement) Unmarshaller.unmarshal(org.exolab.jmscts.requirements.Requirement.class, reader);
    } //-- org.exolab.jmscts.requirements.Requirement unmarshal(java.io.Reader) 

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
