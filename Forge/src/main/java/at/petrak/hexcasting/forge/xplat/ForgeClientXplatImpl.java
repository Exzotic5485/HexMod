package at.petrak.hexcasting.forge.xplat;

import at.petrak.hexcasting.common.network.IMessage;
import at.petrak.hexcasting.forge.network.ForgePacketHandler;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;

public class ForgeClientXplatImpl implements IClientXplatAbstractions {
    @Override
    public void sendPacketToServer(IMessage packet) {
        ForgePacketHandler.getNetwork().sendToServer(packet);
    }

    @Override
    public void registerItemProperty(Item item, ResourceLocation id, ClampedItemPropertyFunction func) {
        ItemProperties.register(item, id, func);
    }

    @Override
    public void setRenderLayer(Block block, RenderType type) {
        ItemBlockRenderTypes.setRenderLayer(block, type);
    }

    @Override
    public <T extends Entity> void registerEntityRenderer(EntityType<? extends T> type,
        EntityRendererProvider<T> renderer) {
        EntityRenderers.register(type, renderer);
    }

    @Override
    public <T extends ParticleOptions> void registerParticleType(ParticleType<T> type,
        Function<SpriteSet, ParticleProvider<T>> factory) {
        Minecraft.getInstance().particleEngine.register(type, factory::apply);
    }
}
