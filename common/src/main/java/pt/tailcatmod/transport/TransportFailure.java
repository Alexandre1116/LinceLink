package pt.tailcatmod.transport;

import java.io.IOException;

public final class TransportFailure extends IOException implements DisplayIssue {
    private final String key, detail;
    private final Object[] arguments;
    public TransportFailure(String key, String diagnostic, String detail, Object... arguments) {
        super(diagnostic);
        this.key = key; this.detail = detail; this.arguments = arguments.clone();
    }
    @Override public String translationKey() { return key; }
    @Override public Object[] translationArguments() { return arguments.clone(); }
    @Override public String detail() { return detail; }
}
