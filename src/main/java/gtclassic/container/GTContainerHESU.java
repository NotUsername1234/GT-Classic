package gtclassic.container;

import gtclassic.GTClassic;
import gtclassic.tileentity.GTTileEntityHESU;
import ic2.core.inventory.container.ContainerTileComponent;
import ic2.core.inventory.slots.SlotArmor;
import ic2.core.inventory.slots.SlotCharge;
import ic2.core.inventory.slots.SlotDischarge;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;


import static ic2.core.block.wiring.container.ContainerElectricBlock.VALID_EQUIPMENT_SLOTS;

public class GTContainerHESU extends ContainerTileComponent<GTTileEntityHESU> {
	
	public static ResourceLocation TEXTURE = new ResourceLocation(GTClassic.MODID, "textures/gui/hesu.png");
    
	public GTContainerHESU(InventoryPlayer player, GTTileEntityHESU tile)
    {
        super(tile);
        this.addSlotToContainer(new SlotDischarge(tile, tile.tier, 0, 128, 50)); //battery
        this.addSlotToContainer(new SlotCharge(tile, tile.tier, 1, 128, 14));

        for(int i = 0; i < 4; ++i) 
        {
            this.addSlotToContainer(new SlotArmor(player, 3 - i, VALID_EQUIPMENT_SLOTS[i], 152, 5 + i * 18));
        }

        this.addPlayerInventory(player);
    }

	@Override
    public ResourceLocation getTexture() 
    {
        return TEXTURE;
    }

    @Override
    public int guiInventorySize() 
    {
    	return 2;
    }
    
    @Override
    protected boolean moveIntoInventoryInverted() {
		return false;
	}

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) 
    {
        return this.getGuiHolder().canInteractWith(playerIn);
    }
}
