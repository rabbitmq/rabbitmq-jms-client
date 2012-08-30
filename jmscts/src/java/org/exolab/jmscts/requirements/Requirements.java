/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact tma@netspace.net.au.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001-2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: Requirements.java,v 1.3 2004/02/02 03:51:07 tanderson Exp $
 */
package org.exolab.jmscts.requirements;

import java.util.HashMap;

import org.exolab.castor.xml.ValidationException;


/**
 * This class implements a cache of {@link Requirement} and {@link Reference}
 * instances.
 *
 * @version     $Revision: 1.3 $ $Date: 2004/02/02 03:51:07 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         Requirements
 */
public class Requirements {

    /**
     * The requirements, mapped on requirement id
     */
    private HashMap<String, Requirement> _requirements = new HashMap<String, Requirement>();

    /**
     * The references, mapped on reference id
     */
    private HashMap<String, Reference> _references = new HashMap<String, Reference>();

    /**
     * An seed used to assigned identifiers to references that aren't supplied
     * with one
     */
    private int _anonymousReference = 0;

    /**
     * Get a requirement by its identifier
     *
     * @param requirementId the requirement identifier
     * @return the requirement, or null if it doesn't exist
     */
    public Requirement getRequirement(String requirementId) {
        return _requirements.get(requirementId);
    }

    /**
     * Returns the requirements as a map, with each requirement keyed on its
     * requirement id
     *
     * @return the requirements
     */
    public HashMap<String, Requirement> getRequirements() {
        return _requirements;
    }

    /**
     * Get a reference by its identifier
     *
     * @param referenceId the reference identifier
     * @return the reference, or null if it doesn't exist
     */
    public Reference getReference(String referenceId) {
        return _references.get(referenceId);
    }

    /**
     * Add a requirement
     *
     * @param requirement the requirement
     * @throws IllegalArgumentException if requirement is null
     * @throws ValidationException if the requirement is a duplicate of
     * another, contains a duplicate reference, or contains an unknown
     * reference id
     */
    public void addRequirement(Requirement requirement)
        throws ValidationException {

        if (requirement == null) {
            throw new IllegalArgumentException("Argument requirement is null");
        }

        if (_requirements.containsKey(requirement.getRequirementId())) {
            throw new ValidationException("Duplicate requirement: "
                                          + requirement.getRequirementId());
        }

        RequirementChoice[] choices = requirement.getRequirementChoice();
        for (int i = 0; i < choices.length; ++i) {
            RequirementChoiceItem item = choices[i].getRequirementChoiceItem();
            if (item.getReference() != null) {
                addReference(item.getReference());
            } else if (!_references.containsKey(item.getReferenceId())) {
                throw new ValidationException(
                    "Requirement " + requirement.getRequirementId()
                    + " has an unknown reference: " + item.getReferenceId());
            }
        }
        _requirements.put(requirement.getRequirementId(), requirement);
    }

    /**
     * Add a reference
     *
     * @param reference the reference
     * @throws IllegalArgumentException if reference is null
     * @throws ValidationException if the reference is a duplicate of
     * another
     */
    public void addReference(Reference reference) throws ValidationException {
        if (reference == null) {
            throw new IllegalArgumentException("Argument reference is null");
        }

        String referenceId = reference.getReferenceId();
        if (referenceId == null) {
            referenceId = "anonymous" + ++_anonymousReference;
        } else if (_references.containsKey(referenceId)) {
            throw new ValidationException("Duplicate reference: "
                                          + reference.getReferenceId());
        }
        _references.put(referenceId, reference);
    }

}
