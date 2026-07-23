package me.SuperRonanCraft.BetterRTP.player.commands;

import me.SuperRonanCraft.BetterRTP.player.commands.types.*;
import java.util.function.Supplier;

public enum RTPCommandType {
    BIOME(CmdBiome::new),
    EDIT(CmdEdit::new),
    HELP(CmdHelp::new),
    INFO(CmdInfo::new),
    LOCATION(CmdLocation::new),
    PLAYER(CmdPlayer::new),
    PLAYERSUDO(CmdPlayerSudo::new),
    QUEUE(CmdQueue::new, true),
    RELOAD(CmdReload::new),
    //SETTINGS(new CmdSettings(), true),
    TEST(CmdTest::new, true),
    VERSION(CmdVersion::new),
    WORLD(CmdWorld::new),
    DEV(CmdDeveloper::new, true),
    LOGGER(CmdLogger::new, true),
    ;

    private final Supplier<RTPCommand> factory;
    private final RTPCommand cmd;
    private final boolean debugOnly;

    RTPCommandType(Supplier<RTPCommand> factory) {
        this(factory, false);
    }

    RTPCommandType(Supplier<RTPCommand> factory, boolean debugOnly) {
        this.factory = factory;
        this.cmd = factory.get();
        this.debugOnly = debugOnly;
    }

    public boolean isDebugOnly() {
        return debugOnly;
    }

    public RTPCommand getCmd() {
        return cmd;
    }

    /** Creates lifecycle-local command state for a plugin load or reload. */
    public RTPCommand createCommand() {
        return factory.get();
    }
}
