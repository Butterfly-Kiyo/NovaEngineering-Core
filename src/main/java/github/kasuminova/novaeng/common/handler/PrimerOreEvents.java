package github.kasuminova.novaeng.common.handler;

import github.kasuminova.novaeng.common.performance.PrimerOreChunk;
import github.kasuminova.novaeng.common.performance.PrimerOreGeneration;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;

public final class PrimerOreEvents {
    public static final PrimerOreEvents INSTANCE = new PrimerOreEvents();
    private static final String KEY = "NovaPrimerOreRulesV1";

    private PrimerOreEvents() {
    }

    public static void read(final PrimerOreChunk chunk, final NBTTagCompound data) {
        final NBTBase stored = data.getTag(KEY);
        if (stored instanceof NBTTagLongArray array) {
            // field_193587_b (SRG) / data (MCP): read via reflection so the private vanilla field
            // stays accessible at runtime regardless of whether an AccessTransformer is applied.
            final long[] raw = ObfuscationReflectionHelper.getPrivateValue(NBTTagLongArray.class, array, "field_193587_b", "data");
            final long[] rules = raw.clone();
            Arrays.sort(rules);
            chunk.nova$setPrimerOreRules(rules);
        }
    }

    public static void write(final PrimerOreChunk chunk, final NBTTagCompound data) {
        final long[] rules = chunk.nova$getPrimerOreRules();
        if (rules != null && rules.length != 0) data.setTag(KEY, new NBTTagLongArray(rules));
    }

    @SubscribeEvent
    public void onLoad(final ChunkDataEvent.Load event) {
        if (event.getChunk() instanceof PrimerOreChunk chunk) read(chunk, event.getData());
    }

    @SubscribeEvent
    public void onSave(final ChunkDataEvent.Save event) {
        if (event.getChunk() instanceof PrimerOreChunk chunk) write(chunk, event.getData());
    }

    @SubscribeEvent
    public void onUnload(final WorldEvent.Unload event) {
        PrimerOreGeneration.unload(event.getWorld());
    }
}
