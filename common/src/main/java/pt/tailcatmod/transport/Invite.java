package pt.tailcatmod.transport;

public final class Invite {
    private final String minecraft, address;
    private final int port;
    public String minecraft() { return minecraft; }
    public String address() { return address; }
    public int port() { return port; }
    public Invite(String minecraft, String address, int port) {
        if (port < 1 || port > 65535 || !minecraft.matches("[0-9.]+") || !address.matches("tc[A-Za-z0-9_-]{20,4096}"))
            throw new Invalid("addServer.enterIp", "Invalid Tailcat invitation", "tailcat-mc:1:…");
        this.minecraft = minecraft; this.address = address; this.port = port;
    }
    public String encode() { return "tailcat-mc:1:" + minecraft + ":" + address + ":" + port; }
    public static final class Invalid extends IllegalArgumentException implements DisplayIssue {
        private final String key, detail;
        private final Object[] arguments;
        private Invalid(String key, String diagnostic, String detail, Object... arguments) {
            super(diagnostic); this.key = key; this.detail = detail; this.arguments = arguments.clone();
        }
        @Override public String translationKey() { return key; }
        @Override public Object[] translationArguments() { return arguments.clone(); }
        @Override public String detail() { return detail; }
    }
    public static Invite parse(String text, String expectedVersion) {
        String[] parts = text.trim().split(":", -1);
        if (parts.length != 5 || !parts[0].equals("tailcat-mc") || !parts[1].equals("1"))
            throw new Invalid("addServer.enterIp", "Paste the complete Tailcat invitation", "tailcat-mc:1:…");
        Invite invite;
        try { invite = new Invite(parts[2], parts[3], Integer.parseInt(parts[4])); }
        catch (NumberFormatException e) { throw new Invalid("addServer.enterIp", "Invalid invitation port", "tailcat-mc:1:…"); }
        if (!invite.minecraft.equals(expectedVersion))
            throw new Invalid("multiplayer.disconnect.incompatible", "Minecraft version mismatch: " + expectedVersion + " -> " + invite.minecraft, "", invite.minecraft);
        return invite;
    }
}
