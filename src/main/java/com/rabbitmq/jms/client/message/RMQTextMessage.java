// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.UnsupportedEncodingException;

import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;
import javax.jms.TextMessage;

import com.rabbitmq.jms.client.RMQMessage;


/**
 * Implements {@link TextMessage} interface.
 */
public class RMQTextMessage extends RMQMessage implements TextMessage {

    private volatile String text;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setText(String string) throws JMSException {
        if (isReadonlyBody()) throw new MessageNotWriteableException("Message has been received and is read only.");
        this.text = string;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() throws JMSException {
        return this.text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void clearBodyInternal() {
        this.text = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeBody(ObjectOutput out, ByteArrayOutputStream bout) throws IOException {
        String text = this.text;
        out.writeBoolean(text == null);
        if (text!=null) {
            byte[] ba = text.getBytes("UTF-8");
            out.writeInt(ba.length);
            out.write(ba);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readBody(ObjectInput inputStream, ByteArrayInputStream bin) throws IOException, ClassNotFoundException {
        boolean isnull = inputStream.readBoolean();
        if (!isnull) {
            int len = inputStream.readInt();
            byte[] ba = new byte[len];
            inputStream.readFully(ba, 0, len);
            this.text = new String(ba, "UTF-8");
        }
    }

    @Override
    protected void readAmqpBody(byte[] barr) {
        try {
            this.text = new String(barr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Will not happen: UTF-8 is supported everywhere
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T doGetBody(Class<T> c) throws JMSException {
        return (T) this.getText();
    }

    @Override
    protected void writeAmqpBody(ByteArrayOutputStream out) throws IOException {
        out.write((this.text!=null ? this.text : "").getBytes("UTF-8"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isBodyAssignableTo(Class c) {
        return c.isAssignableFrom(String.class);
    }



    public static RMQMessage recreate(TextMessage msg) throws JMSException {
        RMQTextMessage rmqTMsg = new RMQTextMessage();
        RMQMessage.copyAttributes(rmqTMsg, msg);

        rmqTMsg.setText(msg.getText());

        return rmqTMsg;
    }

    @Override
    public boolean isAmqpWritable() {
        return true;
    }
}
