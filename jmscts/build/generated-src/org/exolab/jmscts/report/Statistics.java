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
 * Test suite statistics.
 *  
 * 
 * @version $Revision$ $Date$
 */
public class Statistics implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _provider
     */
    private java.lang.String _provider;

    /**
     * This element describes the results of each run of a test.
     *  
     */
    private java.util.ArrayList _testRunsList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Statistics() {
        super();
        _testRunsList = new ArrayList();
    } //-- org.exolab.jmscts.report.Statistics()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addTestRuns
     * 
     * @param vTestRuns
     */
    public void addTestRuns(org.exolab.jmscts.report.TestRuns vTestRuns)
        throws java.lang.IndexOutOfBoundsException
    {
        _testRunsList.add(vTestRuns);
    } //-- void addTestRuns(org.exolab.jmscts.report.TestRuns) 

    /**
     * Method addTestRuns
     * 
     * @param index
     * @param vTestRuns
     */
    public void addTestRuns(int index, org.exolab.jmscts.report.TestRuns vTestRuns)
        throws java.lang.IndexOutOfBoundsException
    {
        _testRunsList.add(index, vTestRuns);
    } //-- void addTestRuns(int, org.exolab.jmscts.report.TestRuns) 

    /**
     * Method clearTestRuns
     */
    public void clearTestRuns()
    {
        _testRunsList.clear();
    } //-- void clearTestRuns() 

    /**
     * Method enumerateTestRuns
     */
    public java.util.Enumeration enumerateTestRuns()
    {
        return new org.exolab.castor.util.IteratorEnumeration(_testRunsList.iterator());
    } //-- java.util.Enumeration enumerateTestRuns() 

    /**
     * Note: hashCode() has not been overriden
     * 
     * @param obj
     */
    public boolean equals(java.lang.Object obj)
    {
        if ( this == obj )
            return true;
        
        if (obj instanceof Statistics) {
        
            Statistics temp = (Statistics)obj;
            if (this._provider != null) {
                if (temp._provider == null) return false;
                else if (!(this._provider.equals(temp._provider))) 
                    return false;
            }
            else if (temp._provider != null)
                return false;
            if (this._testRunsList != null) {
                if (temp._testRunsList == null) return false;
                else if (!(this._testRunsList.equals(temp._testRunsList))) 
                    return false;
            }
            else if (temp._testRunsList != null)
                return false;
            return true;
        }
        return false;
    } //-- boolean equals(java.lang.Object) 

    /**
     * Returns the value of field 'provider'.
     * 
     * @return the value of field 'provider'.
     */
    public java.lang.String getProvider()
    {
        return this._provider;
    } //-- java.lang.String getProvider() 

    /**
     * Method getTestRuns
     * 
     * @param index
     */
    public org.exolab.jmscts.report.TestRuns getTestRuns(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _testRunsList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.exolab.jmscts.report.TestRuns) _testRunsList.get(index);
    } //-- org.exolab.jmscts.report.TestRuns getTestRuns(int) 

    /**
     * Method getTestRuns
     */
    public org.exolab.jmscts.report.TestRuns[] getTestRuns()
    {
        int size = _testRunsList.size();
        org.exolab.jmscts.report.TestRuns[] mArray = new org.exolab.jmscts.report.TestRuns[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.exolab.jmscts.report.TestRuns) _testRunsList.get(index);
        }
        return mArray;
    } //-- org.exolab.jmscts.report.TestRuns[] getTestRuns() 

    /**
     * Method getTestRunsCount
     */
    public int getTestRunsCount()
    {
        return _testRunsList.size();
    } //-- int getTestRunsCount() 

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
     * Method removeTestRuns
     * 
     * @param vTestRuns
     */
    public boolean removeTestRuns(org.exolab.jmscts.report.TestRuns vTestRuns)
    {
        boolean removed = _testRunsList.remove(vTestRuns);
        return removed;
    } //-- boolean removeTestRuns(org.exolab.jmscts.report.TestRuns) 

    /**
     * Sets the value of field 'provider'.
     * 
     * @param provider the value of field 'provider'.
     */
    public void setProvider(java.lang.String provider)
    {
        this._provider = provider;
    } //-- void setProvider(java.lang.String) 

    /**
     * Method setTestRuns
     * 
     * @param index
     * @param vTestRuns
     */
    public void setTestRuns(int index, org.exolab.jmscts.report.TestRuns vTestRuns)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _testRunsList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _testRunsList.set(index, vTestRuns);
    } //-- void setTestRuns(int, org.exolab.jmscts.report.TestRuns) 

    /**
     * Method setTestRuns
     * 
     * @param testRunsArray
     */
    public void setTestRuns(org.exolab.jmscts.report.TestRuns[] testRunsArray)
    {
        //-- copy array
        _testRunsList.clear();
        for (int i = 0; i < testRunsArray.length; i++) {
            _testRunsList.add(testRunsArray[i]);
        }
    } //-- void setTestRuns(org.exolab.jmscts.report.TestRuns) 

    /**
     * Method unmarshal
     * 
     * @param reader
     */
    public static org.exolab.jmscts.report.Statistics unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.exolab.jmscts.report.Statistics) Unmarshaller.unmarshal(org.exolab.jmscts.report.Statistics.class, reader);
    } //-- org.exolab.jmscts.report.Statistics unmarshal(java.io.Reader) 

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
