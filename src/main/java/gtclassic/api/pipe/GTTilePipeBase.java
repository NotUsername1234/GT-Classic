package gtclassic.api.pipe;

import java.util.ArrayList;
import java.util.List;

import gtclassic.api.interfaces.IGTDebuggableTile;
import gtclassic.api.interfaces.IGTMonkeyWrenchTile;
import gtclassic.common.item.GTItemMonkeyWrench;
import ic2.api.classic.network.adv.NetworkField;
import ic2.core.IC2;
import ic2.core.RotationList;
import ic2.core.block.base.tile.TileEntityBlock;
import ic2.core.inventory.base.IHasGui;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.gui.GuiComponentContainer;
import ic2.core.inventory.management.AccessRule;
import ic2.core.inventory.management.IHasHandler;
import ic2.core.inventory.management.InventoryHandler;
import ic2.core.inventory.management.SlotType;
import ic2.core.item.misc.ItemDisplayIcon;
import ic2.core.util.math.MathUtil;
import ic2.core.util.obj.IItemContainer;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public abstract class GTTilePipeBase extends TileEntityBlock implements IHasGui, IHasInventory, IHasHandler,
		IItemContainer, ITickable, IGTDebuggableTile, IGTMonkeyWrenchTile {

	@NetworkField(index = 8)
	public RotationList connection;
	public EnumFacing lastRecievedFrom;
	public boolean onlyPipes;
	public int idle;
	protected InventoryHandler handler = new InventoryHandler(this);
	public NonNullList<ItemStack> inventory;
	public int slotCount;
	public static final int TICK_RATE = 10;

	public GTTilePipeBase(int slots) {
		this.onlyPipes = false;
		this.idle = 0;
		this.connection = RotationList.EMPTY;
		this.addNetworkFields(new String[] { "connection" });
		this.slotCount = slots;
		this.inventory = NonNullList.withSize(this.slotCount, ItemStack.EMPTY);
		this.addSlots(this.handler);
		this.handler.validateSlots();
	}

	protected void addSlots(InventoryHandler handler) {
		int[] array = MathUtil.fromTo(0, this.inventory.size());
		handler.registerDefaultSideAccess(AccessRule.Both, RotationList.ALL);
		handler.registerDefaultSlotAccess(AccessRule.Both, array);
		handler.registerDefaultSlotsForSide(RotationList.ALL, array);
		handler.registerSlotType(SlotType.Storage, array);
	}

	public abstract boolean canConnect(TileEntity tile, EnumFacing dir);

	@Override
	public InventoryHandler getHandler() {
		return this.handler;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.onlyPipes = nbt.getBoolean("onlyPipes");
		this.idle = nbt.getInteger("idle");
		this.handler.readFromNBT(nbt.getCompoundTag("HandlerNBT"));
		this.inventory = NonNullList.withSize(this.slotCount, ItemStack.EMPTY);
		if (nbt.getInteger("lastRecievedFrom") != -1) {
			this.lastRecievedFrom = EnumFacing.getFront(nbt.getInteger("lastRecievedFrom"));
		}
		NBTTagList list = nbt.getTagList("Items", 10);
		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound data = list.getCompoundTagAt(i);
			byte byte0 = data.getByte("Slot");
			if (byte0 >= 0 && byte0 < this.inventory.size()) {
				this.inventory.set(byte0, new ItemStack(data));
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("onlyPipes", this.onlyPipes);
		nbt.setInteger("idle", this.idle);
		nbt.setInteger("lastRecievedFrom", lastRecievedFrom != null ? this.lastRecievedFrom.getIndex() : -1);
		this.handler.writeToNBT(this.getTag(nbt, "HandlerNBT"));
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < this.inventory.size(); ++i) {
			ItemStack stack = this.inventory.get(i);
			if (!stack.isEmpty()) {
				NBTTagCompound data = new NBTTagCompound();
				data.setByte("Slot", (byte) i);
				stack.writeToNBT(data);
				list.appendTag(data);
			}
		}
		nbt.setTag("Items", list);
		return nbt;
	}

	@Override
	public List<ItemStack> getDrops() {
		List<ItemStack> list = new ArrayList<>(this.inventory.size());
		for (int i = 0; i < this.inventory.size(); ++i) {
			ItemStack stack = this.inventory.get(i);
			if (!stack.isEmpty() && !(stack.getItem() instanceof ItemDisplayIcon)) {
				list.add(stack);
			}
		}
		InventoryHandler handler = this.getHandler();
		if (handler != null) {
			IHasInventory inv = handler.getUpgradeSlots();
			for (int i = 0; i < inv.getSlotCount(); ++i) {
				ItemStack result = inv.getStackInSlot(i);
				if (!result.isEmpty()) {
					list.add(result);
				}
			}
		}
		return list;
	}

	@Override
	public boolean onMonkeyWrench(EntityPlayer player, World world, BlockPos pos, EnumFacing side, EnumHand hand) {
		if (player.isSneaking()) {
			this.togglePipeOnlyMode();
			if (this.isSimulating()) {
				String msg = this.onlyPipes ? "Will only flow into pipes" : "Will flow into any connection";
				IC2.platform.messagePlayer(player, msg);
			}
			return true;
		}
		return false;
	}

	public Vec3i getConnections() {
		return new Vec3i(this.connection.getCode(), 0, 0);
	}

	@Override
	public void onLoaded() {
		super.onLoaded();
		updateConnections();
	}

	public void updateConnections() {
		for (EnumFacing facing : EnumFacing.VALUES) {
			BlockPos sidedPos = pos.offset(facing);
			if (world.isBlockLoaded(sidedPos)) {
				world.neighborChanged(sidedPos, Blocks.AIR, pos);
			}
		}
		if (world.isBlockLoaded(pos)) {
			world.neighborChanged(pos, Blocks.AIR, pos);
		}
	}

	@Override
	public boolean canUpdate() {
		return this.isSimulating();
	}

	@Override
	public void onBlockUpdate(Block block) {
		super.onBlockUpdate(block);
		if (!this.isRendering()) {
			RotationList newList = RotationList.EMPTY;
			EnumFacing[] var3 = EnumFacing.VALUES;
			int var4 = var3.length;
			for (int var5 = 0; var5 < var4; ++var5) {
				EnumFacing dir = var3[var5];
				TileEntity tile = this.getWorld().getTileEntity(this.getPos().offset(dir));
				if (canConnect(tile, dir)) {
					newList = newList.add(dir);
				}
			}
			if (this.connection.getCode() != newList.getCode()) {
				this.connection = newList;
				this.getNetwork().updateTileEntityField(this, "connection");
			}
		}
	}

	@Override
	public void onNetworkUpdate(String field) {
		if (field.equals("connection")) {
			this.world.markBlockRangeForRenderUpdate(this.getPos(), this.getPos());
		}
		super.onNetworkUpdate(field);
	}

	@Override
	public boolean canRemoveBlock(EntityPlayer player) {
		return true;
	}

	public abstract boolean isEmpty();

	public void update() {
		if (world.getTotalWorldTime() % TICK_RATE == 0) {
			this.updateIdle();
			if (isEmpty()) {
				return;
			}
			onPipeTick();
		}
	}

	public abstract void onPipeTick();

	public void togglePipeOnlyMode() {
		this.onlyPipes = !this.onlyPipes;
	}

	public void updateIdle() {
		this.idle = idle + TICK_RATE;
		if (this.idle >= 100) {
			this.lastRecievedFrom = null;
			this.idle = 0;
		}
	}

	public boolean isLastRecievedFrom(EnumFacing side) {
		if (lastRecievedFrom == null) {
			return false;
		}
		return side == lastRecievedFrom;
	}

	@Override
	public Class<? extends GuiScreen> getGuiClass(EntityPlayer player) {
		return GuiComponentContainer.class;
	}

	@Override
	public void onGuiClosed(EntityPlayer var1) {
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return !this.isInvalid() && GTItemMonkeyWrench.doesPlayerHaveMonkeyWrench(player);
	}

	@Override
	public boolean hasGui(EntityPlayer player) {
		return GTItemMonkeyWrench.doesPlayerHaveMonkeyWrench(player);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		this.inventory.set(slot, stack);
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return this.inventory.get(slot);
	}

	@Override
	public int getSlotCount() {
		return this.slotCount;
	}

	@Override
	public int getMaxStackSize(int slot) {
		return 64;
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack) {
		return true;
	}
}