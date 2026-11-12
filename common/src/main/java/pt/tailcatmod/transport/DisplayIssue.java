package pt.tailcatmod.transport;

/** Rendering metadata; the transport never depends on Minecraft client classes. */
public interface DisplayIssue {
    String translationKey();
    Object[] translationArguments();
    String detail();
}
