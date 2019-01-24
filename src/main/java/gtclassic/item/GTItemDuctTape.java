package gtclassic.item;

import java.util.Arrays;
import java.util.List;

import gtclassic.GTMod;
import ic2.core.platform.textures.Ic2Icons;
import ic2.core.platform.textures.obj.IStaticTexturedItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GTItemDuctTape extends Item implements IStaticTexturedItem {

	public GTItemDuctTape() {
		this.maxStackSize = 1;
		this.setMaxDamage(255);
		setRegistryName("braintech_aerospace_ardt");
		setUnlocalizedName(GTMod.MODID + ".braintech_aerospace_ardt");
		setCreativeTab(GTMod.creativeTabGT);
	}

	@Override
	public EnumRarity getRarity(ItemStack thisItem) {
		return EnumRarity.UNCOMMON;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {

		tooltip.add(I18n.format("tooltip." + GTMod.MODID + ".ducttape"));
		tooltip.add((stack.getMaxDamage() + 1) - stack.getItemDamage() + "/256");
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack getContainerItem(ItemStack itemStack) {
		ItemStack copy = itemStack.copy();
		return copy.attemptDamageItem(1, itemRand, null) ? ItemStack.EMPTY : copy;
	}

	@Override
	@Deprecated
	public boolean hasContainerItem() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getTexture(int i) {
		return Ic2Icons.getTextures(GTMod.MODID + "_items")[56];
	}

	@Override
	public List<Integer> getValidVariants() {
		return Arrays.asList(0);
	}

}
