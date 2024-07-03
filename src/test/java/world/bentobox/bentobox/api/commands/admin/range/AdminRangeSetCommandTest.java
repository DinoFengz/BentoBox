package world.bentobox.bentobox.api.commands.admin.range;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.LocalesManager;
import world.bentobox.bentobox.managers.PlayersManager;
import world.bentobox.bentobox.util.Util;

/**
 * @author tastybento
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AdminRangeSetCommandTest {

    private CompositeCommand ac;
    private UUID uuid;
    private User user;
    private IslandsManager im;
    private PlayersManager pm;
    @Mock
    private PluginManager pim;
    @Mock
    private @NonNull Location location;

    /**
     */
    @Before
    public void setUp() throws Exception {
        // Set up plugin
        BentoBox plugin = mock(BentoBox.class);
        // Use reflection to set the private static field "instance" in BentoBox
        Field instanceField = BentoBox.class.getDeclaredField("instance");

        instanceField.setAccessible(true);
        instanceField.set(null, plugin);
        Util.setPlugin(plugin);

        // Command manager
        CommandsManager cm = mock(CommandsManager.class);
        when(plugin.getCommandsManager()).thenReturn(cm);

        // Player
        Player p = mock(Player.class);
        // Sometimes use Mockito.withSettings().verboseLogging()
        user = mock(User.class);
        when(user.isOp()).thenReturn(false);
        uuid = UUID.randomUUID();
        UUID notUUID = UUID.randomUUID();
        while (notUUID.equals(uuid)) {
            notUUID = UUID.randomUUID();
        }
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(p);
        when(user.getName()).thenReturn("tastybento");
        User.setPlugin(plugin);

        // Parent command has no aliases
        ac = mock(CompositeCommand.class);
        when(ac.getSubCommandAliases()).thenReturn(new HashMap<>());
        when(ac.getWorld()).thenReturn(mock(World.class));

        // Island World Manager
        IslandWorldManager iwm = mock(IslandWorldManager.class);
        when(iwm.getFriendlyName(any())).thenReturn("BSkyBlock");
        when(iwm.getIslandProtectionRange(any())).thenReturn(200);
        when(plugin.getIWM()).thenReturn(iwm);

        // Player has island to begin with
        im = mock(IslandsManager.class);
        when(im.hasIsland(any(), any(UUID.class))).thenReturn(true);
        when(im.hasIsland(any(), any(User.class))).thenReturn(true);
        Island island = mock(Island.class);
        when(island.getRange()).thenReturn(50);
        when(island.getProtectionRange()).thenReturn(50);
        when(location.toVector()).thenReturn(new Vector(2, 3, 4));
        when(island.getCenter()).thenReturn(location);
        when(im.getOwnedIslands(any(), any(UUID.class))).thenReturn(Set.of(island));
        when(plugin.getIslands()).thenReturn(im);

        // Has team
        pm = mock(PlayersManager.class);
        when(im.inTeam(any(), Mockito.eq(uuid))).thenReturn(true);

        when(plugin.getPlayers()).thenReturn(pm);

        // Server & Scheduler
        BukkitScheduler sch = mock(BukkitScheduler.class);
        when(Bukkit.getScheduler()).thenReturn(sch);
        when(Bukkit.getPluginManager()).thenReturn(pim);

        // Locales
        LocalesManager lm = mock(LocalesManager.class);
        Answer<String> answer = invocation -> invocation.getArgument(1, String.class);

        when(lm.get(any(), any())).thenAnswer(answer);
        when(plugin.getLocalesManager()).thenReturn(lm);

        // Addon
        when(iwm.getAddon(any())).thenReturn(Optional.empty());
    }

    @After
    public void tearDown() {
        User.clearUsers();
        Mockito.framework().clearInlineMocks();
    }

    /**
     * Test method for
     * {@link world.bentobox.bentobox.api.commands.admin.range.AdminRangeSetCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteConsoleNoArgs() {
        AdminRangeSetCommand arc = new AdminRangeSetCommand(ac);
        CommandSender sender = mock(CommandSender.class);
        User console = User.getInstance(sender);
        assertFalse(arc.canExecute(console, "", new ArrayList<>()));
        // Show help
        verify(sender).sendMessage("commands.help.header");
    }

    /**
     * Test method for
     * {@link world.bentobox.bentobox.api.commands.admin.range.AdminRangeSetCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecutePlayerNoArgs() {
        AdminRangeSetCommand arc = new AdminRangeSetCommand(ac);
        assertFalse(arc.canExecute(user, "", List.of()));
        // Show help
        verify(user).sendMessage("commands.help.header", "[label]", "BSkyBlock");
    }

    /**
     * Test method for
     * {@link world.bentobox.bentobox.api.commands.admin.range.AdminRangeSetCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUnknownPlayer() {
        AdminRangeSetCommand arc = new AdminRangeSetCommand(ac);
        String[] args = { "tastybento", "100" };
        assertFalse(arc.canExecute(user, "", Arrays.asList(args)));
        verify(user).sendMessage("general.errors.unknown-player", "[name]", args[0]);
    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.admin.range.AdminRangeSetCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteKnownPlayerNotOwnerNoTeam() {
        when(pm.getUUID(anyString())).thenReturn(uuid);
        when(im.getOwnedIslands(any(), any(UUID.class))).thenReturn(Set.of());
        when(im.inTeam(any(), any(UUID.class))).thenReturn(false);
        AdminRangeSetCommand arc = new AdminRangeSetCommand(ac);
        List<String> args = new ArrayList<>();
        args.add("tastybento");
        args.add("100");
        assertFalse(arc.canExecute(user, "", args));
        verify(user).sendMessage("general.errors.player-has-no-island");
    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.admin.range.AdminRangeSetCommand#canExecute(User, String, List)}
     */
    @Test
    public void testExecuteKnownPlayerNotOwnerButInTeam() {
        when(pm.getUUID(anyString())).thenReturn(uuid);
        when(im.getOwnedIslands(any(), any(UUID.class))).thenReturn(Set.of());
        when(im.inTeam(any(), any(UUID.class))).thenReturn(true);
        AdminRangeSetCommand arc = new AdminRangeSetCommand(ac);
        List<String> args = new ArrayList<>();
        args.add("tastybento");
        args.add("100");
        assertFalse(arc.canExecute(user, "", args));
        verify(user).sendMessage("general.errors.player-has-no-island");
    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.admin.range.AdminRangeSetCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteTooHigh() {
        when(pm.getUUID(anyString())).thenReturn(uuid);
        AdminRangeSetCommand arc = new AdminRangeSetCommand(ac);
        List<String> args = new ArrayList<>();
        args.add("tastybento");
        args.add("1000");
        assertTrue(arc.canExecute(user, "", args));
        assertFalse(arc.execute(user, "", args));
        verify(user).sendMessage("commands.admin.range.invalid-value.too-high", TextVariables.NUMBER, "100");
    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.admin.range.AdminRangeSetCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteNotANumber() {
        when(pm.getUUID(anyString())).thenReturn(uuid);
        AdminRangeSetCommand arc = new AdminRangeSetCommand(ac);
        List<String> args = new ArrayList<>();
        args.add("tastybento");
        args.add("NAN");
        assertFalse(arc.canExecute(user, "", args));
        verify(user).sendMessage("general.errors.must-be-positive-number", TextVariables.NUMBER, "NAN");
    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.admin.range.AdminRangeSetCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test()
    public void testExecuteDoubleNumber() {
        when(pm.getUUID(anyString())).thenReturn(uuid);
        AdminRangeSetCommand arc = new AdminRangeSetCommand(ac);
        List<String> args = new ArrayList<>();
        args.add("tastybento");
        args.add("3.141592654");
        assertFalse(arc.canExecute(user, "", args));
        verify(user).sendMessage("general.errors.must-be-positive-number", TextVariables.NUMBER, "3.141592654");
    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.admin.range.AdminRangeSetCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteZero() {
        when(pm.getUUID(anyString())).thenReturn(uuid);
        AdminRangeSetCommand arc = new AdminRangeSetCommand(ac);
        List<String> args = new ArrayList<>();
        args.add("tastybento");
        args.add("0");
        assertTrue(arc.canExecute(user, "", args));
        assertFalse(arc.execute(user, "", args));
        verify(user).sendMessage("commands.admin.range.invalid-value.too-low", TextVariables.NUMBER, "0");
    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.admin.range.AdminRangeSetCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteNegNumber() {
        when(pm.getUUID(anyString())).thenReturn(uuid);
        AdminRangeSetCommand arc = new AdminRangeSetCommand(ac);
        List<String> args = new ArrayList<>();
        args.add("tastybento");
        args.add("-437645");
        assertFalse(arc.canExecute(user, "", args));
        verify(user).sendMessage("general.errors.must-be-positive-number", TextVariables.NUMBER, "-437645");
    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.admin.range.AdminRangeSetCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteSame() {
        when(pm.getUUID(anyString())).thenReturn(uuid);
        AdminRangeSetCommand arc = new AdminRangeSetCommand(ac);
        List<String> args = new ArrayList<>();
        args.add("tastybento");
        args.add("50");
        assertTrue(arc.canExecute(user, "", args));
        assertFalse(arc.execute(user, "", args));
        verify(user).sendMessage("commands.admin.range.invalid-value.same-as-before", TextVariables.NUMBER, "50");
    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.admin.range.AdminRangeSetCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecute() {
        when(pm.getUUID(anyString())).thenReturn(uuid);
        AdminRangeSetCommand arc = new AdminRangeSetCommand(ac);
        List<String> args = new ArrayList<>();
        args.add("tastybento");
        args.add("48");
        assertTrue(arc.canExecute(user, "", args));
        assertTrue(arc.execute(user, "", args));
        verify(user).sendMessage("commands.admin.range.set.success", TextVariables.NUMBER, "48");
    }

}
