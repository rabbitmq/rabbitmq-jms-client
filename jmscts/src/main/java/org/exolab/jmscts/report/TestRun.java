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
 * This element describes the results of a single test run.
 *  
 * 
 * @version $Revision$ $Date$
 */
public class TestRun implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * This element specifies the context of a test case.
     *  
     */
    private org.exolab.jmscts.report.Context _context;

    /**
     * This element describes a test case failure.
     *  
     */
    private org.exolab.jmscts.report.Failure _failure;

    /**
     * A statistic collected during a test run.
     *  
     */
    private java.util.ArrayList _statisticList;


      //----------------/
     //- Constructors -/
    //----------------/

    public TestRun() {
        super();
        _statisticList = new ArrayList();
    } //-- org.exolab.jmscts.report.TestRun()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addStatistic
     * 
     * @param vStatistic
     */
    public void addStatistic(org.exolab.jmscts.report.Statistic vStatistic)
        throws java.lang.IndexOutOfBoundsException
    {
        _statisticList.add(vStatistic);
    } //-- void addStatistic(org.exolab.jmscts.report.Statistic) 

    /**
     * Method addStatistic
     * 
     * @param index
     * @param vStatistic
     */
    public void addStatistic(int index, org.exolab.jmscts.report.Statistic vStatistic)
        throws java.lang.IndexOutOfBoundsException
    {
        _statisticList.add(index, vStatistic);
    } //-- void addStatistic(int, org.exolab.jmscts.report.Statistic) 

    /**
     * Method clearStatistic
     */
    public void clearStatistic()
    {
        _statisticList.clear();
    } //-- void clearStatistic() 

    /**
     * Method enumerateStatistic
     */
    public java.util.Enumeration enumerateStatistic()
    {
        return new org.exolab.castor.util.IteratorEnumeration(_statisticList.iterator());
    } //-- java.util.Enumeration enumerateStatistic() 

    /**
     * Note: hashCode() has not been overriden
     * 
     * @param obj
     */
    public boolean equals(java.lang.Object obj)
    {
        if ( this == obj )
            return true;
        
        if (obj instanceof TestRun) {
        
            TestRun temp = (TestRun)obj;
            if (this._context != null) {
                if (temp._context == null) return false;
                else if (!(this._context.equals(temp._context))) 
                    return false;
            }
            else if (temp._context != null)
                return false;
            if (this._failure != null) {
                if (temp._failure == null) return false;
                else if (!(this._failure.equals(temp._failure))) 
                    return false;
            }
            else if (temp._failure != null)
                return false;
            if (this._statisticList != null) {
                if (temp._statisticList == null) return false;
                else if (!(this._statisticList.equals(temp._statisticList))) 
                    return false;
            }
            else if (temp._statisticList != null)
                return false;
            return true;
        }
        return false;
    } //-- boolean equals(java.lang.Object) 

    /**
     * Returns the value of field 'context'. The field 'context'
     * has the following description: This element specifies the
     * context of a test case.
     *  
     * 
     * @return the value of field 'context'.
     */
    public org.exolab.jmscts.report.Context getContext()
    {
        return this._context;
    } //-- org.exolab.jmscts.report.Context getContext() 

    /**
     * Returns the value of field 'failure'. The field 'failure'
     * has the following description: This element describes a test
     * case failure.
     *  
     * 
     * @return the value of field 'failure'.
     */
    public org.exolab.jmscts.report.Failure getFailure()
    {
        return this._failure;
    } //-- org.exolab.jmscts.report.Failure getFailure() 

    /**
     * Method getStatistic
     * 
     * @param index
     */
    public org.exolab.jmscts.report.Statistic getStatistic(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _statisticList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.exolab.jmscts.report.Statistic) _statisticList.get(index);
    } //-- org.exolab.jmscts.report.Statistic getStatistic(int) 

    /**
     * Method getStatistic
     */
    public org.exolab.jmscts.report.Statistic[] getStatistic()
    {
        int size = _statisticList.size();
        org.exolab.jmscts.report.Statistic[] mArray = new org.exolab.jmscts.report.Statistic[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.exolab.jmscts.report.Statistic) _statisticList.get(index);
        }
        return mArray;
    } //-- org.exolab.jmscts.report.Statistic[] getStatistic() 

    /**
     * Method getStatisticCount
     */
    public int getStatisticCount()
    {
        return _statisticList.size();
    } //-- int getStatisticCount() 

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
     * Method removeStatistic
     * 
     * @param vStatistic
     */
    public boolean removeStatistic(org.exolab.jmscts.report.Statistic vStatistic)
    {
        boolean removed = _statisticList.remove(vStatistic);
        return removed;
    } //-- boolean removeStatistic(org.exolab.jmscts.report.Statistic) 

    /**
     * Sets the value of field 'context'. The field 'context' has
     * the following description: This element specifies the
     * context of a test case.
     *  
     * 
     * @param context the value of field 'context'.
     */
    public void setContext(org.exolab.jmscts.report.Context context)
    {
        this._context = context;
    } //-- void setContext(org.exolab.jmscts.report.Context) 

    /**
     * Sets the value of field 'failure'. The field 'failure' has
     * the following description: This element describes a test
     * case failure.
     *  
     * 
     * @param failure the value of field 'failure'.
     */
    public void setFailure(org.exolab.jmscts.report.Failure failure)
    {
        this._failure = failure;
    } //-- void setFailure(org.exolab.jmscts.report.Failure) 

    /**
     * Method setStatistic
     * 
     * @param index
     * @param vStatistic
     */
    public void setStatistic(int index, org.exolab.jmscts.report.Statistic vStatistic)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _statisticList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _statisticList.set(index, vStatistic);
    } //-- void setStatistic(int, org.exolab.jmscts.report.Statistic) 

    /**
     * Method setStatistic
     * 
     * @param statisticArray
     */
    public void setStatistic(org.exolab.jmscts.report.Statistic[] statisticArray)
    {
        //-- copy array
        _statisticList.clear();
        for (int i = 0; i < statisticArray.length; i++) {
            _statisticList.add(statisticArray[i]);
        }
    } //-- void setStatistic(org.exolab.jmscts.report.Statistic) 

    /**
     * Method unmarshal
     * 
     * @param reader
     */
    public static org.exolab.jmscts.report.TestRun unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.exolab.jmscts.report.TestRun) Unmarshaller.unmarshal(org.exolab.jmscts.report.TestRun.class, reader);
    } //-- org.exolab.jmscts.report.TestRun unmarshal(java.io.Reader) 

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
