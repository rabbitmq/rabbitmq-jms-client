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
 * This element lists the tests which have covered a particular 
 *  requirement, and a count of any failures that occurred.
 *  
 * 
 * @version $Revision$ $Date$
 */
public class Coverage implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _runs
     */
    private int _runs;

    /**
     * keeps track of state for field: _runs
     */
    private boolean _has_runs;

    /**
     * Field _failures
     */
    private int _failures;

    /**
     * keeps track of state for field: _failures
     */
    private boolean _has_failures;

    /**
     * Field _supported
     */
    private boolean _supported;

    /**
     * keeps track of state for field: _supported
     */
    private boolean _has_supported;

    /**
     * Field _requirementId
     */
    private java.lang.String _requirementId;

    /**
     * Field _testList
     */
    private java.util.ArrayList _testList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Coverage() {
        super();
        _testList = new ArrayList();
    } //-- org.exolab.jmscts.report.Coverage()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addTest
     * 
     * @param vTest
     */
    public void addTest(java.lang.String vTest)
        throws java.lang.IndexOutOfBoundsException
    {
        _testList.add(vTest);
    } //-- void addTest(java.lang.String) 

    /**
     * Method addTest
     * 
     * @param index
     * @param vTest
     */
    public void addTest(int index, java.lang.String vTest)
        throws java.lang.IndexOutOfBoundsException
    {
        _testList.add(index, vTest);
    } //-- void addTest(int, java.lang.String) 

    /**
     * Method clearTest
     */
    public void clearTest()
    {
        _testList.clear();
    } //-- void clearTest() 

    /**
     * Method deleteSupported
     */
    public void deleteSupported()
    {
        this._has_supported= false;
    } //-- void deleteSupported() 

    /**
     * Method enumerateTest
     */
    public java.util.Enumeration enumerateTest()
    {
        return new org.exolab.castor.util.IteratorEnumeration(_testList.iterator());
    } //-- java.util.Enumeration enumerateTest() 

    /**
     * Note: hashCode() has not been overriden
     * 
     * @param obj
     */
    public boolean equals(java.lang.Object obj)
    {
        if ( this == obj )
            return true;
        
        if (obj instanceof Coverage) {
        
            Coverage temp = (Coverage)obj;
            if (this._runs != temp._runs)
                return false;
            if (this._has_runs != temp._has_runs)
                return false;
            if (this._failures != temp._failures)
                return false;
            if (this._has_failures != temp._has_failures)
                return false;
            if (this._supported != temp._supported)
                return false;
            if (this._has_supported != temp._has_supported)
                return false;
            if (this._requirementId != null) {
                if (temp._requirementId == null) return false;
                else if (!(this._requirementId.equals(temp._requirementId))) 
                    return false;
            }
            else if (temp._requirementId != null)
                return false;
            if (this._testList != null) {
                if (temp._testList == null) return false;
                else if (!(this._testList.equals(temp._testList))) 
                    return false;
            }
            else if (temp._testList != null)
                return false;
            return true;
        }
        return false;
    } //-- boolean equals(java.lang.Object) 

    /**
     * Returns the value of field 'failures'.
     * 
     * @return the value of field 'failures'.
     */
    public int getFailures()
    {
        return this._failures;
    } //-- int getFailures() 

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
     * Returns the value of field 'runs'.
     * 
     * @return the value of field 'runs'.
     */
    public int getRuns()
    {
        return this._runs;
    } //-- int getRuns() 

    /**
     * Returns the value of field 'supported'.
     * 
     * @return the value of field 'supported'.
     */
    public boolean getSupported()
    {
        return this._supported;
    } //-- boolean getSupported() 

    /**
     * Method getTest
     * 
     * @param index
     */
    public java.lang.String getTest(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _testList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (String)_testList.get(index);
    } //-- java.lang.String getTest(int) 

    /**
     * Method getTest
     */
    public java.lang.String[] getTest()
    {
        int size = _testList.size();
        java.lang.String[] mArray = new java.lang.String[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (String)_testList.get(index);
        }
        return mArray;
    } //-- java.lang.String[] getTest() 

    /**
     * Method getTestCount
     */
    public int getTestCount()
    {
        return _testList.size();
    } //-- int getTestCount() 

    /**
     * Method hasFailures
     */
    public boolean hasFailures()
    {
        return this._has_failures;
    } //-- boolean hasFailures() 

    /**
     * Method hasRuns
     */
    public boolean hasRuns()
    {
        return this._has_runs;
    } //-- boolean hasRuns() 

    /**
     * Method hasSupported
     */
    public boolean hasSupported()
    {
        return this._has_supported;
    } //-- boolean hasSupported() 

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
     * Method removeTest
     * 
     * @param vTest
     */
    public boolean removeTest(java.lang.String vTest)
    {
        boolean removed = _testList.remove(vTest);
        return removed;
    } //-- boolean removeTest(java.lang.String) 

    /**
     * Sets the value of field 'failures'.
     * 
     * @param failures the value of field 'failures'.
     */
    public void setFailures(int failures)
    {
        this._failures = failures;
        this._has_failures = true;
    } //-- void setFailures(int) 

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
     * Sets the value of field 'runs'.
     * 
     * @param runs the value of field 'runs'.
     */
    public void setRuns(int runs)
    {
        this._runs = runs;
        this._has_runs = true;
    } //-- void setRuns(int) 

    /**
     * Sets the value of field 'supported'.
     * 
     * @param supported the value of field 'supported'.
     */
    public void setSupported(boolean supported)
    {
        this._supported = supported;
        this._has_supported = true;
    } //-- void setSupported(boolean) 

    /**
     * Method setTest
     * 
     * @param index
     * @param vTest
     */
    public void setTest(int index, java.lang.String vTest)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _testList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _testList.set(index, vTest);
    } //-- void setTest(int, java.lang.String) 

    /**
     * Method setTest
     * 
     * @param testArray
     */
    public void setTest(java.lang.String[] testArray)
    {
        //-- copy array
        _testList.clear();
        for (int i = 0; i < testArray.length; i++) {
            _testList.add(testArray[i]);
        }
    } //-- void setTest(java.lang.String) 

    /**
     * Method unmarshal
     * 
     * @param reader
     */
    public static org.exolab.jmscts.report.Coverage unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.exolab.jmscts.report.Coverage) Unmarshaller.unmarshal(org.exolab.jmscts.report.Coverage.class, reader);
    } //-- org.exolab.jmscts.report.Coverage unmarshal(java.io.Reader) 

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
