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
 * Copyright 2003-2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: FileHelper.java,v 1.2 2004/02/02 03:50:24 tanderson Exp $
 */
package org.exolab.jmscts.report;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;


/**
 * Helper class for manipulating files
 *
 * @version     $Revision: 1.2 $ $Date: 2004/02/02 03:50:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public final class FileHelper {

    /**
     * The logger
     */
    private static final Logger log = Logger.getLogger(FileHelper.class);


    /**
     * Prevent construction of utility class
     */
    private FileHelper() {
    }

    /**
     * Copy a file
     *
     * @param input source path
     * @param output target path
     * @throws IOException if the copy fails
     */
    public static void copy(String input, String output) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Copying from " + input + " to " + output);
        }

        BufferedReader source = null;
        BufferedWriter target = null;
        try {
            source = new BufferedReader(new FileReader(input));
            target = new BufferedWriter(new FileWriter(output));
            String line = null;
            while ((line = source.readLine()) != null) {
                target.write(line);
            }
        } finally {
            if (source != null) {
                source.close();
            }
            if (target != null) {
                target.close();
            }
        }
    }

}
