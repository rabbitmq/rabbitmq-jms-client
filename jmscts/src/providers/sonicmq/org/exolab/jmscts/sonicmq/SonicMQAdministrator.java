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
 *    please contact jima@intalio.com.
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
 * Copyright 2001, 2003 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: SonicMQAdministrator.java,v 1.1 2003/05/04 14:12:40 tanderson Exp $
 */
package org.exolab.jmscts.sonicmq;

import java.util.HashMap;
import java.util.HashSet;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import progress.message.tools.IBrokerManagerListener;
import progress.message.tools.BrokerManager;

import org.exolab.jmscts.provider.Administrator;


/**
 * This class provides methods for obtaining and manipulating administered 
 * objects managed by the Sonicmq implementation of JMS
 *
 * @version     $Revision: 1.1 $ $Date: 2003/05/04 14:12:40 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         SonicMQProvider
 */
class SonicMQAdministrator implements Administrator {

    /**
     * The broker address
     */
    private String _broker = "localhost:2506";

    /**
     * The cached administration connection
     */
    private BrokerManager _manager = null;

    /**
     * The cache of known administered objects
     */
    private HashMap _directory = new HashMap();

    /**
     * The set of known queues
     */
    private HashSet _queues = new HashSet();

    /**
     * Queue connection for creating queues
     */
    private QueueConnection _queueConnection = null;
    
    /**
     * Queue session for creating queues
     */
    private QueueSession _queueSession = null;

    /**
     * Topic connection for creating topics
     */
    private TopicConnection _topicConnection = null;
    
    /**
     * Topic session for creating topics
     */
    private TopicSession _topicSession = null;

    /**
     * Returns the name of the QueueConnectionFactory bound in JNDI
     *
     * @return the default QueueConnectionFactory name
     */
    public String getQueueConnectionFactory() {
        return "QueueConnectionFactory";
    }

    /**
     * Returns the name of the TopicConnectionFactory bound in JNDI
     *
     * @return the default TopicConnectionFactory name
     */
    public String getTopicConnectionFactory() {
        return "TopicConnectionFactory";
    }

    /**
     * Returns the name of the XAQueueConnectionFactory bound in JNDI
     *
     * @return the default XAQueueConnectionFactory name
     */
    public String getXAQueueConnectionFactory() {
        return "XAQueueConnectionFactory";
    }

    /**
     * Returns the name of the XATopicConnectionFactory bound in JNDI
     *
     * @return the default XATopicConnectionFactory name
     */
    public String getXATopicConnectionFactory() {
        return "XATopicConnectionFactory";
    }
    
    /**
     * Look up the named administered object
     *
     * @param name the name that the administered object is bound to
     * @return the administered object bound to name
     * @throws NamingException if the object is not bound, or the lookup fails
     */
    public Object lookup(String name) throws NamingException {
        Object result = _directory.get(name);
        if (result == null) {
            try {
                result = createAdministeredObject(name);
            } catch (JMSException exception) {
                NamingException error = new NamingException(
                    exception.getMessage());
                error.setRootCause(exception);
                throw error;
            }
            if (result == null) {
                throw new NameNotFoundException("Name not found: " + name);
            }
        }
        return result;
    }

    /**
     * Create an administered destination
     *
     * @param name the destination name
     * @param queue if true, create a queue, else create a topic
     * @throws JMSException if the destination cannot be created
     */
    public void createDestination(String name, boolean queue)
        throws JMSException {
        final boolean global = false;      
        // if true, the queue may accept messages routed from another 
        // broker (which may hang for the evaluation version of SonicMQ)
  
        boolean readExclusive = false; 
        // set to false to allow multiple concurrent receivers to receive 
        // messages from the queue.

        final int retrieveThreshold = 1200; // typical values
        final int saveThreshold = 1400;
        final int maxQueueSize = 1000;

        try {
            BrokerManager manager = getBrokerManager();
            if (queue) {
                manager.setQueue(
                    name, global, readExclusive, retrieveThreshold, 
                    saveThreshold, maxQueueSize);
                createQueue(name);
            } else {
                createTopic(name);
            }
        } catch (Exception exception) {
            JMSException error = new JMSException(exception.getMessage());
            error.setLinkedException(exception);
            throw error;
        }
    }

    /**
     * Destroy an administered destination
     *
     * @param name the destination name
     * @throws JMSException if the destination cannot be destroyed
     */
    public void destroyDestination(String name)
        throws JMSException {

        try {
            Destination destination = (Destination) lookup(name);
            if (destination instanceof Queue) {
                BrokerManager manager = getBrokerManager();
                manager.deleteQueue(name);
                _queues.remove(name);
            }
            _directory.remove(name);
        } catch (NamingException exception) {
            JMSException error = new JMSException(exception.getMessage());
            error.setLinkedException(exception);
            throw error;
        } catch (Exception exception) {
            JMSException error = new JMSException(exception.getMessage());
            error.setLinkedException(exception);
            throw error;
        }
    }

    /**
     * Returns true if an administered destination exists
     *
     * @param name the destination name
     * @throws JMSException for any internal JMS provider error
     */
    public boolean destinationExists(String name)
        throws JMSException {

        boolean exists = false;
        try {
            lookup(name);
            exists = true;
        } catch (NameNotFoundException ignore) {
        } catch (Exception exception) {
            JMSException error = new JMSException(exception.getMessage());
            error.setLinkedException(exception);
            throw error;
        }
        return exists;
    }

    public void initialise() throws JMSException {
        BrokerManager manager = getBrokerManager();
        try {
            String[][] queues = manager.getQueues(null, false);
            for (int i = 0; i < queues.length; ++i) {
                _queues.add(queues[i][0]);
            }
        } catch (Exception exception) {
            JMSException error = new JMSException(exception.getMessage());
            error.setLinkedException(exception);
            throw error;
        }
    }

    public synchronized void cleanup() {
        if (_manager != null) {
            _manager.disconnect();
        }

        if (_queueConnection != null) {
            try {
                _queueConnection.close();
            } catch (Exception ignore) {
            }
        }

        if (_topicConnection != null) {
            try {
                _topicConnection.close();
            } catch (Exception ignore) {
            }
        }
        
        _directory.clear();
        _queues.clear();
    }

    private Object createAdministeredObject(String name) 
        throws JMSException, NamingException {
        Object result = null;
        if (_queues.contains(name)) {
            result = createQueue(name);
        } else if (name.equals(getQueueConnectionFactory())) {
            result = new progress.message.jclient.QueueConnectionFactory(
                _broker);
            _directory.put(name, result);
        } else if (name.equals(getTopicConnectionFactory())) {
            result = new progress.message.jclient.TopicConnectionFactory(
                _broker);
            _directory.put(name, result);
        } else if (name.equals(getXAQueueConnectionFactory())) {
            result = new progress.message.jclient.xa.XAQueueConnectionFactory(
                _broker);
            _directory.put(name, result);
        } else if (name.equals(getXATopicConnectionFactory())) {
            result = new progress.message.jclient.xa.XATopicConnectionFactory(
                _broker);
            _directory.put(name, result);
        }
        return result;
    }

    private synchronized Object createQueue(String name) 
        throws JMSException, NamingException {
        Object result = null;
        if (_queueSession == null) {
            QueueConnectionFactory factory = (QueueConnectionFactory) lookup(
                getQueueConnectionFactory());
            _queueConnection = factory.createQueueConnection("Administrator",
                                                             "Administrator");
            _queueSession = _queueConnection.createQueueSession(
                false, QueueSession.AUTO_ACKNOWLEDGE);
        }
        result = _queueSession.createQueue(name);
        _directory.put(name, result);
        _queues.add(name);
        return result;
    }

    private synchronized Object createTopic(String name) 
        throws JMSException, NamingException {
        Object result = null;
        if (_topicSession == null) {
            TopicConnectionFactory factory = (TopicConnectionFactory) lookup(
                getTopicConnectionFactory());
            _topicConnection = factory.createTopicConnection("Administrator",
                                                             "Administrator");
            _topicSession = _topicConnection.createTopicSession(
                false, TopicSession.AUTO_ACKNOWLEDGE);
        }
        result = _topicSession.createTopic(name);
        _directory.put(name, result);
        return result;
    }

    private synchronized BrokerManager getBrokerManager() throws JMSException {

        if (_manager == null) {
            // dummy up a listener. None of its functionality is required.
            IBrokerManagerListener listener = new IBrokerManagerListener() {
                public void brokerConnectionDropped(String host) {
                }
            
                public void brokerEventNotification(String description) {
                }

                public void brokerShutdown(String host) {
                }

                public void brokerUndeliveredMsgNotification(
                    String broker, String messageID, String destination, 
                    long timestamp, int reason, boolean preserved) {
                }
            };
                
            _manager = new BrokerManager(listener, "localhost:2506",
                                         "Administrator", "Administrator");
            try {
                _manager.connect();
                if (!_manager.isAdminConnection()) {
                    _manager.disconnect();
                    throw new JMSException(
                        "User doesn't have permission to administer SonicMQ");
                }
            } catch (JMSException exception) {
                throw exception;
            } catch (Exception exception) {
                JMSException error = new JMSException(exception.getMessage());
                error.setLinkedException(exception);
                throw error;
            }
        }
        return _manager;
    }

} //-- SonicMQAdministrator
