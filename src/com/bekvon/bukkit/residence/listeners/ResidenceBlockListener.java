package com.bekvon.bukkit.residence.listeners;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.commands.auto.direction;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.ActionBar.CMIActionBar;
import net.Zrips.CMILib.Container.CMIBlock;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Version.Version;

public class ResidenceBlockListener implements Listener {

    private List<String> MessageInformed = new ArrayList<String>();

    private Set<UUID> ResCreated = new HashSet<UUID>();
    public static Set<UUID> newPlayers = new HashSet<UUID>();

    private Residence plugin;

    public ResidenceBlockListener(Residence residence) {
	this.plugin = residence;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAnvilInventoryClick(InventoryClickEvent e) {
	// Disabling listener if flag disabled globally
	if (!Flags.anvilbreak.isGlobalyEnabled())
	    return;
	Inventory inv = e.getInventory();
	try {
	    if (inv == null || inv.getType() != InventoryType.ANVIL || e.getInventory().getLocation() == null)
		return;
	} catch (Exception | NoSuchMethodError ex) {
	    return;
	}
	Block b = e.getInventory().getLocation().getBlock();
	if (b == null || !CMIMaterial.isAnvil(b.getType()))
	    return;

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(e.getInventory().getLocation());
	if (res == null)
	    return;
	// Fix anvil only when item is picked up
	if (e.getRawSlot() != 2)
	    return;
	if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
	    return;
	if (!res.getPermissions().has(Flags.anvilbreak, FlagCombo.OnlyFalse))
	    return;

	if (Version.isCurrentLower(Version.v1_13_R1)) {
	    try {
		b.getClass().getMethod("setData", byte.class).invoke(b, (byte) 1);
	    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
		e1.printStackTrace();
	    }
	} else {
	    // Need to fix roTation issue
	    b.setType(CMIMaterial.ANVIL.getMaterial());
	}

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlantGrow(BlockGrowEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.grow.isGlobalyEnabled())
	    return;
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.grow, true)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVineGrow(BlockSpreadEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.grow.isGlobalyEnabled())
	    return;
	if (event.getSource().getType() != Material.VINE)
	    return;
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.grow, true)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onleaveDecay(LeavesDecayEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.decay.isGlobalyEnabled())
	    return;
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.decay, true)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTreeGrowt(StructureGrowEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.grow.isGlobalyEnabled())
	    return;
	if (plugin.isDisabledWorldListener(event.getWorld()))
	    return;
	FlagPermissions perms = plugin.getPermsByLoc(event.getLocation());
	if (!perms.has(Flags.grow, true)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTreeGrow(StructureGrowEvent event) {

	if (plugin.isDisabledWorldListener(event.getWorld()))
	    return;

	ClaimedResidence startRes = plugin.getResidenceManager().getByLoc(event.getLocation());
	List<BlockState> blocks = event.getBlocks();

	for (BlockState one : new ArrayList<BlockState>(blocks)) {
	    ClaimedResidence targetRes = plugin.getResidenceManager().getByLoc(one.getLocation());
	    if (startRes == null && targetRes != null || targetRes != null && startRes != null && !startRes.getName().equals(targetRes.getName()) && !startRes.isOwner(targetRes.getOwner())) {
		blocks.remove(one);
	    }
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
	if (!canBreakBlock(event.getPlayer(), event.getBlock(), true))
	    event.setCancelled(true);
    }

    public static boolean canBreakBlock(Player player, Block block, boolean inform) {

	if (player == null)
	    return true;

	// disabling event on world
	if (Residence.getInstance().isDisabledWorldListener(block.getWorld()))
	    return true;

	if (Residence.getInstance().isResAdminOn(player)) {
	    return true;
	}

	Material mat = block.getType();
	String world = block.getWorld().getName();

	ResidencePlayer resPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = resPlayer.getGroup();
	if (Residence.getInstance().getItemManager().isIgnored(mat, group, world)) {
	    return true;
	}

	ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(block.getLocation());

	if (res != null && res.getItemIgnoreList().isListed(mat))
	    return true;

	if (Residence.getInstance().getConfigManager().enabledRentSystem() && res != null) {
	    if (Residence.getInstance().getConfigManager().preventRentModify() && res.isRented()) {
		if (inform)
		    Residence.getInstance().msg(player, lm.Rent_ModifyDeny);
		return false;
	    }
	}

	FlagPermissions perms = Residence.getInstance().getPermsByLocForPlayer(block.getLocation(), player);

	boolean hasdestroy = perms.playerHas(player, Flags.destroy, perms.playerHas(player, Flags.build, true));

	if (res != null && res.getRaid().isUnderRaid()) {
	    if (res.getRaid().isAttacker(player.getUniqueId()) && ConfigManager.RaidAttackerBlockBreak || res.getRaid().isDefender(player.getUniqueId()) && ConfigManager.RaidDefenderBlockBreak) {
		hasdestroy = true;
	    }
	}

	if (!hasdestroy && !ResPerm.bypass_destroy.hasPermission(player, 10000L)) {
	    if (inform)
		Residence.getInstance().msg(player, lm.Flag_Deny, Flags.destroy);
	    return false;
	} else if (mat == Material.CHEST && !perms.playerHas(player, Flags.container, true)) {
	    if (inform)
		Residence.getInstance().msg(player, lm.Flag_Deny, Flags.container);
	    return false;
	}
	return true;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.snowtrail.isGlobalyEnabled())
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	if (!(event instanceof EntityBlockFormEvent))
	    return;

	if (((EntityBlockFormEvent) event).getEntity() instanceof Snowman) {
	    FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	    if (!perms.has(Flags.snowtrail, true)) {
		event.setCancelled(true);
	    }
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onIceForm(BlockFormEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.iceform.isGlobalyEnabled())
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;

	Material ice = Material.getMaterial("FROSTED_ICE");

	if (event.getNewState().getType() != Material.SNOW && event.getNewState().getType() != Material.ICE && ice != null && ice != event.getNewState().getType())
	    return;

	FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.iceform, true)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onIceMelt(BlockFadeEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.icemelt.isGlobalyEnabled())
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;

	if (!CMIMaterial.get(event.getNewState().getType()).equals(CMIMaterial.WATER) && event.getBlock().getState().getType() != Material.SNOW && event.getBlock().getState()
	    .getType() != Material.SNOW_BLOCK)
	    return;

	FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.icemelt, true)) {
	    event.setCancelled(true);
	}
    }

    public static final String SourceResidenceName = "SourceResidenceName";

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.fallinprotection.isGlobalyEnabled())
	    return;
	if (event.getEntityType() != EntityType.FALLING_BLOCK)
	    return;
	Entity ent = event.getEntity();
	if (!ent.hasMetadata(SourceResidenceName)) {
	    ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());
	    String resName = res == null ? "NULL" : res.getName();
	    ent.setMetadata(SourceResidenceName, new FixedMetadataValue(plugin, resName));
	} else {
	    String saved = ent.getMetadata(SourceResidenceName).get(0).asString();
	    ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());

	    if (res != null && res.getPermissions().has(Flags.fallinprotection, FlagCombo.OnlyFalse))
		return;

	    String resName = res == null ? "NULL" : res.getName();
	    if (!saved.equalsIgnoreCase(resName)) {
		event.setCancelled(true);
		ent.remove();
	    }
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFall(EntityChangeBlockEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.fallinprotection.isGlobalyEnabled())
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	if (!plugin.getConfigManager().isBlockFall())
	    return;

	if ((event.getEntityType() != EntityType.FALLING_BLOCK))
	    return;

	if (event.getTo().hasGravity())
	    return;

	Block block = event.getBlock();

	if (block == null)
	    return;

	if (!plugin.getConfigManager().getBlockFallWorlds().contains(block.getLocation().getWorld().getName()))
	    return;

	if (block.getY() <= plugin.getConfigManager().getBlockFallLevel())
	    return;

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(block.getLocation());
	Location loc = new Location(block.getLocation().getWorld(), block.getX(), block.getY(), block.getZ());
	for (int i = loc.getBlockY() - 1; i >= plugin.getConfigManager().getBlockFallLevel() - 1; i--) {
	    loc.setY(i);
	    if (loc.getBlock().getType() != Material.AIR) {
		ClaimedResidence targetRes = plugin.getResidenceManager().getByLoc(loc);
		if (targetRes == null)
		    continue;
		if (res != null && !res.getName().equals(targetRes.getName())) {
		    if (targetRes.getPermissions().has(Flags.fallinprotection, FlagCombo.OnlyFalse))
			continue;
		    event.setCancelled(true);
		    block.setType(Material.AIR);
		}
		return;
	    }
	}
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestPlace(BlockPlaceEvent event) {

	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	if (!plugin.getConfigManager().ShowNoobMessage())
	    return;

	Player player = event.getPlayer();
	if (plugin.isResAdminOn(player))
	    return;
	Block block = event.getBlock();
	if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST)
	    return;

	if (plugin.getPlayerManager().getResidenceCount(player.getUniqueId()) != 0)
	    return;

	if (MessageInformed.contains(player.getName()))
	    return;

	if (!ResPerm.newguyresidence.hasPermission(player))
	    return;

	plugin.msg(player, lm.General_NewPlayerInfo);

	MessageInformed.add(player.getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChestPlaceNearResidence(BlockPlaceEvent event) {

	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;

	Player player = event.getPlayer();
	if (plugin.isResAdminOn(player))
	    return;

	Block block = event.getBlock();
	if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST)
	    return;

	ClaimedResidence orRes = plugin.getResidenceManager().getByLoc(block.getLocation());

	boolean cancel = false;

	ClaimedResidence res = null;
	Block b = block.getLocation().clone().add(0, 0, -1).getBlock();
	if (b.getType() == block.getType()) {
	    res = plugin.getResidenceManager().getByLoc(b.getLocation());
	    if (res != null && !res.equals(orRes))
		cancel = true;
	}
	b = block.getLocation().clone().add(0, 0, 1).getBlock();
	if (b.getType() == block.getType()) {
	    res = plugin.getResidenceManager().getByLoc(b.getLocation());
	    if (res != null && !res.equals(orRes))
		cancel = true;
	}
	b = block.getLocation().clone().add(1, 0, 0).getBlock();
	if (b.getType() == block.getType()) {
	    res = plugin.getResidenceManager().getByLoc(b.getLocation());
	    if (res != null && !res.equals(orRes))
		cancel = true;
	}
	b = block.getLocation().clone().add(-1, 0, 0).getBlock();
	if (b.getType() == block.getType()) {
	    res = plugin.getResidenceManager().getByLoc(b.getLocation());
	    if (res != null && !res.equals(orRes))
		cancel = true;
	}

	if (cancel) {
	    CMIActionBar.send(player, plugin.msg(lm.General_CantPlaceChest));
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestPlaceCreateRes(BlockPlaceEvent event) {
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	if (!plugin.getConfigManager().isNewPlayerUse())
	    return;

	Player player = event.getPlayer();
	if (plugin.isResAdminOn(player))
	    return;
	Block block = event.getBlock();
	if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST)
	    return;

	if (plugin.getPlayerManager().getResidenceCount(player.getUniqueId()) != 0)
	    return;

	if (ResCreated.contains(player.getUniqueId()))
	    return;

	if (!newPlayers.contains(player.getUniqueId()))
	    return;

	Location loc = block.getLocation();

	plugin.getSelectionManager().placeLoc1(player, new Location(loc.getWorld(), loc.getBlockX() - 1, loc.getBlockY() - 1, loc.getBlockZ() - 1), true);
	plugin.getSelectionManager().placeLoc2(player, new Location(loc.getWorld(), loc.getBlockX() + 1, loc.getBlockY() + 1, loc.getBlockZ() + 1), true);

	resize(plugin, player, plugin.getSelectionManager().getSelectionCuboid(player), !plugin.getConfigManager().isNewPlayerFree(),
	    plugin.getConfigManager().getNewPlayerRangeX() * 2,
	    plugin.getConfigManager().getNewPlayerRangeY() * 2,
	    plugin.getConfigManager().getNewPlayerRangeZ() * 2);

	boolean created = plugin.getResidenceManager().addResidence(player, player.getName(), plugin.getSelectionManager().getPlayerLoc1(player),
	    plugin.getSelectionManager().getPlayerLoc2(player), plugin.getConfigManager().isNewPlayerFree());
	if (created) {
	    ResCreated.add(player.getUniqueId());
	    newPlayers.remove(player.getUniqueId());
	}
    }

    public static void resize(Residence plugin, Player player, CuboidArea cuboid, boolean checkBalance, int maxX, int maxY, int maxZ) {

	ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = rPlayer.getGroup();

	double cost = cuboid.getCost(group);

	double balance = 0;
	if (plugin.getEconomyManager() != null)
	    balance = plugin.getEconomyManager().getBalance(player.getName());

	direction dir = direction.Top;

	List<direction> locked = new ArrayList<direction>();

	boolean checkCollision = plugin.getConfigManager().isARCCheckCollision();
	int skipped = 0;
	int done = 0;
	while (true) {
	    done++;

	    if (skipped >= 6) {
		break;
	    }

	    // fail safe if loop keeps going on
	    if (done > 10000)
		break;

	    if (locked.contains(dir)) {
		dir = dir.getNext();
		skipped++;
		continue;
	    }

	    CuboidArea c = new CuboidArea();
	    c.setLowLocation(cuboid.getLowLocation().clone().add(-dir.getLow().getX(), -dir.getLow().getY(), -dir.getLow().getZ()));
	    c.setHighLocation(cuboid.getHighLocation().clone().add(dir.getHigh().getX(), dir.getHigh().getY(), dir.getHigh().getZ()));

	    if (c.getLowVector().getY() < 0) {
		c.getLowVector().setY(0);
		locked.add(dir);
		dir = dir.getNext();
		skipped++;
		continue;
	    }

	    if (c.getHighVector().getY() >= c.getWorld().getMaxHeight()) {
		c.getHighVector().setY(c.getWorld().getMaxHeight() - 1);
		locked.add(dir);
		dir = dir.getNext();
		skipped++;
		continue;
	    }

	    if (checkCollision && plugin.getResidenceManager().collidesWithResidence(c) != null) {
		locked.add(dir);
		dir = dir.getNext();
		skipped++;
		continue;
	    }

	    if (c.getXSize() >= maxX - group.getMinX()) {
		locked.add(dir);
		dir = dir.getNext();
		skipped++;
		continue;
	    }

	    if (c.getYSize() >= maxY - group.getMinY()) {
		locked.add(dir);
		dir = dir.getNext();
		skipped++;
		continue;
	    }

	    if (c.getZSize() >= maxZ - group.getMinZ()) {
		locked.add(dir);
		dir = dir.getNext();
		skipped++;
		continue;
	    }

	    skipped = 0;

	    if (checkBalance) {
		if (plugin.getConfigManager().enableEconomy()) {
		    cost = c.getCost(group);
		    if (cost > balance)
			break;
		}
	    }

	    cuboid.setLowLocation(c.getLowLocation());
	    cuboid.setHighLocation(c.getHighLocation());

	    dir = dir.getNext();
	}

	plugin.getSelectionManager().placeLoc1(player, cuboid.getLowLocation());
	plugin.getSelectionManager().placeLoc2(player, cuboid.getHighLocation());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {

	if (!canPlaceBlock(event.getPlayer(), event.getBlock(), true))
	    event.setCancelled(true);
    }

    public static boolean canPlaceBlock(Player player, Block block, boolean informPlayer) {
	// disabling event on world
	if (Residence.getInstance().isDisabledWorldListener(block.getWorld()))
	    return true;

	if (Residence.getInstance().isResAdminOn(player)) {
	    return true;
	}

	Material mat = block.getType();
	String world = block.getWorld().getName();

	ResidencePlayer resPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = resPlayer.getGroup();
	if (Residence.getInstance().getItemManager().isIgnored(mat, group, world)) {
	    return true;
	}
	ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(block.getLocation());
	if (Residence.getInstance().getConfigManager().enabledRentSystem() && res != null) {
	    String resname = res.getName();
	    if (Residence.getInstance().getConfigManager().preventRentModify() && Residence.getInstance().getRentManager().isRented(resname)) {
		if (informPlayer)
		    Residence.getInstance().msg(player, lm.Rent_ModifyDeny);
		return false;
	    }
	}
	if (!CMIMaterial.get(mat).isNone() && res != null && !res.getItemBlacklist().isAllowed(mat)) {
	    if (informPlayer)
		Residence.getInstance().msg(player, lm.General_ItemBlacklisted);
	    return false;
	}
	FlagPermissions perms = Residence.getInstance().getPermsByLocForPlayer(block.getLocation(), player);
	boolean hasplace = perms.playerHas(player, Flags.place, perms.playerHas(player, Flags.build, true));

	if (res != null && res.getRaid().isUnderRaid()) {
	    if (res.getRaid().isAttacker(player.getUniqueId()) && ConfigManager.RaidAttackerBlockPlace || res.getRaid().isDefender(player.getUniqueId()) && ConfigManager.RaidDefenderBlockPlace) {
		hasplace = true;
	    }
	}

	if (!hasplace && !ResPerm.bypass_build.hasPermission(player, 10000L)) {
	    if (informPlayer)
		Residence.getInstance().msg(player, lm.Flag_Deny, Flags.place);
	    return false;
	}

	if (CMIMaterial.isBed(mat)) {
	    CMIBlock cb = new CMIBlock(block);
	    Block sec = cb.getSecondaryBedBlock();
	    if (sec != null) {
		perms = Residence.getInstance().getPermsByLocForPlayer(sec.getLocation(), player);
		hasplace = perms.playerHas(player, Flags.place, perms.playerHas(player, Flags.build, true));
		if (!hasplace
		    && !ResPerm.bypass_build.hasPermission(player, 10000L)) {
		    if (informPlayer)
			Residence.getInstance().msg(player, lm.Flag_Deny, Flags.place);
		    return false;
		}
	    }
	}
	return true;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.spread.isGlobalyEnabled())
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	Location loc = event.getBlock().getLocation();
	FlagPermissions perms = plugin.getPermsByLoc(loc);
	if (!perms.has(Flags.spread, true)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;

	// Disabling listener if flag disabled globally
	if (!Flags.piston.isGlobalyEnabled())
	    return;

	FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.piston, true)) {
	    event.setCancelled(true);
	    return;
	}

	// Disabling listener if flag disabled globally
	if (!Flags.pistonprotection.isGlobalyEnabled())
	    return;

	List<Block> blocks = Utils.getPistonRetractBlocks(event);

	if (!event.isSticky())
	    return;

	ClaimedResidence pistonRes = plugin.getResidenceManager().getByLoc(event.getBlock().getLocation());

	for (Block block : blocks) {
	    Location locFrom = block.getLocation();
	    ClaimedResidence blockFrom = plugin.getResidenceManager().getByLoc(locFrom);
	    if (blockFrom == null)
		continue;
	    if (blockFrom == pistonRes)
		continue;
	    if (pistonRes != null && blockFrom.isOwner(pistonRes.getOwner()))
		continue;
	    if (!blockFrom.getPermissions().has(Flags.pistonprotection, FlagCombo.OnlyTrue))
		continue;
	    event.setCancelled(true);
	    break;
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {

	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;

	// Disabling listener if flag disabled globally
	if (!Flags.piston.isGlobalyEnabled())
	    return;
	FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.piston, true)) {
	    event.setCancelled(true);
	    return;
	}

	// Disabling listener if flag disabled globally
	if (!Flags.pistonprotection.isGlobalyEnabled())
	    return;

	Location origins = event.getBlock().getLocation();

	int lowestY = 255;
	int bigestY = 0;
	int lowestX = Integer.MAX_VALUE;
	int lowestZ = Integer.MAX_VALUE;
	int bigestX = -Integer.MAX_VALUE;
	int bigestZ = -Integer.MAX_VALUE;

	BlockFace dir = event.getDirection();

	for (Block block : event.getBlocks()) {
	    Location one = block.getLocation().clone().add(dir.getModX(), dir.getModY(), dir.getModZ());
	    if (one.getBlockY() < lowestY)
		lowestY = one.getBlockY();
	    if (one.getBlockX() < lowestX)
		lowestX = one.getBlockX();
	    if (one.getBlockZ() < lowestZ)
		lowestZ = one.getBlockZ();
	    if (one.getBlockY() > bigestY)
		bigestY = one.getBlockY();
	    if (one.getBlockX() > bigestX)
		bigestX = one.getBlockX();
	    if (one.getBlockZ() > bigestZ)
		bigestZ = one.getBlockZ();
	}

	ClaimedResidence pistonRes = plugin.getResidenceManager().getByLoc(event.getBlock().getLocation());

	if (pistonRes != null && pistonRes.containsLoc(new Location(origins.getWorld(), lowestX, lowestY, lowestZ)) && pistonRes.containsLoc(new Location(origins.getWorld(), bigestX, bigestY, bigestZ))) {
	    return;
	}

	for (int i = event.getBlocks().size() - 1; i >= 0; i--) {
	    Block block = event.getBlocks().get(i);
	    Location locTo = block.getLocation().clone().add(dir.getModX(), dir.getModY(), dir.getModZ());
	    ClaimedResidence blockTo = plugin.getResidenceManager().getByLoc(locTo);
	    boolean hasPerm = blockTo != null && blockTo.getPermissions().has(Flags.pistonprotection, FlagCombo.OnlyTrue);
	    if (pistonRes == null && hasPerm || blockTo != null && pistonRes != null && !blockTo.isOwner(pistonRes.getOwner()) && hasPerm) {
		event.setCancelled(true);
		return;
	    }
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;

	ClaimedResidence fromRes = plugin.getResidenceManager().getByLoc(event.getBlock().getLocation());
	ClaimedResidence toRes = plugin.getResidenceManager().getByLoc(event.getToBlock().getLocation());

	FlagPermissions perms = plugin.getPermsByLoc(event.getToBlock().getLocation());
	boolean hasflow = perms.has(Flags.flow, FlagCombo.TrueOrNone);
	Material mat = event.getBlock().getType();

	if (perms.has(Flags.flowinprotection, FlagCombo.TrueOrNone))
	    if (fromRes == null && toRes != null || fromRes != null && toRes != null && !fromRes.equals(toRes) && !fromRes.isOwner(toRes.getOwner())) {
		event.setCancelled(true);
		return;
	    }

	if (perms.has(Flags.flow, FlagCombo.OnlyFalse)) {
	    event.setCancelled(true);
	    return;
	}

	if (mat == Material.LAVA) {
	    if (!perms.has(Flags.lavaflow, hasflow)) {
		event.setCancelled(true);
	    }
	    return;
	}
	if (mat == Material.WATER) {
	    if (!perms.has(Flags.waterflow, hasflow)) {
		event.setCancelled(true);
	    }
	    return;
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLandDryFade(BlockFadeEvent event) {
	// Moved to separate class
	if (Version.isCurrentEqualOrHigher(Version.v1_13_R1))
	    return;
	// Disabling listener if flag disabled globally
	if (!Flags.dryup.isGlobalyEnabled())
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;

	CMIMaterial mat = CMIMaterial.get(event.getBlock());
	if (!mat.equals(CMIMaterial.FARMLAND))
	    return;

	FlagPermissions perms = plugin.getPermsByLoc(event.getNewState().getLocation());
	if (!perms.has(Flags.dryup, true)) {
	    Block b = event.getBlock();
	    try {
		b.getClass().getMethod("setData", byte.class).invoke(b, (byte) 7);
	    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
		e1.printStackTrace();
	    }
	    event.setCancelled(true);
	    return;
	}
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLandDryPhysics(BlockPhysicsEvent event) {
	// Moved to separate class
	if (Version.isCurrentEqualOrHigher(Version.v1_13_R1))
	    return;
	// Disabling listener if flag disabled globally
	if (!Flags.dryup.isGlobalyEnabled())
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;

	if (!event.getBlock().getWorld().isChunkLoaded((int) Math.floor(event.getBlock().getLocation().getX()) >> 4, ((int) Math.floor(event.getBlock().getLocation().getZ()) >> 4)))
	    return;

	CMIMaterial mat = CMIMaterial.get(event.getBlock());
	if (!mat.equals(CMIMaterial.FARMLAND))
	    return;

	FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	if (perms.has(Flags.dryup, FlagCombo.OnlyFalse)) {
	    Block b = event.getBlock();
	    try {
		b.getClass().getMethod("setData", byte.class).invoke(b, (byte) 7);
	    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
		e1.printStackTrace();
	    }
	    event.setCancelled(true);
	    return;
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	if (event.isCancelled())
	    return;

	Location location = new Location(event.getBlock().getWorld(), event.getVelocity().getBlockX(), event.getVelocity().getBlockY(), event.getVelocity().getBlockZ());

	ClaimedResidence targetres = plugin.getResidenceManager().getByLoc(location);

	if (targetres == null && location.getBlockY() >= plugin.getConfigManager().getPlaceLevel() && plugin.getConfigManager().getNoPlaceWorlds().contains(location
	    .getWorld().getName())) {
	    ItemStack mat = event.getItem();
	    if (plugin.getConfigManager().isNoLavaPlace())
		if (mat.getType() == Material.LAVA_BUCKET) {
		    event.setCancelled(true);
		    return;
		}

	    if (plugin.getConfigManager().isNoWaterPlace())
		if (mat.getType() == Material.WATER_BUCKET) {
		    event.setCancelled(true);
		    return;
		}
	}

	ClaimedResidence sourceres = plugin.getResidenceManager().getByLoc(event.getBlock().getLocation());

	if ((sourceres == null && targetres != null || sourceres != null && targetres == null || sourceres != null && targetres != null && !sourceres.getName().equals(
	    targetres.getName())) && (event.getItem().getType() == Material.LAVA_BUCKET || event.getItem().getType() == Material.WATER_BUCKET)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLavaWaterFlow(BlockFromToEvent event) {
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	Material mat = event.getBlock().getType();

	Location location = event.getToBlock().getLocation();
	if (!plugin.getConfigManager().getNoFlowWorlds().contains(location.getWorld().getName()))
	    return;

	if (location.getBlockY() < plugin.getConfigManager().getFlowLevel())
	    return;

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(location);

	if (res != null)
	    return;

	if (plugin.getConfigManager().isNoLava())
	    if (mat == Material.LAVA) {
		event.setCancelled(true);
		return;
	    }

	if (plugin.getConfigManager().isNoWater())
	    if (mat == Material.WATER) {
		event.setCancelled(true);
		return;
	    }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.firespread.isGlobalyEnabled())
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.firespread, true))
	    event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	IgniteCause cause = event.getCause();
	if (cause == IgniteCause.SPREAD) {
	    // Disabling listener if flag disabled globally
	    if (!Flags.firespread.isGlobalyEnabled())
		return;
	    FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	    if (!perms.has(Flags.firespread, true))
		event.setCancelled(true);
	} else if (cause == IgniteCause.FLINT_AND_STEEL) {
	    // Disabling listener if flag disabled globally
	    if (!Flags.ignite.isGlobalyEnabled())
		return;
	    Player player = event.getPlayer();
	    FlagPermissions perms = plugin.getPermsByLocForPlayer(event.getBlock().getLocation(), player);
	    if (player != null && !perms.playerHas(player, Flags.ignite, true) && !plugin.isResAdminOn(player)) {
		event.setCancelled(true);
		plugin.msg(player, lm.Flag_Deny, Flags.ignite);
	    }
	} else {
	    // Disabling listener if flag disabled globally
	    if (!Flags.ignite.isGlobalyEnabled())
		return;
	    FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	    if (!perms.has(Flags.ignite, true)) {
		event.setCancelled(true);
	    }
	}
    }
}
