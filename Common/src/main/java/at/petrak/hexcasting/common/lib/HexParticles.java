package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexParticles {
    private static final Map<ResourceLocation, ParticleType<?>> PARTICLES = new LinkedHashMap<>();

    public static final ConjureParticleOptions.Type CONJURE_PARTICLE = register(
        "conjure_block_particle", new ConjureParticleOptions.Type(false));
    public static final ConjureParticleOptions.Type LIGHT_PARTICLE = register(
        "conjure_light_particle", () -> new ConjureParticleOptions.Type(false));

    private static <T extends ParticleOptions> ParticleType<T> register(String id, ParticleType<T> particle) {
        var old = PARTICLES.put(modLoc(id), particle);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + id);
        }
        return particle;
    }
}
