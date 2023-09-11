/*
 *  Copyright (c) 2023 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */

package org.eclipse.edc.sample.extension.policy;


import org.eclipse.edc.policy.engine.spi.AtomicConstraintFunction;
import org.eclipse.edc.policy.engine.spi.PolicyContext;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.spi.monitor.Monitor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeIntervalFunction implements AtomicConstraintFunction<Permission> {

    private Monitor monitor;

    public TimeIntervalFunction(Monitor monitor) {
        this.monitor = monitor;
    }



    @Override
    public boolean evaluate(Operator operator, Object rightValue, Permission rule, PolicyContext context) {
        var sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        Date date;
        try {
            date = sdf.parse((String) rightValue);
        } catch (ParseException e) {
            monitor.severe("Failed to parse right value of constraint to date.");
            return false;
        }

        switch (operator) {
            case LT: var isBefore = new Date().before(date);
                monitor.info("Current date is " + (isBefore ? "before" : "after") + " desired end date");
                return isBefore;
            case GT: var isAfter = new Date().after(date);
                monitor.info("Current date is " + (isAfter ? "after" : "before") + " desired start date");
                return isAfter;
            default: return false;
        }
    }
}


