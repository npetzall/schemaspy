package org.schemaspy.testing;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.schemaspy.app.Context;
import org.schemaspy.app.SingletonContext;

public class ContextRule implements TestRule {

    private final String[] args;
    private Context context;

    public ContextRule(String...args) {
        this.args = args;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return statement(base, description);
    }

    private Statement statement(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before(description);
                try {
                    base.evaluate();
                } finally {
                    after(description);
                }
            }
        };
    }

    private void before(Description description) {
        SingletonContext.getInstance().addContext(getContextName(description), new Context(args));
    }

    private void after(Description description) {
        SingletonContext.getInstance().removeContext(getContextName(description));
    }

    private String getContextName(Description description) {
        return description.getDisplayName();
    }

    public Context getContext() {
        return context;
    }
}
