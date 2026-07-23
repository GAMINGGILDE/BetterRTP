package me.SuperRonanCraft.BetterRTP.player.commands.types;

/** Pure command-line parsing for /rtp info. */
record InfoCommandRequest(
        Subcommand subcommand, String worldName, String playerName) {

    static InfoCommandRequest parse(String[] args) {
        if (args == null || args.length <= 1) {
            return new InfoCommandRequest(Subcommand.OVERVIEW, null, null);
        }
        Subcommand subcommand;
        try {
            subcommand = Subcommand.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException exception) {
            return new InfoCommandRequest(Subcommand.UNKNOWN, null, null);
        }
        return switch (subcommand) {
            case WORLD -> new InfoCommandRequest(
                    subcommand,
                    args.length > 2 ? args[2] : null,
                    args.length > 3 ? args[3] : null);
            case PLAYER -> new InfoCommandRequest(
                    subcommand, null, args.length > 2 ? args[2] : null);
            default -> new InfoCommandRequest(subcommand, null, null);
        };
    }

    enum Subcommand {
        OVERVIEW,
        PARTICLES,
        SHAPES,
        POTION_EFFECTS,
        WORLD,
        PLAYER,
        UNKNOWN
    }
}
