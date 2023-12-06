package org.eclipse.edc.sample.extension.policy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

import org.eclipse.edc.policy.engine.spi.AtomicConstraintFunction;
import org.eclipse.edc.spi.agent.ParticipantAgent;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.engine.spi.PolicyContext;

public class LocationConstraintFunction implements AtomicConstraintFunction<Permission> {

    private Monitor monitor;
    private PolicyContext policyContext;

    public LocationConstraintFunction(Monitor monitor, PolicyContext context) {
        this.monitor = monitor;

        ParticipantAgent participantAgent = new ParticipantAgent(new HashMap<>(), new HashMap<>());
        context.putContextData(ParticipantAgent.class, participantAgent);

        this.policyContext = context;
    }

    @Override
    public boolean evaluate(Operator operator, Object rightValue, Permission rule, PolicyContext context) {
        var participantAgent = this.policyContext.getContextData(ParticipantAgent.class);

        if (participantAgent == null) {
            monitor.severe("ParticipantAgent is null. Cannot evaluate the policy.");
            return false;
        }

        var region = participantAgent.getClaims().get("region");

        monitor.info(String.format("Evaluating constraint: location %s %s", operator, rightValue.toString()));

        return switch (operator) {
            case EQ -> Objects.equals(region, rightValue);
            case NEQ -> !Objects.equals(region, rightValue);
            case IN -> ((Collection<?>) rightValue).contains(region);
            default -> false;
        };
    }
}