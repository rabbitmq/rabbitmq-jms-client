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

import java.io.Serializable;
import java.util.ArrayList;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * The document element is the root element of all requirements
 *  documents.
 *
 *
 * @version $Revision$ $Date$
 */
public class Document implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /** TODO */
    private static final long serialVersionUID = 1L;

    /**
     * Field _description
     */
    private Description _description;

    /**
     * This element includes requirements from another requirements
     * document.
     *
     */
    private java.util.ArrayList<Serializable> _includeList;

    /**
     * This element specifies a requirement reference.
     *
     */
    private java.util.ArrayList<Serializable> _referenceList;

    /**
     * This element specifies a requirement, defined by the JMS
     * specification
     *  or associated API documentation. A requirement may be
     * optional.
     *
     */
    private java.util.ArrayList<Serializable> _requirementList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Document() {
        super();
        _includeList = new ArrayList<Serializable>();
        _referenceList = new ArrayList<Serializable>();
        _requirementList = new ArrayList<Serializable>();
    } //-- org.exolab.jmscts.requirements.Document()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addInclude
     *
     * @param vInclude
     */
    public void addInclude(org.exolab.jmscts.requirements.Include vInclude)
        throws java.lang.IndexOutOfBoundsException
    {
        _includeList.add(vInclude);
    } //-- void addInclude(org.exolab.jmscts.requirements.Include)

    /**
     * Method addInclude
     *
     * @param index
     * @param vInclude
     */
    public void addInclude(int index, org.exolab.jmscts.requirements.Include vInclude)
        throws java.lang.IndexOutOfBoundsException
    {
        _includeList.add(index, vInclude);
    } //-- void addInclude(int, org.exolab.jmscts.requirements.Include)

    /**
     * Method addReference
     *
     * @param vReference
     */
    public void addReference(org.exolab.jmscts.requirements.Reference vReference)
        throws java.lang.IndexOutOfBoundsException
    {
        _referenceList.add(vReference);
    } //-- void addReference(org.exolab.jmscts.requirements.Reference)

    /**
     * Method addReference
     *
     * @param index
     * @param vReference
     */
    public void addReference(int index, org.exolab.jmscts.requirements.Reference vReference)
        throws java.lang.IndexOutOfBoundsException
    {
        _referenceList.add(index, vReference);
    } //-- void addReference(int, org.exolab.jmscts.requirements.Reference)

    /**
     * Method addRequirement
     *
     * @param vRequirement
     */
    public void addRequirement(org.exolab.jmscts.requirements.Requirement vRequirement)
        throws java.lang.IndexOutOfBoundsException
    {
        _requirementList.add(vRequirement);
    } //-- void addRequirement(org.exolab.jmscts.requirements.Requirement)

    /**
     * Method addRequirement
     *
     * @param index
     * @param vRequirement
     */
    public void addRequirement(int index, org.exolab.jmscts.requirements.Requirement vRequirement)
        throws java.lang.IndexOutOfBoundsException
    {
        _requirementList.add(index, vRequirement);
    } //-- void addRequirement(int, org.exolab.jmscts.requirements.Requirement)

    /**
     * Method clearInclude
     */
    public void clearInclude()
    {
        _includeList.clear();
    } //-- void clearInclude()

    /**
     * Method clearReference
     */
    public void clearReference()
    {
        _referenceList.clear();
    } //-- void clearReference()

    /**
     * Method clearRequirement
     */
    public void clearRequirement()
    {
        _requirementList.clear();
    } //-- void clearRequirement()

    /**
     * Method enumerateInclude
     */
    @SuppressWarnings("unchecked")
    public java.util.Enumeration<Serializable> enumerateInclude()
    {
        return new org.exolab.castor.util.IteratorEnumeration(_includeList.iterator());
    } //-- java.util.Enumeration enumerateInclude()

    /**
     * Method enumerateReference
     */
    @SuppressWarnings("unchecked")
    public java.util.Enumeration<Serializable> enumerateReference()
    {
        return new org.exolab.castor.util.IteratorEnumeration(_referenceList.iterator());
    } //-- java.util.Enumeration enumerateReference()

    /**
     * Method enumerateRequirement
     */
    @SuppressWarnings("unchecked")
    public java.util.Enumeration<Serializable> enumerateRequirement()
    {
        return new org.exolab.castor.util.IteratorEnumeration(_requirementList.iterator());
    } //-- java.util.Enumeration enumerateRequirement()

    /**
     * Note: hashCode() has not been overriden
     *
     * @param obj
     */
    public boolean equals(java.lang.Object obj)
    {
        if ( this == obj )
            return true;

        if (obj instanceof Document) {

            Document temp = (Document)obj;
            if (this._description != null) {
                if (temp._description == null) return false;
                else if (!(this._description.equals(temp._description)))
                    return false;
            }
            else if (temp._description != null)
                return false;
            if (this._includeList != null) {
                if (temp._includeList == null) return false;
                else if (!(this._includeList.equals(temp._includeList)))
                    return false;
            }
            else if (temp._includeList != null)
                return false;
            if (this._referenceList != null) {
                if (temp._referenceList == null) return false;
                else if (!(this._referenceList.equals(temp._referenceList)))
                    return false;
            }
            else if (temp._referenceList != null)
                return false;
            if (this._requirementList != null) {
                if (temp._requirementList == null) return false;
                else if (!(this._requirementList.equals(temp._requirementList)))
                    return false;
            }
            else if (temp._requirementList != null)
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
     * Method getInclude
     *
     * @param index
     */
    public org.exolab.jmscts.requirements.Include getInclude(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _includeList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.exolab.jmscts.requirements.Include) _includeList.get(index);
    } //-- org.exolab.jmscts.requirements.Include getInclude(int)

    /**
     * Method getInclude
     */
    public org.exolab.jmscts.requirements.Include[] getInclude()
    {
        int size = _includeList.size();
        org.exolab.jmscts.requirements.Include[] mArray = new org.exolab.jmscts.requirements.Include[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.exolab.jmscts.requirements.Include) _includeList.get(index);
        }
        return mArray;
    } //-- org.exolab.jmscts.requirements.Include[] getInclude()

    /**
     * Method getIncludeCount
     */
    public int getIncludeCount()
    {
        return _includeList.size();
    } //-- int getIncludeCount()

    /**
     * Method getReference
     *
     * @param index
     */
    public org.exolab.jmscts.requirements.Reference getReference(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _referenceList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.exolab.jmscts.requirements.Reference) _referenceList.get(index);
    } //-- org.exolab.jmscts.requirements.Reference getReference(int)

    /**
     * Method getReference
     */
    public org.exolab.jmscts.requirements.Reference[] getReference()
    {
        int size = _referenceList.size();
        org.exolab.jmscts.requirements.Reference[] mArray = new org.exolab.jmscts.requirements.Reference[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.exolab.jmscts.requirements.Reference) _referenceList.get(index);
        }
        return mArray;
    } //-- org.exolab.jmscts.requirements.Reference[] getReference()

    /**
     * Method getReferenceCount
     */
    public int getReferenceCount()
    {
        return _referenceList.size();
    } //-- int getReferenceCount()

    /**
     * Method getRequirement
     *
     * @param index
     */
    public org.exolab.jmscts.requirements.Requirement getRequirement(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _requirementList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.exolab.jmscts.requirements.Requirement) _requirementList.get(index);
    } //-- org.exolab.jmscts.requirements.Requirement getRequirement(int)

    /**
     * Method getRequirement
     */
    public org.exolab.jmscts.requirements.Requirement[] getRequirement()
    {
        int size = _requirementList.size();
        org.exolab.jmscts.requirements.Requirement[] mArray = new org.exolab.jmscts.requirements.Requirement[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.exolab.jmscts.requirements.Requirement) _requirementList.get(index);
        }
        return mArray;
    } //-- org.exolab.jmscts.requirements.Requirement[] getRequirement()

    /**
     * Method getRequirementCount
     */
    public int getRequirementCount()
    {
        return _requirementList.size();
    } //-- int getRequirementCount()

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
     * Method removeInclude
     *
     * @param vInclude
     */
    public boolean removeInclude(org.exolab.jmscts.requirements.Include vInclude)
    {
        boolean removed = _includeList.remove(vInclude);
        return removed;
    } //-- boolean removeInclude(org.exolab.jmscts.requirements.Include)

    /**
     * Method removeReference
     *
     * @param vReference
     */
    public boolean removeReference(org.exolab.jmscts.requirements.Reference vReference)
    {
        boolean removed = _referenceList.remove(vReference);
        return removed;
    } //-- boolean removeReference(org.exolab.jmscts.requirements.Reference)

    /**
     * Method removeRequirement
     *
     * @param vRequirement
     */
    public boolean removeRequirement(org.exolab.jmscts.requirements.Requirement vRequirement)
    {
        boolean removed = _requirementList.remove(vRequirement);
        return removed;
    } //-- boolean removeRequirement(org.exolab.jmscts.requirements.Requirement)

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
     * Method setInclude
     *
     * @param index
     * @param vInclude
     */
    public void setInclude(int index, org.exolab.jmscts.requirements.Include vInclude)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _includeList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _includeList.set(index, vInclude);
    } //-- void setInclude(int, org.exolab.jmscts.requirements.Include)

    /**
     * Method setInclude
     *
     * @param includeArray
     */
    public void setInclude(org.exolab.jmscts.requirements.Include[] includeArray)
    {
        //-- copy array
        _includeList.clear();
        for (int i = 0; i < includeArray.length; i++) {
            _includeList.add(includeArray[i]);
        }
    } //-- void setInclude(org.exolab.jmscts.requirements.Include)

    /**
     * Method setReference
     *
     * @param index
     * @param vReference
     */
    public void setReference(int index, org.exolab.jmscts.requirements.Reference vReference)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _referenceList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _referenceList.set(index, vReference);
    } //-- void setReference(int, org.exolab.jmscts.requirements.Reference)

    /**
     * Method setReference
     *
     * @param referenceArray
     */
    public void setReference(org.exolab.jmscts.requirements.Reference[] referenceArray)
    {
        //-- copy array
        _referenceList.clear();
        for (int i = 0; i < referenceArray.length; i++) {
            _referenceList.add(referenceArray[i]);
        }
    } //-- void setReference(org.exolab.jmscts.requirements.Reference)

    /**
     * Method setRequirement
     *
     * @param index
     * @param vRequirement
     */
    public void setRequirement(int index, org.exolab.jmscts.requirements.Requirement vRequirement)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _requirementList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _requirementList.set(index, vRequirement);
    } //-- void setRequirement(int, org.exolab.jmscts.requirements.Requirement)

    /**
     * Method setRequirement
     *
     * @param requirementArray
     */
    public void setRequirement(org.exolab.jmscts.requirements.Requirement[] requirementArray)
    {
        //-- copy array
        _requirementList.clear();
        for (int i = 0; i < requirementArray.length; i++) {
            _requirementList.add(requirementArray[i]);
        }
    } //-- void setRequirement(org.exolab.jmscts.requirements.Requirement)

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static org.exolab.jmscts.requirements.Document unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.exolab.jmscts.requirements.Document) Unmarshaller.unmarshal(org.exolab.jmscts.requirements.Document.class, reader);
    } //-- org.exolab.jmscts.requirements.Document unmarshal(java.io.Reader)

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
