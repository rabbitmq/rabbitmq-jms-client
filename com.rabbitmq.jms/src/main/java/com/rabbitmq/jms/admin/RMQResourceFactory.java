package com.rabbitmq.jms.admin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

public class RMQResourceFactory implements ObjectFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        if ((obj == null) || !(obj instanceof Reference)) {
            return null;
        }
        Reference ref = (Reference) obj;
        Enumeration<RefAddr> refs = ref.getAll();

        String type = ref.getClassName();
        Object o = Class.forName(type).newInstance();

        while (refs.hasMoreElements()) {
            RefAddr addr = refs.nextElement();
            String param = addr.getType();
            String value = null;
            if (addr.getContent() != null) {
                value = addr.getContent().toString();
            }
            if (setProperty(o, param, value)) {

            } else {
                // TODO logging implementation
                System.out.println("Property not configured[" + param + "]. No setter found on[" + o + "].");
            }
        }
        return o;
    }

    /**
     * Finds the setXXX method to set a generic property on an object
     * It will attempt to locate a setName property matching the name parameter
     * @param o - object to invoke setXXX on
     * @param name - the name of the property. 
     * @param value - the value to set
     * @return true if the setXXX method was found and successfully invoked
     */
    public static boolean setProperty(Object o, String name, String value) {
        String setter = "set" + capitalize(name);

        try {
            Method methods[] = o.getClass().getMethods();
            // First, the ideal case - a setFoo( String ) method
            for (int i = 0; i < methods.length; i++) {
                Class<?> paramT[] = methods[i].getParameterTypes();
                if (setter.equals(methods[i].getName()) && paramT.length == 1 && "java.lang.String".equals(paramT[0].getName())) {

                    methods[i].invoke(o, new Object[] { value });
                    return true;
                }
            }

            // Try a setFoo ( int ) or ( boolean )
            for (int i = 0; i < methods.length; i++) {
                boolean ok = true;
                if (setter.equals(methods[i].getName()) && methods[i].getParameterTypes().length == 1) {

                    // match - find the type and invoke it
                    Class<?> paramType = methods[i].getParameterTypes()[0];
                    Object params[] = new Object[1];

                    // Try a setFoo ( int )
                    if ("java.lang.Integer".equals(paramType.getName()) || "int".equals(paramType.getName())) {
                        try {
                            params[0] = new Integer(value);
                        } catch (NumberFormatException ex) {
                            ok = false;
                        }
                        // Try a setFoo ( long )
                    } else if ("java.lang.Long".equals(paramType.getName()) || "long".equals(paramType.getName())) {
                        try {
                            params[0] = new Long(value);
                        } catch (NumberFormatException ex) {
                            ok = false;
                        }

                        // Try a setFoo ( boolean )
                    } else if ("java.lang.Boolean".equals(paramType.getName()) || "boolean".equals(paramType.getName())) {
                        params[0] = Boolean.valueOf(value);

                        // Try a setFoo ( InetAddress )
                    } else if ("java.net.InetAddress".equals(paramType.getName())) {
                        try {
                            params[0] = InetAddress.getByName(value);
                        } catch (UnknownHostException exc) {
                            // TODO logging implementation
                            System.out.println("Unable to resolve host name:" + value);
                            ok = false;
                        }

                        // Unknown type
                    } else {
                        // TODO logging implementation
                        System.out.println("Unknown type " + paramType.getName());
                    }

                    if (ok) {
                        methods[i].invoke(o, params);
                        return true;
                    }
                }
            }
            // TODO logging implementation
            System.out.println("Unable to find setter for ["+name+" : "+value+"]");

        } catch (IllegalArgumentException ex2) {
            // TODO logging implementation
            System.out.println("IAE " + o + " " + name + " " + value);
        } catch (SecurityException ex1) {
            // TODO logging implementation
            System.out.println("SecurityException for " + o.getClass() + " " + name + "=" + value + ")");
        } catch (IllegalAccessException iae) {
            // TODO logging implementation
            System.out.println("IllegalAccessException for " + o.getClass() + " " + name + "=" + value + ")");
        } catch (InvocationTargetException ie) {
            Throwable cause = ie.getCause();
            if (cause instanceof ThreadDeath) {
                throw (ThreadDeath) cause;
            }
            if (cause instanceof VirtualMachineError) {
                throw (VirtualMachineError) cause;
            }
            // TODO logging implementation
            System.out.println("InvocationTargetException for " + o.getClass() + " " + name + "=" + value + ")");
        }
        return false;
    }

    public static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

}
