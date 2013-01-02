/*
 * This class was automatically generated with
 * <a href="http://www.castor.org">Castor 0.9.4.3</a>, using an XML
 * Schema.
 * $Id$
 */

package org.exolab.jmscts.core.meta;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.ArrayList;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class ClassMeta.
 *
 * @version $Revision$ $Date$
 */
public class ClassMeta extends org.exolab.jmscts.core.meta.Meta
implements java.io.Serializable
{


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /** TODO */
    private static final long serialVersionUID = 1L;
    /**
     * Field _methodMetaList
     */
    private java.util.ArrayList<MethodMeta> _methodMetaList;


      //----------------/
     //- Constructors -/
    //----------------/

    public ClassMeta() {
        super();
        _methodMetaList = new ArrayList<MethodMeta>();
    } //-- org.exolab.jmscts.core.meta.ClassMeta()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addMethodMeta
     *
     * @param vMethodMeta
     */
    public void addMethodMeta(org.exolab.jmscts.core.meta.MethodMeta vMethodMeta)
        throws java.lang.IndexOutOfBoundsException
    {
        _methodMetaList.add(vMethodMeta);
    } //-- void addMethodMeta(org.exolab.jmscts.core.meta.MethodMeta)

    /**
     * Method addMethodMeta
     *
     * @param index
     * @param vMethodMeta
     */
    public void addMethodMeta(int index, org.exolab.jmscts.core.meta.MethodMeta vMethodMeta)
        throws java.lang.IndexOutOfBoundsException
    {
        _methodMetaList.add(index, vMethodMeta);
    } //-- void addMethodMeta(int, org.exolab.jmscts.core.meta.MethodMeta)

    /**
     * Method clearMethodMeta
     */
    public void clearMethodMeta()
    {
        _methodMetaList.clear();
    } //-- void clearMethodMeta()

    /**
     * Method enumerateMethodMeta
     */
    @SuppressWarnings("unchecked")
    public java.util.Enumeration<MethodMeta> enumerateMethodMeta()
    {
        return new org.exolab.castor.util.IteratorEnumeration(_methodMetaList.iterator());
    } //-- java.util.Enumeration enumerateMethodMeta()

    /**
     * Note: hashCode() has not been overriden
     *
     * @param obj
     */
    public boolean equals(java.lang.Object obj)
    {
        if ( this == obj )
            return true;

        if (super.equals(obj)==false)
            return false;

        if (obj instanceof ClassMeta) {

            ClassMeta temp = (ClassMeta)obj;
            if (this._methodMetaList != null) {
                if (temp._methodMetaList == null) return false;
                else if (!(this._methodMetaList.equals(temp._methodMetaList)))
                    return false;
            }
            else if (temp._methodMetaList != null)
                return false;
            return true;
        }
        return false;
    } //-- boolean equals(java.lang.Object)

    /**
     * Method getMethodMeta
     *
     * @param index
     */
    public org.exolab.jmscts.core.meta.MethodMeta getMethodMeta(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _methodMetaList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return _methodMetaList.get(index);
    } //-- org.exolab.jmscts.core.meta.MethodMeta getMethodMeta(int)

    /**
     * Method getMethodMeta
     */
    public org.exolab.jmscts.core.meta.MethodMeta[] getMethodMeta()
    {
        int size = _methodMetaList.size();
        org.exolab.jmscts.core.meta.MethodMeta[] mArray = new org.exolab.jmscts.core.meta.MethodMeta[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = _methodMetaList.get(index);
        }
        return mArray;
    } //-- org.exolab.jmscts.core.meta.MethodMeta[] getMethodMeta()

    /**
     * Method getMethodMetaCount
     */
    public int getMethodMetaCount()
    {
        return _methodMetaList.size();
    } //-- int getMethodMetaCount()

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
     * Method removeMethodMeta
     *
     * @param vMethodMeta
     */
    public boolean removeMethodMeta(org.exolab.jmscts.core.meta.MethodMeta vMethodMeta)
    {
        boolean removed = _methodMetaList.remove(vMethodMeta);
        return removed;
    } //-- boolean removeMethodMeta(org.exolab.jmscts.core.meta.MethodMeta)

    /**
     * Method setMethodMeta
     *
     * @param index
     * @param vMethodMeta
     */
    public void setMethodMeta(int index, org.exolab.jmscts.core.meta.MethodMeta vMethodMeta)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _methodMetaList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _methodMetaList.set(index, vMethodMeta);
    } //-- void setMethodMeta(int, org.exolab.jmscts.core.meta.MethodMeta)

    /**
     * Method setMethodMeta
     *
     * @param methodMetaArray
     */
    public void setMethodMeta(org.exolab.jmscts.core.meta.MethodMeta[] methodMetaArray)
    {
        //-- copy array
        _methodMetaList.clear();
        for (int i = 0; i < methodMetaArray.length; i++) {
            _methodMetaList.add(methodMetaArray[i]);
        }
    } //-- void setMethodMeta(org.exolab.jmscts.core.meta.MethodMeta)

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static org.exolab.jmscts.core.meta.ClassMeta unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.exolab.jmscts.core.meta.ClassMeta) Unmarshaller.unmarshal(org.exolab.jmscts.core.meta.ClassMeta.class, reader);
    } //-- org.exolab.jmscts.core.meta.ClassMeta unmarshal(java.io.Reader)

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
