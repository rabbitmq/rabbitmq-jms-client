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
 * This element describes the results of each run of a test.
 *  
 * 
 * @version $Revision$ $Date$
 */
public class TestRuns implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _test
     */
    private java.lang.String _test;

    /**
     * Field _testRunList
     */
    private java.util.ArrayList _testRunList;


      //----------------/
     //- Constructors -/
    //----------------/

    public TestRuns() {
        super();
        _testRunList = new ArrayList();
    } //-- org.exolab.jmscts.report.TestRuns()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addTestRun
     * 
     * @param vTestRun
     */
    public void addTestRun(TestRun vTestRun)
        throws java.lang.IndexOutOfBoundsException
    {
        _testRunList.add(vTestRun);
    } //-- void addTestRun(TestRun) 

    /**
     * Method addTestRun
     * 
     * @param index
     * @param vTestRun
     */
    public void addTestRun(int index, TestRun vTestRun)
        throws java.lang.IndexOutOfBoundsException
    {
        _testRunList.add(index, vTestRun);
    } //-- void addTestRun(int, TestRun) 

    /**
     * Method clearTestRun
     */
    public void clearTestRun()
    {
        _testRunList.clear();
    } //-- void clearTestRun() 

    /**
     * Method enumerateTestRun
     */
    public java.util.Enumeration enumerateTestRun()
    {
        return new org.exolab.castor.util.IteratorEnumeration(_testRunList.iterator());
    } //-- java.util.Enumeration enumerateTestRun() 

    /**
     * Note: hashCode() has not been overriden
     * 
     * @param obj
     */
    public boolean equals(java.lang.Object obj)
    {
        if ( this == obj )
            return true;
        
        if (obj instanceof TestRuns) {
        
            TestRuns temp = (TestRuns)obj;
            if (this._test != null) {
                if (temp._test == null) return false;
                else if (!(this._test.equals(temp._test))) 
                    return false;
            }
            else if (temp._test != null)
                return false;
            if (this._testRunList != null) {
                if (temp._testRunList == null) return false;
                else if (!(this._testRunList.equals(temp._testRunList))) 
                    return false;
            }
            else if (temp._testRunList != null)
                return false;
            return true;
        }
        return false;
    } //-- boolean equals(java.lang.Object) 

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
     * Method getTestRun
     * 
     * @param index
     */
    public TestRun getTestRun(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _testRunList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (TestRun) _testRunList.get(index);
    } //-- TestRun getTestRun(int) 

    /**
     * Method getTestRun
     */
    public TestRun[] getTestRun()
    {
        int size = _testRunList.size();
        TestRun[] mArray = new TestRun[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (TestRun) _testRunList.get(index);
        }
        return mArray;
    } //-- TestRun[] getTestRun() 

    /**
     * Method getTestRunCount
     */
    public int getTestRunCount()
    {
        return _testRunList.size();
    } //-- int getTestRunCount() 

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
     * Method removeTestRun
     * 
     * @param vTestRun
     */
    public boolean removeTestRun(TestRun vTestRun)
    {
        boolean removed = _testRunList.remove(vTestRun);
        return removed;
    } //-- boolean removeTestRun(TestRun) 

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
     * Method setTestRun
     * 
     * @param index
     * @param vTestRun
     */
    public void setTestRun(int index, TestRun vTestRun)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _testRunList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _testRunList.set(index, vTestRun);
    } //-- void setTestRun(int, TestRun) 

    /**
     * Method setTestRun
     * 
     * @param testRunArray
     */
    public void setTestRun(TestRun[] testRunArray)
    {
        //-- copy array
        _testRunList.clear();
        for (int i = 0; i < testRunArray.length; i++) {
            _testRunList.add(testRunArray[i]);
        }
    } //-- void setTestRun(TestRun) 

    /**
     * Method unmarshal
     * 
     * @param reader
     */
    public static org.exolab.jmscts.report.TestRuns unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.exolab.jmscts.report.TestRuns) Unmarshaller.unmarshal(org.exolab.jmscts.report.TestRuns.class, reader);
    } //-- org.exolab.jmscts.report.TestRuns unmarshal(java.io.Reader) 

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
