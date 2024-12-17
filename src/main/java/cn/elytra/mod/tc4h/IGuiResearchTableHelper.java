package cn.elytra.mod.tc4h;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.lib.research.ResearchNoteData;
import thaumcraft.common.lib.utils.HexUtils;

public interface IGuiResearchTableHelper {

    void tc4h$combineAspect(Aspect aspect, Aspect aspect2);

    void tc4h$placeAspect(HexUtils.Hex hex, Aspect aspect);

    @Nullable
    ResearchNoteData tc4h$getResearchNoteData();

    EntityPlayer tc4h$getPlayer();

    AspectList tc4h$getAspectList();

}
