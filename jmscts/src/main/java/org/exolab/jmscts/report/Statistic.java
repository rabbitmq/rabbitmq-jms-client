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
import org.exolab.castor.types.Time;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.jmscts.report.types.StatisticType;
import org.xml.sax.ContentHandler;

/**
 * A statistic collected during a test run.
 *  
 * 
 * @version $Revision$ $Date$
 */
public class Statistic implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _type
     */
    private org.exolab.jmscts.report.types.StatisticType _type;

    /**
     * Field _count
     */
    private int _count;

    /**
     * keeps track of state for field: _count
     */
    private boolean _has_count;

    /**
     * Field _time
     */
    private org.exolab.castor.types.Time _time;

    /**
     * Field _rate
     */
    private double _rate;

    /**
     * keeps track of state for field: _rate
     */
    private boolean _has_rate;


      //----------------/
     //- Constructors -/
    //----------------/

    public Statistic() {
        super();
    } //-- org.exolab.jmscts.report.Statistic()


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
        
        if (obj instanceof Statistic) {
        
            Statistic temp = (Statistic)obj;
            if (this._type != null) {
                if (temp._type == null) return false;
                else if (!(this._type.equals(temp._type))) 
                    return false;
            }
            else if (temp._type != null)
                return false;
            if (this._count != temp._count)
                return false;
            if (this._has_count != temp._has_count)
                return false;
            if (this._time != null) {
                if (temp._time == null) return false;
                else if (!(this._time.equals(temp._time))) 
                    return false;
            }
            else if (temp._time != null)
                return false;
            if (this._rate != temp._rate)
                return false;
            if (this._has_rate != temp._has_rate)
                return false;
            return true;
        }
        return false;
    } //-- boolean equals(java.lang.Object) 

    /**
     * Returns the value of field 'count'.
     * 
     * @return the value of field 'count'.
     */
    public int getCount()
    {
        return this._count;
    } //-- int getCount() 

    /**
     * Returns the value of field 'rate'.
     * 
     * @return the value of field 'rate'.
     */
    public double getRate()
    {
        return this._rate;
    } //-- double getRate() 

    /**
     * Returns the value of field 'time'.
     * 
     * @return the value of field 'time'.
     */
    public org.exolab.castor.types.Time getTime()
    {
        return this._time;
    } //-- org.exolab.castor.types.Time getTime() 

    /**
     * Returns the value of field 'type'.
     * 
     * @return the value of field 'type'.
     */
    public org.exolab.jmscts.report.types.StatisticType getType()
    {
        return this._type;
    } //-- org.exolab.jmscts.report.types.StatisticType getType() 

    /**
     * Method hasCount
     */
    public boolean hasCount()
    {
        return this._has_count;
    } //-- boolean hasCount() 

    /**
     * Method hasRate
     */
    public boolean hasRate()
    {
        return this._has_rate;
    } //-- boolean hasRate() 

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
     * Sets the value of field 'count'.
     * 
     * @param count the value of field 'count'.
     */
    public void setCount(int count)
    {
        this._count = count;
        this._has_count = true;
    } //-- void setCount(int) 

    /**
     * Sets the value of field 'rate'.
     * 
     * @param rate the value of field 'rate'.
     */
    public void setRate(double rate)
    {
        this._rate = rate;
        this._has_rate = true;
    } //-- void setRate(double) 

    /**
     * Sets the value of field 'time'.
     * 
     * @param time the value of field 'time'.
     */
    public void setTime(org.exolab.castor.types.Time time)
    {
        this._time = time;
    } //-- void setTime(org.exolab.castor.types.Time) 

    /**
     * Sets the value of field 'type'.
     * 
     * @param type the value of field 'type'.
     */
    public void setType(org.exolab.jmscts.report.types.StatisticType type)
    {
        this._type = type;
    } //-- void setType(org.exolab.jmscts.report.types.StatisticType) 

    /**
     * Method unmarshal
     * 
     * @param reader
     */
    public static org.exolab.jmscts.report.Statistic unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.exolab.jmscts.report.Statistic) Unmarshaller.unmarshal(org.exolab.jmscts.report.Statistic.class, reader);
    } //-- org.exolab.jmscts.report.Statistic unmarshal(java.io.Reader) 

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
