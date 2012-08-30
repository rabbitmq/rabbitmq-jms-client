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
import java.util.ArrayList;
import java.util.Enumeration;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

/**
 * This element details the state of the current executing test
 *  
 * 
 * @version $Revision$ $Date$
 */
public class CurrentTest implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _requirementIdList
     */
    private java.util.ArrayList _requirementIdList;

    /**
     * Field _test
     */
    private java.lang.String _test;

    /**
     * This element describes the results of a single test run.
     *  
     */
    private org.exolab.jmscts.report.TestRun _testRun;


      //----------------/
     //- Constructors -/
    //----------------/

    public CurrentTest() {
        super();
        _requirementIdList = new ArrayList();
    } //-- org.exolab.jmscts.report.CurrentTest()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addRequirementId
     * 
     * @param vRequirementId
     */
    public void addRequirementId(java.lang.String vRequirementId)
        throws java.lang.IndexOutOfBoundsException
    {
        _requirementIdList.add(vRequirementId);
    } //-- void addRequirementId(java.lang.String) 

    /**
     * Method addRequirementId
     * 
     * @param index
     * @param vRequirementId
     */
    public void addRequirementId(int index, java.lang.String vRequirementId)
        throws java.lang.IndexOutOfBoundsException
    {
        _requirementIdList.add(index, vRequirementId);
    } //-- void addRequirementId(int, java.lang.String) 

    /**
     * Method clearRequirementId
     */
    public void clearRequirementId()
    {
        _requirementIdList.clear();
    } //-- void clearRequirementId() 

    /**
     * Method enumerateRequirementId
     */
    public java.util.Enumeration enumerateRequirementId()
    {
        return new org.exolab.castor.util.IteratorEnumeration(_requirementIdList.iterator());
    } //-- java.util.Enumeration enumerateRequirementId() 

    /**
     * Note: hashCode() has not been overriden
     * 
     * @param obj
     */
    public boolean equals(java.lang.Object obj)
    {
        if ( this == obj )
            return true;
        
        if (obj instanceof CurrentTest) {
        
            CurrentTest temp = (CurrentTest)obj;
            if (this._requirementIdList != null) {
                if (temp._requirementIdList == null) return false;
                else if (!(this._requirementIdList.equals(temp._requirementIdList))) 
                    return false;
            }
            else if (temp._requirementIdList != null)
                return false;
            if (this._test != null) {
                if (temp._test == null) return false;
                else if (!(this._test.equals(temp._test))) 
                    return false;
            }
            else if (temp._test != null)
                return false;
            if (this._testRun != null) {
                if (temp._testRun == null) return false;
                else if (!(this._testRun.equals(temp._testRun))) 
                    return false;
            }
            else if (temp._testRun != null)
                return false;
            return true;
        }
        return false;
    } //-- boolean equals(java.lang.Object) 

    /**
     * Method getRequirementId
     * 
     * @param index
     */
    public java.lang.String getRequirementId(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _requirementIdList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (String)_requirementIdList.get(index);
    } //-- java.lang.String getRequirementId(int) 

    /**
     * Method getRequirementId
     */
    public java.lang.String[] getRequirementId()
    {
        int size = _requirementIdList.size();
        java.lang.String[] mArray = new java.lang.String[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (String)_requirementIdList.get(index);
        }
        return mArray;
    } //-- java.lang.String[] getRequirementId() 

    /**
     * Method getRequirementIdCount
     */
    public int getRequirementIdCount()
    {
        return _requirementIdList.size();
    } //-- int getRequirementIdCount() 

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
     * Returns the value of field 'testRun'. The field 'testRun'
     * has the following description: This element describes the
     * results of a single test run.
     *  
     * 
     * @return the value of field 'testRun'.
     */
    public org.exolab.jmscts.report.TestRun getTestRun()
    {
        return this._testRun;
    } //-- org.exolab.jmscts.report.TestRun getTestRun() 

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
     * Method removeRequirementId
     * 
     * @param vRequirementId
     */
    public boolean removeRequirementId(java.lang.String vRequirementId)
    {
        boolean removed = _requirementIdList.remove(vRequirementId);
        return removed;
    } //-- boolean removeRequirementId(java.lang.String) 

    /**
     * Method setRequirementId
     * 
     * @param index
     * @param vRequirementId
     */
    public void setRequirementId(int index, java.lang.String vRequirementId)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _requirementIdList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _requirementIdList.set(index, vRequirementId);
    } //-- void setRequirementId(int, java.lang.String) 

    /**
     * Method setRequirementId
     * 
     * @param requirementIdArray
     */
    public void setRequirementId(java.lang.String[] requirementIdArray)
    {
        //-- copy array
        _requirementIdList.clear();
        for (int i = 0; i < requirementIdArray.length; i++) {
            _requirementIdList.add(requirementIdArray[i]);
        }
    } //-- void setRequirementId(java.lang.String) 

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
     * Sets the value of field 'testRun'. The field 'testRun' has
     * the following description: This element describes the
     * results of a single test run.
     *  
     * 
     * @param testRun the value of field 'testRun'.
     */
    public void setTestRun(org.exolab.jmscts.report.TestRun testRun)
    {
        this._testRun = testRun;
    } //-- void setTestRun(org.exolab.jmscts.report.TestRun) 

    /**
     * Method unmarshal
     * 
     * @param reader
     */
    public static org.exolab.jmscts.report.CurrentTest unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.exolab.jmscts.report.CurrentTest) Unmarshaller.unmarshal(org.exolab.jmscts.report.CurrentTest.class, reader);
    } //-- org.exolab.jmscts.report.CurrentTest unmarshal(java.io.Reader) 

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
