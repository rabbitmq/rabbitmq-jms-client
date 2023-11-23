// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.

package com.rabbitmq.jms.client;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Predicate;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.MessageFormatException;

abstract class Utils {

  static final Predicate<String> SUBSCRIPTION_NAME_PREDICATE = name -> {
    if (name == null) {
      return true;
    }
    if (name.length() > 128) {
      return false;
    }
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (c != '_' && c != '.' && c != '-' && !Character.isLetter(c) && !Character.isDigit(c)) {
        return false;
      }
    }
    return true;
  };

  private Utils() {

  }

  static JMSRuntimeException wrap(JMSException e) {
    return wrap(e.getMessage(), e);
  }

  static JMSRuntimeException wrap(String message, JMSException e) {
    return new JMSRuntimeException(message, e.getErrorCode(), e);
  }

  static <T> T wrap(JmsExceptionCallable<T> operation) {
    try {
      return operation.call();
    } catch (JMSException e) {
      throw wrap(e);
    }
  }

  static void wrap(JmsExceptionRunnable operation) {
    try {
      operation.run();
    } catch (JMSException e) {
      throw wrap(e);
    }
  }

  static boolean getBooleanProperty(Map<String, Serializable> properties, String name)
      throws JMSException {
    Object o = properties.get(name);
    if (o == null) {
      //default value for null is false
      return false;
    } else if (o instanceof String) {
      return Boolean.parseBoolean((String) o);
    } else if (o instanceof Boolean) {
      return (Boolean) o;
    } else {
      throw new MessageFormatException(
          String.format("Unable to convert from class [%s]", o.getClass().getName()));
    }
  }

  static byte getByteProperty(Map<String, Serializable> properties, String name)
      throws JMSException {
    Object o = properties.get(name);
    if (o == null) {
      throw new NumberFormatException("Null is not a valid byte");
    } else if (o instanceof String) {
      return Byte.parseByte((String) o);
    } else if (o instanceof Byte) {
      return (Byte) o;
    } else {
      throw new MessageFormatException(
          String.format("Unable to convert from class [%s]", o.getClass().getName()));
    }
  }

  static short getShortProperty(Map<String, Serializable> properties, String name)
      throws JMSException {
    Object o = properties.get(name);
    if (o == null) {
      throw new NumberFormatException("Null is not a valid short");
    } else if (o instanceof String) {
      return Short.parseShort((String) o);
    } else if (o instanceof Byte) {
      return (Byte) o;
    } else if (o instanceof Short) {
      return (Short) o;
    } else {
      throw new MessageFormatException(
          String.format("Unable to convert from class [%s]", o.getClass().getName()));
    }
  }

  static int getIntProperty(Map<String, Serializable> properties, String name) throws JMSException {
    Object o = properties.get(name);
    if (o == null) {
      throw new NumberFormatException("Null is not a valid int");
    } else if (o instanceof String) {
      return Integer.parseInt((String) o);
    } else if (o instanceof Byte) {
      return (Byte) o;
    } else if (o instanceof Short) {
      return (Short) o;
    } else if (o instanceof Integer) {
      return (Integer) o;
    } else {
      throw new MessageFormatException(
          String.format("Unable to convert from class [%s]", o.getClass().getName()));
    }
  }

  static long getLongProperty(Map<String, Serializable> properties, String name)
      throws JMSException {
    Object o = properties.get(name);
    return convertToLong(o);
  }

  static long convertToLong(Object o) throws JMSException {
    if (o == null) {
      throw new NumberFormatException("Null is not a valid long");
    } else if (o instanceof String) {
      return Long.parseLong((String) o);
    } else if (o instanceof Byte) {
      return (Byte) o;
    } else if (o instanceof Short) {
      return (Short) o;
    } else if (o instanceof Integer) {
      return (Integer) o;
    } else if (o instanceof Long) {
      return (Long) o;
    } else {
      throw new MessageFormatException(
          String.format("Unable to convert from class [%s]", o.getClass().getName()));
    }
  }

  static float getFloatProperty(Map<String, Serializable> properties, String name)
      throws JMSException {
    Object o = properties.get(name);
    if (o == null) {
      throw new NumberFormatException("Null is not a valid float");
    } else if (o instanceof String) {
      return Float.parseFloat((String) o);
    } else if (o instanceof Float) {
      return (Float) o;
    } else {
      throw new MessageFormatException(
          String.format("Unable to convert from class [%s]", o.getClass().getName()));
    }
  }

  static double getDoubleProperty(Map<String, Serializable> properties, String name)
      throws JMSException {
    Object o = properties.get(name);
    if (o == null) {
      throw new NumberFormatException("Null is not a valid double");
    } else if (o instanceof String) {
      return Double.parseDouble((String) o);
    } else if (o instanceof Float) {
      return (Float) o;
    } else if (o instanceof Double) {
      return (Double) o;
    } else {
      throw new MessageFormatException(
          String.format("Unable to convert from class [%s]", o.getClass().getName()));
    }
  }

  static String getStringProperty(Map<String, Serializable> properties, String name) {
    Object o = properties.get(name);
    if (o == null) {
      return null;
    } else if (o instanceof String) {
      return (String) o;
    } else {
      return o.toString();
    }
  }

  interface JmsExceptionCallable<T> {

    T call() throws JMSException;

  }

  interface JmsExceptionRunnable {

    void run() throws JMSException;

  }

}
