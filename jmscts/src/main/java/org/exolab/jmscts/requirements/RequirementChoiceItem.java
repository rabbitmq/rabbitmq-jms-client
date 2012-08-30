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
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class RequirementChoiceItem.
 * 
 * @version $Revision$ $Date$
 */
public class RequirementChoiceItem implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _referenceId
     */
    private java.lang.String _referenceId;

    /**
     * This element specifies a requirement reference.
     *  
     */
    private org.exolab.jmscts.requirements.Reference _reference;


      //----------------/
     //- Constructors -/
    //----------------/

    public RequirementChoiceItem() {
        super();
    } //-- org.exolab.jmscts.requirements.RequirementChoiceItem()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'reference'. The field
     * 'reference' has the following description: This element
     * specifies a requirement reference.
     *  
     * 
     * @return the value of field 'reference'.
     */
    public org.exolab.jmscts.requirements.Reference getReference()
    {
        return this._reference;
    } //-- org.exolab.jmscts.requirements.Reference getReference() 

    /**
     * Returns the value of field 'referenceId'.
     * 
     * @return the value of field 'referenceId'.
     */
    public java.lang.String getReferenceId()
    {
        return this._referenceId;
    } //-- java.lang.String getReferenceId() 

    /**
     * Sets the value of field 'reference'. The field 'reference'
     * has the following description: This element specifies a
     * requirement reference.
     *  
     * 
     * @param reference the value of field 'reference'.
     */
    public void setReference(org.exolab.jmscts.requirements.Reference reference)
    {
        this._reference = reference;
    } //-- void setReference(org.exolab.jmscts.requirements.Reference) 

    /**
     * Sets the value of field 'referenceId'.
     * 
     * @param referenceId the value of field 'referenceId'.
     */
    public void setReferenceId(java.lang.String referenceId)
    {
        this._referenceId = referenceId;
    } //-- void setReferenceId(java.lang.String) 

}
