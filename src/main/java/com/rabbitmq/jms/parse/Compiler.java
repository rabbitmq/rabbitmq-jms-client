/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.parse;

/**
 * A {@link Compiler} produces compiled code. Compiled code is produced as a {@link String}.
 * <p>
 * Compiling never throws an exception, but instead returns prematurely.
 * The {@link #compileOk()} method indicates success or failure, and {@link #getErrorMessage()}
 * describes the error if {@link #compileOk()} returns <code>false</code>.
 * </p>
 */
public interface Compiler {

    /**
     * Return the compiled code. This call is idempotent.
     * @return the compiled code
     */
    String compile();

    /**
     * Returns <code>true</code> if the compiled code is complete and compilation successful, <code>false</code> otherwise.
     * This call is idempotent.
     * @return <code>true</code> if the compiled code is complete and successful, false otherwise.
     */
    boolean compileOk();

    /**
     * This call is idempotent.
     * @return a message indicating the error during the compilation if there was one.
     */
    String getErrorMessage();
}
