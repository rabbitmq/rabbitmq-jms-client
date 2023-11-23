/* Copyright (c) 2014-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.parse;

import java.util.Map;

/**
 * An {@link Evaluator} will evaluate an expression.
 * <p>
 * An evaluator takes a set of values (the environment) and evaluates an expression in it, returning a <code>Result</code>.
 * For example, it might evaluate a (previously given) boolean expression given a set of values assigned to variables.
 */
public interface Evaluator {

    /**
     * Evaluates the (given) expression with the <code>env</code>ironment given.
     * This call should not fail, provided {@link #evaluatorOk()} is <code>true</code>.
     * @param env - the values of the variables used when evaluating the expression
     * @return the evaluated result: true or false
     */
    boolean evaluate(Map<String, Object> env);

    /**
     * This call is idempotent.
     * @return <code>true</code> if the evaluator can run cleanly; <code>false</code> otherwise
     */
    boolean evaluatorOk();

    /**
     * This call is idempotent.
     * @return null if evaluatorOk() is false; otherwise an error message denoting the type of failure to initialise.
     */
    String getErrorMessage();
}
