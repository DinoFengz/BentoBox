package world.bentobox.bentobox.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.hooks.Hook;

/**
 * @author Poslovitch
 */
public class HooksManager {

    private BentoBox plugin;
    private List<Hook> hooks;

    public HooksManager(BentoBox plugin) {
        this.plugin = plugin;
        this.hooks = new ArrayList<>();
    }

    public void registerHook(Hook hook) {
        if (hook.isPluginAvailable()) {
            plugin.log("Hooking with " + hook.getPluginName() + "...");
            if (hook.hook()) {
                hooks.add(hook);
            } else {
                plugin.log("Could not hook with " + hook.getPluginName() + ((hook.getFailureCause() != null) ? " because: " + hook.getFailureCause() : "") + ". Skipping...");
            }
        }
        // Do not tell the user if we couldn't hook with a plugin which is not available.
        // We may have in the near future almost ~25 hooks, which would basically spam the console and make users nervous.
    }

    public List<Hook> getHooks() {
        return hooks;
    }

    public Optional<Hook> getHook(String pluginName) {
        return hooks.stream().filter(hook -> hook.getPluginName().equals(pluginName)).findFirst();
    }
}
