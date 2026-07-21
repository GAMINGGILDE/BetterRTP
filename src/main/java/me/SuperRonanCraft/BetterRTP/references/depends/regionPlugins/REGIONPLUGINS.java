package me.SuperRonanCraft.BetterRTP.references.depends.regionPlugins;

import lombok.Getter;
import me.SuperRonanCraft.BetterRTP.references.settings.SoftDepends;

@Getter
public enum REGIONPLUGINS {
    GRIEFPREVENTION("GriefPrevention", new RTP_GriefPrevention()),
    LANDS("Lands", new RTP_Lands()),
    TOWNY("Towny", new RTP_Towny()),
    WORLDGUARD("WorldGuard", new RTP_WorldGuard());

    private final SoftDepends.RegionPlugin plugin = new SoftDepends.RegionPlugin();
    private final String setting_name, pluginyml_name;
    private final RegionPluginCheck validator;

    REGIONPLUGINS(String all_name, RegionPluginCheck validator) {
        this(all_name, all_name, validator);
    }

    REGIONPLUGINS(String setting_name, String pluginyml_name, RegionPluginCheck validator) {
        this.setting_name = setting_name;
        this.pluginyml_name = pluginyml_name;
        this.validator = validator;
    }

    public boolean isEnabled() {
        return plugin.isEnabled();
    }
}
