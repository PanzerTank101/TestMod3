package choonster.testmod3.network;

import choonster.testmod3.TestMod3;
import choonster.testmod3.item.ItemLootTableTest;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Sent by {@link ItemLootTableTest} to notify the player of the loot they received.
 * <p>
 * This message is needed because there's no builtin way to send an {@link ItemStack}'s display name as a {@link ITextComponent}
 * apart from {@link ItemStack#getTextComponent()}, which incorrectly performs translations on the server.
 *
 * @author Choonster
 */
public class MessagePlayerReceivedLoot implements IMessage {
	/**
	 * The {@link ItemStack}s received by the player.
	 */
	private ItemStack[] itemStacks;

	@SuppressWarnings("unused")
	public MessagePlayerReceivedLoot() {
	}

	public MessagePlayerReceivedLoot(final Collection<ItemStack> itemStacks) {
		this.itemStacks = itemStacks.toArray(new ItemStack[itemStacks.size()]);
	}

	/**
	 * Convert from the supplied buffer into your specific message type
	 *
	 * @param buf The buffer
	 */
	@Override
	public void fromBytes(final ByteBuf buf) {
		final int numStacks = buf.readInt();

		itemStacks = new ItemStack[numStacks];

		for (int i = 0; i < numStacks; i++) {
			itemStacks[i] = ByteBufUtils.readItemStack(buf);
		}
	}

	/**
	 * Deconstruct your message into the supplied byte buffer
	 *
	 * @param buf The buffer
	 */
	@Override
	public void toBytes(final ByteBuf buf) {
		buf.writeInt(itemStacks.length);

		for (final ItemStack itemStack : itemStacks) {
			ByteBufUtils.writeItemStack(buf, itemStack);
		}
	}

	public static class Handler implements IMessageHandler<MessagePlayerReceivedLoot, IMessage> {

		/**
		 * Get an {@link ITextComponent} with the quantity and display name of the {@link ItemStack}.
		 *
		 * @param itemStack The ItemStack
		 * @return The ITextComponent
		 */
		private ITextComponent getItemStackTextComponent(final ItemStack itemStack) {
			return new TextComponentTranslation("message.testmod3:player_received_loot.item", itemStack.getCount(), itemStack.getTextComponent());
		}

		/**
		 * Called when a message is received of the appropriate type. You can optionally return a reply message, or null if no reply
		 * is needed.
		 *
		 * @param message The message
		 * @param ctx     The message context
		 * @return an optional return message
		 */
		@Nullable
		@Override
		public IMessage onMessage(final MessagePlayerReceivedLoot message, final MessageContext ctx) {
			TestMod3.proxy.getThreadListener(ctx).addScheduledTask(() -> {
				final EntityPlayer player = TestMod3.proxy.getPlayer(ctx);

				final ITextComponent lootMessage = getItemStackTextComponent(message.itemStacks[0]);
				for (int i = 1; i < message.itemStacks.length; i++) {
					lootMessage.appendText(", ");
					lootMessage.appendSibling(getItemStackTextComponent(message.itemStacks[i]));
				}

				final ITextComponent chatMessage = new TextComponentTranslation("message.testmod3:player_received_loot.base", lootMessage);

				player.sendMessage(chatMessage);
			});

			return null;
		}
	}
}
