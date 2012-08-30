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
 * The requirementCoverage element is the root element of all test
 * case 
 *  requirement coverage documents.
 *  
 * 
 * @version $Revision$ $Date$
 */
public class RequirementCoverage implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _provider
     */
    private java.lang.String _provider;

    /**
     * This element details the state of the current executing test
     *  
     */
    private org.exolab.jmscts.report.CurrentTest _currentTest;

    /**
     * This element lists the tests which have covered a particular
     * 
     *  requirement, and a count of any failures that occurred.
     *  
     */
    private java.util.ArrayList _coverageList;

    /**
     * This element describes the results of each run of a test.
     *  
     */
    private java.util.ArrayList _testRunsList;

    /**
     * This element describes a test case failure.
     *  
     */
    private java.util.ArrayList _failureList;


      //----------------/
     //- Constructors -/
    //----------------/

    public RequirementCoverage() {
        super();
        _coverageList = new ArrayList();
        _testRunsList = new ArrayList();
        _failureList = new ArrayList();
    } //-- org.exolab.jmscts.report.RequirementCoverage()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addCoverage
     * 
     * @param vCoverage
     */
    public void addCoverage(org.exolab.jmscts.report.Coverage vCoverage)
        throws java.lang.IndexOutOfBoundsException
    {
        _coverageList.add(vCoverage);
    } //-- void addCoverage(org.exolab.jmscts.report.Coverage) 

    /**
     * Method addCoverage
     * 
     * @param index
     * @param vCoverage
     */
    public void addCoverage(int index, org.exolab.jmscts.report.Coverage vCoverage)
        throws java.lang.IndexOutOfBoundsException
    {
        _coverageList.add(index, vCoverage);
    } //-- void addCoverage(int, org.exolab.jmscts.report.Coverage) 

    /**
     * Method addFailure
     * 
     * @param vFailure
     */
    public void addFailure(org.exolab.jmscts.report.Failure vFailure)
        throws java.lang.IndexOutOfBoundsException
    {
        _failureList.add(vFailure);
    } //-- void addFailure(org.exolab.jmscts.report.Failure) 

    /**
     * Method addFailure
     * 
     * @param index
     * @param vFailure
     */
    public void addFailure(int index, org.exolab.jmscts.report.Failure vFailure)
        throws java.lang.IndexOutOfBoundsException
    {
        _failureList.add(index, vFailure);
    } //-- void addFailure(int, org.exolab.jmscts.report.Failure) 

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
     * Method clearCoverage
     */
    public void clearCoverage()
    {
        _coverageList.clear();
    } //-- void clearCoverage() 

    /**
     * Method clearFailure
     */
    public void clearFailure()
    {
        _failureList.clear();
    } //-- void clearFailure() 

    /**
     * Method clearTestRuns
     */
    public void clearTestRuns()
    {
        _testRunsList.clear();
    } //-- void clearTestRuns() 

    /**
     * Method enumerateCoverage
     */
    public java.util.Enumeration enumerateCoverage()
    {
        return new org.exolab.castor.util.IteratorEnumeration(_coverageList.iterator());
    } //-- java.util.Enumeration enumerateCoverage() 

    /**
     * Method enumerateFailure
     */
    public java.util.Enumeration enumerateFailure()
    {
        return new org.exolab.castor.util.IteratorEnumeration(_failureList.iterator());
    } //-- java.util.Enumeration enumerateFailure() 

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
        
        if (obj instanceof RequirementCoverage) {
        
            RequirementCoverage temp = (RequirementCoverage)obj;
            if (this._provider != null) {
                if (temp._provider == null) return false;
                else if (!(this._provider.equals(temp._provider))) 
                    return false;
            }
            else if (temp._provider != null)
                return false;
            if (this._currentTest != null) {
                if (temp._currentTest == null) return false;
                else if (!(this._currentTest.equals(temp._currentTest))) 
                    return false;
            }
            else if (temp._currentTest != null)
                return false;
            if (this._coverageList != null) {
                if (temp._coverageList == null) return false;
                else if (!(this._coverageList.equals(temp._coverageList))) 
                    return false;
            }
            else if (temp._coverageList != null)
                return false;
            if (this._testRunsList != null) {
                if (temp._testRunsList == null) return false;
                else if (!(this._testRunsList.equals(temp._testRunsList))) 
                    return false;
            }
            else if (temp._testRunsList != null)
                return false;
            if (this._failureList != null) {
                if (temp._failureList == null) return false;
                else if (!(this._failureList.equals(temp._failureList))) 
                    return false;
            }
            else if (temp._failureList != null)
                return false;
            return true;
        }
        return false;
    } //-- boolean equals(java.lang.Object) 

    /**
     * Method getCoverage
     * 
     * @param index
     */
    public org.exolab.jmscts.report.Coverage getCoverage(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _coverageList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.exolab.jmscts.report.Coverage) _coverageList.get(index);
    } //-- org.exolab.jmscts.report.Coverage getCoverage(int) 

    /**
     * Method getCoverage
     */
    public org.exolab.jmscts.report.Coverage[] getCoverage()
    {
        int size = _coverageList.size();
        org.exolab.jmscts.report.Coverage[] mArray = new org.exolab.jmscts.report.Coverage[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.exolab.jmscts.report.Coverage) _coverageList.get(index);
        }
        return mArray;
    } //-- org.exolab.jmscts.report.Coverage[] getCoverage() 

    /**
     * Method getCoverageCount
     */
    public int getCoverageCount()
    {
        return _coverageList.size();
    } //-- int getCoverageCount() 

    /**
     * Returns the value of field 'currentTest'. The field
     * 'currentTest' has the following description: This element
     * details the state of the current executing test
     *  
     * 
     * @return the value of field 'currentTest'.
     */
    public org.exolab.jmscts.report.CurrentTest getCurrentTest()
    {
        return this._currentTest;
    } //-- org.exolab.jmscts.report.CurrentTest getCurrentTest() 

    /**
     * Method getFailure
     * 
     * @param index
     */
    public org.exolab.jmscts.report.Failure getFailure(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _failureList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.exolab.jmscts.report.Failure) _failureList.get(index);
    } //-- org.exolab.jmscts.report.Failure getFailure(int) 

    /**
     * Method getFailure
     */
    public org.exolab.jmscts.report.Failure[] getFailure()
    {
        int size = _failureList.size();
        org.exolab.jmscts.report.Failure[] mArray = new org.exolab.jmscts.report.Failure[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.exolab.jmscts.report.Failure) _failureList.get(index);
        }
        return mArray;
    } //-- org.exolab.jmscts.report.Failure[] getFailure() 

    /**
     * Method getFailureCount
     */
    public int getFailureCount()
    {
        return _failureList.size();
    } //-- int getFailureCount() 

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
     * Method removeCoverage
     * 
     * @param vCoverage
     */
    public boolean removeCoverage(org.exolab.jmscts.report.Coverage vCoverage)
    {
        boolean removed = _coverageList.remove(vCoverage);
        return removed;
    } //-- boolean removeCoverage(org.exolab.jmscts.report.Coverage) 

    /**
     * Method removeFailure
     * 
     * @param vFailure
     */
    public boolean removeFailure(org.exolab.jmscts.report.Failure vFailure)
    {
        boolean removed = _failureList.remove(vFailure);
        return removed;
    } //-- boolean removeFailure(org.exolab.jmscts.report.Failure) 

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
     * Method setCoverage
     * 
     * @param index
     * @param vCoverage
     */
    public void setCoverage(int index, org.exolab.jmscts.report.Coverage vCoverage)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _coverageList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _coverageList.set(index, vCoverage);
    } //-- void setCoverage(int, org.exolab.jmscts.report.Coverage) 

    /**
     * Method setCoverage
     * 
     * @param coverageArray
     */
    public void setCoverage(org.exolab.jmscts.report.Coverage[] coverageArray)
    {
        //-- copy array
        _coverageList.clear();
        for (int i = 0; i < coverageArray.length; i++) {
            _coverageList.add(coverageArray[i]);
        }
    } //-- void setCoverage(org.exolab.jmscts.report.Coverage) 

    /**
     * Sets the value of field 'currentTest'. The field
     * 'currentTest' has the following description: This element
     * details the state of the current executing test
     *  
     * 
     * @param currentTest the value of field 'currentTest'.
     */
    public void setCurrentTest(org.exolab.jmscts.report.CurrentTest currentTest)
    {
        this._currentTest = currentTest;
    } //-- void setCurrentTest(org.exolab.jmscts.report.CurrentTest) 

    /**
     * Method setFailure
     * 
     * @param index
     * @param vFailure
     */
    public void setFailure(int index, org.exolab.jmscts.report.Failure vFailure)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _failureList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _failureList.set(index, vFailure);
    } //-- void setFailure(int, org.exolab.jmscts.report.Failure) 

    /**
     * Method setFailure
     * 
     * @param failureArray
     */
    public void setFailure(org.exolab.jmscts.report.Failure[] failureArray)
    {
        //-- copy array
        _failureList.clear();
        for (int i = 0; i < failureArray.length; i++) {
            _failureList.add(failureArray[i]);
        }
    } //-- void setFailure(org.exolab.jmscts.report.Failure) 

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
    public static org.exolab.jmscts.report.RequirementCoverage unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.exolab.jmscts.report.RequirementCoverage) Unmarshaller.unmarshal(org.exolab.jmscts.report.RequirementCoverage.class, reader);
    } //-- org.exolab.jmscts.report.RequirementCoverage unmarshal(java.io.Reader) 

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
