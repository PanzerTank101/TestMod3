package choonster.testmod3.client.event;

import choonster.testmod3.TestMod3;
import choonster.testmod3.item.ItemModBow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = TestMod3.MODID)
public class ClientEventHandler {
	private static final Minecraft MINECRAFT = Minecraft.getMinecraft();

	@SubscribeEvent
	public static void onFOVUpdate(final FOVUpdateEvent event) {
		if (event.getEntity().isHandActive() && event.getEntity().getActiveItemStack().getItem() instanceof ItemModBow) {
			float fovModifier = event.getEntity().getItemInUseMaxCount() / 20.0f;

			if (fovModifier > 1.0f) {
				fovModifier = 1.0f;
			} else {
				fovModifier *= fovModifier;
			}

			event.setNewfov(event.getFov() * (1.0f - fovModifier * 0.15f));
		}
	}

	/**
	 * Rotate the player every tick while they're standing on a Block of Iron.
	 * <p>
	 * Test for this thread:
	 * http://www.minecraftforge.net/forum/index.php/topic,36093.0.html
	 *
	 * @param event The event
	 */
	@SubscribeEvent
	public static void onClientTick(final TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END && MINECRAFT.player != null) {
			final EntityPlayer player = MINECRAFT.player;
			if (MINECRAFT.world.getBlockState(new BlockPos(player).down()).getBlock() == Blocks.IRON_BLOCK) {
				player.turn(5, 0);
			}
		}
	}

	/**
	 * When an {@link EntityMinecart} is spawned on the client side, add it to a {@link Team} and make it glow.
	 * <p>
	 * Test for this thread:
	 * http://www.minecraftforge.net/forum/topic/50836-adding-an-entity-other-than-a-player-to-a-team/
	 *
	 * @param event The event
	 */
	@SubscribeEvent
	public static void entityJoinWorld(final EntityJoinWorldEvent event) {
		final World world = event.getWorld();
		final Entity entity = event.getEntity();

		if (world.isRemote && entity instanceof EntityMinecart) {
			final Scoreboard scoreboard = world.getScoreboard();
			if (scoreboard.getTeam(TestMod3.MODID) == null) {
				final ScorePlayerTeam team = scoreboard.createTeam(TestMod3.MODID);
				team.setPrefix(TextFormatting.DARK_AQUA.toString());
				team.setColor(TextFormatting.DARK_AQUA);
			}

			scoreboard.addPlayerToTeam(entity.getCachedUniqueIdString(), TestMod3.MODID);
			entity.setGlowing(true);
		}
	}
}
