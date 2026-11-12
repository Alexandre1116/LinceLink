package pt.tailcatmod.transport;

public final class Invite {
    private final String minecraft, address;
    private final int port;
    public String minecraft() { return minecraft; }
    public String address() { return address; }
    public int port() { return port; }
    public Invite(String minecraft, String address, int port) {
        if (port < 1 || port > 65535 || !minecraft.matches("[0-9.]+") || !address.matches("tc[A-Za-z0-9_-]{20,4096}"))
            throw new IllegalArgumentException("Convite Tailcat inválido.");
        this.minecraft = minecraft; this.address = address; this.port = port;
    }
    public String encode() { return "tailcat-mc:1:" + minecraft + ":" + address + ":" + port; }
    public static Invite parse(String text, String expectedVersion) {
        String[] parts = text.trim().split(":", -1);
        if (parts.length != 5 || !parts[0].equals("tailcat-mc") || !parts[1].equals("1"))
            throw new IllegalArgumentException("Cola o convite completo criado pelo mod (tailcat-mc:1:…).");
        Invite invite = new Invite(parts[2], parts[3], Integer.parseInt(parts[4]));
        if (!invite.minecraft.equals(expectedVersion))
            throw new IllegalArgumentException("O anfitrião usa Minecraft " + invite.minecraft + "; estás a usar " + expectedVersion + ".");
        return invite;
    }
}
