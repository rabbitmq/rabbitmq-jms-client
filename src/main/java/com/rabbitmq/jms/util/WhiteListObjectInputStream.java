/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * An {@link ObjectInputStream} implementation that checks loaded classes
 * against a list of trusted packages or package prefixes.
 * </p>
 * <p>
 * Heavily inspired by and derived from
 * org.apache.activemq.util.ClassLoadingAwareObjectInputStream in ActiveMQ
 * as well as https://github.com/spring-projects/spring-amqp/commit/4150f107e60cac4a7735fcf7cb4c1889a0cbab6c.
 * </p>
 *
 * @see ObjectInputStream
 */
public class WhiteListObjectInputStream extends ObjectInputStream {
    private static final ClassLoader FALLBACK_CLASS_LOADER =
            WhiteListObjectInputStream.class.getClassLoader();

    public static final List<String> DEFAULT_TRUSTED_PACKAGES;

    static {
        // backwards compatible default
        String viaProperty = System.getProperty("com.rabbitmq.jms.TrustedPackagesPrefixes", "*");
        DEFAULT_TRUSTED_PACKAGES = Arrays.asList(viaProperty.split(","));
    }

    private final ClassLoader inputStreamLoader;
    private List<String> trustedPackages = WhiteListObjectInputStream.DEFAULT_TRUSTED_PACKAGES;

    /**
     * <p>
     * Creates an ObjectInputStream that reads from the specified InputStream.
     * A serialization stream header is read from the stream and verified.
     * This constructor will block until the corresponding ObjectOutputStream
     * has written and flushed the header.
     * </p>
     * <p>If a security manager is installed, this constructor will check for
     * the "enableSubclassImplementation" SerializablePermission when invoked
     * directly or indirectly by the constructor of a subclass which overrides
     * the ObjectInputStream.readFields or ObjectInputStream.readUnshared
     * methods.
     * </p>
     *
     * @param in input stream to read from
     * @throws IOException          if an I/O error occurs while reading stream header
     * @throws SecurityException    if untrusted subclass illegally overrides
     *                              security-sensitive methods
     * @throws NullPointerException if <code>in</code> is <code>null</code>
     * @see ObjectInputStream#ObjectInputStream()
     * @see ObjectInputStream#readFields()
     */
    public WhiteListObjectInputStream(InputStream in) throws IOException {
        super(in);
        this.inputStreamLoader = in.getClass().getClassLoader();
    }

    /**
     * <p>Creates an ObjectInputStream that reads from the specified InputStream.
     * A serialization stream header is read from the stream and verified.
     * This constructor will block until the corresponding ObjectOutputStream
     * has written and flushed the header.
     * </p>
     * <p>If a security manager is installed, this constructor will check for
     * the "enableSubclassImplementation" SerializablePermission when invoked
     * directly or indirectly by the constructor of a subclass which overrides
     * the ObjectInputStream.readFields or ObjectInputStream.readUnshared
     * methods.
     * </p>
     * @param in              input stream to read from
     * @param trustedPackages List of packages that are trusted. Classes in them
     *                        will be serialized.
     * @throws IOException          if an I/O error occurs while reading stream header
     * @throws SecurityException    if untrusted subclass illegally overrides
     *                              security-sensitive methods
     * @throws NullPointerException if <code>in</code> is <code>null</code>
     * @see ObjectInputStream#ObjectInputStream()
     * @see ObjectInputStream#readFields()
     */
    public WhiteListObjectInputStream(InputStream in, List<String> trustedPackages) throws IOException {
        super(in);
        this.inputStreamLoader = in.getClass().getClassLoader();
        this.trustedPackages = trustedPackages;
    }

    /**
     * Load the local class equivalent of the specified stream class
     * description.  Subclasses may implement this method to allow classes to
     * be fetched from an alternate source.
     * <p>The corresponding method in <code>ObjectOutputStream</code> is
     * <code>annotateClass</code>.  This method will be invoked only once for
     * each unique class in the stream.  This method can be implemented by
     * subclasses to use an alternate loading mechanism but must return a
     * <code>Class</code> object. Once returned, if the class is not an array
     * class, its serialVersionUID is compared to the serialVersionUID of the
     * serialized class, and if there is a mismatch, the deserialization fails
     * and an exception is thrown.
     * </p>
     * <p>The default implementation of this method in
     * <code>ObjectInputStream</code> returns the result of calling
     * <pre>
     *     Class.forName(desc.getName(), false, loader)
     * </pre>
     * where <code>loader</code> is determined as follows: if there is a
     * method on the current thread's stack whose declaring class was
     * defined by a user-defined class loader (and was not a generated to
     * implement reflective invocations), then <code>loader</code> is class
     * loader corresponding to the closest such method to the currently
     * executing frame; otherwise, <code>loader</code> is
     * <code>null</code>. If this call results in a
     * <code>ClassNotFoundException</code> and the name of the passed
     * <code>ObjectStreamClass</code> instance is the Java language keyword
     * for a primitive type or void, then the <code>Class</code> object
     * representing that primitive type or void will be returned
     * (e.g., an <code>ObjectStreamClass</code> with the name
     * <code>"int"</code> will be resolved to <code>Integer.TYPE</code>).
     * Otherwise, the <code>ClassNotFoundException</code> will be thrown to
     * the caller of this method.
     *
     * @param desc an instance of class <code>ObjectStreamClass</code>
     * @return a <code>Class</code> object corresponding to <code>desc</code>
     * @throws IOException            any of the usual Input/Output exceptions.
     * @throws ClassNotFoundException if class of a serialized object cannot
     *                                be found or isn't trusted.
     */
    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        ClassLoader threadLoader = Thread.currentThread().getContextClassLoader();
        Class clazz = load(desc.getName(), threadLoader, inputStreamLoader);
        checkWhiteList(clazz);
        return clazz;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class[] ifaces = new Class[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            ifaces[i] = load(interfaces[i], cl);
        }

        Class clazz = null;
        try {
            clazz = Proxy.getProxyClass(cl, ifaces);
        } catch (IllegalArgumentException e) {
            try {
                clazz = Proxy.getProxyClass(inputStreamLoader, ifaces);
            } catch (IllegalArgumentException _ignored1) {
                // ignore
            }
            try {
                clazz = Proxy.getProxyClass(FALLBACK_CLASS_LOADER, ifaces);
            } catch (IllegalArgumentException _ignored2) {
                // ignore
            }
        }

        if (clazz != null) {
            checkWhiteList(clazz);
            return clazz;
        } else {
            throw new ClassNotFoundException(null);
        }
    }

    @SuppressWarnings("unused")
    public void addTrustedPackage(String trustedPackage) {
        this.trustedPackages.add(trustedPackage);
    }

    /**
     * @return list of packages trusted for deserialization
     * from ObjectMessage payloads
     */
    @SuppressWarnings("unused")
    public List<String> getTrustedPackages() {
        return trustedPackages;
    }

    /**
     * @param trustedPackages list of packages trusted for deserialization
     *                        from ObjectMessage payloads
     */
    @SuppressWarnings("unused")
    public void setTrustedPackages(List<String> trustedPackages) {
        this.trustedPackages = trustedPackages;
    }

    /**
     * @return true if this object stream considers all packages to
     * be trusted, false otherwise
     */
    public boolean shouldTrustAllPackages() {
        return (this.trustedPackages != null) && (trustedPackages.size() == 1 && trustedPackages.get(0).equals("*"));
    }

    private void checkWhiteList(Class clazz) throws ClassNotFoundException {
        if (clazz.isPrimitive()) {
            return;
        }

        if (clazz.getPackage() != null && !shouldTrustAllPackages()) {
            boolean result = false;
            String p = clazz.getPackage().getName();
            for (String pkg : this.trustedPackages) {
                // Note: this means that an empty string works the same way as "*"
                //       but making it mean "trust no package" makes even less sense
                if (p.equals(pkg) || p.startsWith(pkg)) {
                    result = true;
                    break;
                }
            }

            if (!result) {
                throw new ClassNotFoundException("Class " + clazz + " is not trusted to be deserialized as ObjectMessage payload. "
                                                 + "Trusted packages can be configured via -Dcom.rabbitmq.jms.TrustedPackagesPrefixes "
                                                 + " or RMQConnectionFactory#setTrustedPackages.");
            }
        }
    }

    private Class<?> load(String className, ClassLoader... cls) throws ClassNotFoundException {
        for (ClassLoader cl : cls) {
            try {
                return Class.forName(className, false, cl);
            } catch (ClassNotFoundException _ignored) {
                // continue
            }
        }

        return Class.forName(className, false, FALLBACK_CLASS_LOADER);
    }
}
