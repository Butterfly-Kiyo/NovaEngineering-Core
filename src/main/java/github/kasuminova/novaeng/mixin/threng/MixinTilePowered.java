package github.kasuminova.novaeng.mixin.threng;

import github.kasuminova.novaeng.common.util.OnlyWriteEnergyContainer;
import net.minecraftforge.energy.IEnergyStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(targets = "io.github.phantamanta44.threng.tile.base.TilePowered", remap = false)
public abstract class MixinTilePowered {

    // LibNine reports canExtract() == true even when the reservoir's output rate is zero.
    @ModifyArg(method = "initCapabilities()Lio/github/phantamanta44/libnine/capability/provider/CapabilityBrokerDirPredicated;",
        at = @At(value = "INVOKE",
            target = "Lio/github/phantamanta44/libnine/capability/provider/CapabilityBrokerDirPredicated;with(Lnet/minecraftforge/common/capabilities/Capability;Ljava/lang/Object;)Lio/github/phantamanta44/libnine/capability/provider/CapabilityBrokerDirPredicated;"),
        index = 1, require = 1)
    private Object nova$inputOnlyEnergy(final Object energy) {
        return new OnlyWriteEnergyContainer((IEnergyStorage) energy);
    }
}
