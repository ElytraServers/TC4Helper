package com.github.wohaopa.tc4helper.mixins;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import cn.elytra.mod.tc4h.IGuiResearchTableHelper;
import cn.elytra.mod.tc4h.TCResearchHelperManager;
import cn.elytra.mod.tc4h.gui.HelperButton;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.gui.GuiResearchTable;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketAspectCombinationToServer;
import thaumcraft.common.lib.network.playerdata.PacketAspectPlaceToServer;
import thaumcraft.common.lib.research.ResearchNoteData;
import thaumcraft.common.lib.utils.HexUtils;
import thaumcraft.common.tiles.TileResearchTable;

@Mixin(value = GuiResearchTable.class, remap = false)
public abstract class GuiResearchTableMixin extends GuiContainer implements IGuiResearchTableHelper {

    private GuiResearchTableMixin(Container p_i1072_1_) {
        super(p_i1072_1_);
    }

    @Shadow
    EntityPlayer player;

    @Shadow
    public ResearchNoteData note;

    @Shadow
    private TileResearchTable tileEntity;

    @Shadow
    private AspectList aspectlist;

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(new HelperButton(101, width / 2 - 25, 20, 50, 20));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button instanceof HelperButton) {
            TCResearchHelperManager.execute(this);
        }
    }

    @Override
    public void tc4h$placeAspect(HexUtils.Hex hex, Aspect aspect) {
        PacketHandler.INSTANCE.sendToServer(
            new PacketAspectPlaceToServer(
                this.player,
                (byte) hex.q,
                (byte) hex.r,
                this.tileEntity.xCoord,
                this.tileEntity.yCoord,
                this.tileEntity.zCoord,
                aspect));
    }

    @Override
    public void tc4h$combineAspect(Aspect aspect1, Aspect aspect2) {
        PacketHandler.INSTANCE.sendToServer(
            new PacketAspectCombinationToServer(
                this.player,
                this.tileEntity.xCoord,
                this.tileEntity.yCoord,
                this.tileEntity.zCoord,
                aspect1,
                aspect2,
                this.tileEntity.bonusAspects.getAmount(aspect1) > 0,
                this.tileEntity.bonusAspects.getAmount(aspect2) > 0,
                true));
    }

    @Override
    public ResearchNoteData tc4h$getResearchNoteData() {
        return note;
    }

    @Override
    public EntityPlayer tc4h$getPlayer() {
        return player;
    }

    @Override
    public AspectList tc4h$getAspectList() {
        return aspectlist;
    }
}
