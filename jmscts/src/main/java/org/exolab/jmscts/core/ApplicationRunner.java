/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy of
 * this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. 3. The
 * name "Exolab" must not be used to endorse or promote products derived from
 * this Software without prior written permission of Exoffice Technologies. For
 * written permission, please contact tma@netspace.net.au. 4. Products derived
 * from this Software may not be called "Exolab" nor may "Exolab" appear in
 * their names without prior written permission of Exoffice Technologies. Exolab
 * is a registered trademark of Exoffice Technologies. 5. Due credit should be
 * given to the Exolab Project (http://www.exolab.org/). THIS SOFTWARE IS
 * PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. Copyright 2001-2004 (C) Exoffice Technologies
 * Inc. All Rights Reserved. $Id: ApplicationRunner.java,v 1.3 2004/01/31
 * 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.lang.reflect.Method;
import java.rmi.Naming;

import org.exolab.jmscts.core.service.ExecutionMonitorListener;

/**
 * ApplicationRunner executes an application, notifying
 * {@link org.exolab.jmscts.core.service.ExecutionMonitorService} when the
 * application starts and stops.<br>
 * Instances of this class are executed in a separate JVM by
 * {@link AsyncExecutor}
 * 
 * @version $Revision: 1.3 $ $Date: 2004/01/31 13:44:24 $
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see AsyncExecutor
 */
final class ApplicationRunner {

    /**
     * Prevent construction of utility class
     */
    private ApplicationRunner() {
    }

    /**
     * Main line to execute the tool
     * 
     * @param args command line arguments
     * @throws Exception for any error
     */
    public static void main(String[] args) throws Exception {
        final int count = 3; // expect 3 args
        if (args.length < count) {
            System.err.println("usage: " + ApplicationRunner.class.getName() + " <app id> <port> <class> [arguments]");
            System.exit(1);
        }

        String id = args[0];
        String port = args[1];
        String app = args[2];

        // look up the execution monitor listener
        String name = "//localhost:" + port + "/ExecutionListener";
        ExecutionMonitorListener listener = (ExecutionMonitorListener) Naming.lookup(name);

        // load the application
        Class<?> application = Class.forName(app);
        @SuppressWarnings("rawtypes")
        Class[] params = new Class[] { String[].class };
        Method method = application.getMethod("main", params);

        // notify the listener that this has started
        listener.started(id);

        // strip off the arguments required by this
        String[] filteredArgs = new String[args.length - count];
        System.arraycopy(args, count, filteredArgs, 0, args.length - count);

        try {
            // start the application
            method.invoke(null, new Object[] { filteredArgs });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            listener.error(id, throwable);
        }
    }

}
