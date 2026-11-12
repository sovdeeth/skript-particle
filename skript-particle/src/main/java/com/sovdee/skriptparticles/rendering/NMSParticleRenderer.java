package com.sovdee.skriptparticles.rendering;

import com.sovdee.shapes.sampling.ShapeRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftParticle;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;

/**
 * Sends particles via NMS packets, using direct ByteBuf pipeline injection for optimal performance.
 * <p>
 * Implements {@link ShapeRenderer} so the library's render loop calls {@link #renderPoint} per-point.
 * Bundles up to 4096 particles per frame using bundle delimiters.
 * <p>
 * Handles version differences in the packet constructor:
 * <ul>
 *     <li>1.21.0 - 1.21.3: {@code (T, boolean overrideLimiter, double x,y,z, float xDist,yDist,zDist,maxSpeed, int count)}</li>
 *     <li>1.21.4+: {@code (T, boolean overrideLimiter, boolean alwaysShow, double x,y,z, float xDist,yDist,zDist,maxSpeed, int count)}</li>
 * </ul>
 */
public class NMSParticleRenderer implements ShapeRenderer<ParticleRenderContext> {

    private static final Logger LOGGER = Logger.getLogger(NMSParticleRenderer.class.getName());

    /**
     * Maximum number of packets per bundle. The protocol supports up to 4096.
     */
    private static final int MAX_BUNDLE_SIZE = 4096;

    /**
     * Warn when a shape draws more than this many particles in a single call.
     */
    /**
     * How many distinct particle data objects to keep converted options for.
     * Gradients use at most 256; anything producing more than this is not worth caching.
     */
    private static final int MAX_CACHED_OPTIONS = 1024;

    /**
     * Functional interface for packet creation, eliminating branching in hot loop.
     */
    @FunctionalInterface
    private interface PacketFactory {
        ClientboundLevelParticlesPacket create(
                ParticleOptions options, boolean force,
                double x, double y, double z,
                float xDist, float yDist, float zDist, float maxSpeed, int count
        ) throws Throwable;
    }

    /**
     * Cached packet factory using the version-appropriate constructor.
     * Null if reflection lookup failed (will fall back to Bukkit API).
     */
    private static final @Nullable PacketFactory PACKET_FACTORY;

    static {
        PacketFactory factory = null;

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            // Try the 2-boolean constructor first (1.21.4+)
            MethodHandle ctor = lookup.findConstructor(ClientboundLevelParticlesPacket.class, MethodType.methodType(
                    void.class,
                    ParticleOptions.class, boolean.class, boolean.class,
                    double.class, double.class, double.class,
                    float.class, float.class, float.class, float.class,
                    int.class
            ));
            factory = (options, force, x, y, z, xDist, yDist, zDist, maxSpeed, count) ->
                    (ClientboundLevelParticlesPacket) ctor.invokeExact(
                            options, force, force, x, y, z, xDist, yDist, zDist, maxSpeed, count
                    );
        } catch (NoSuchMethodException | IllegalAccessException e) {
            try {
                // Fall back to 1-boolean constructor (1.21.0 - 1.21.3)
                //noinspection JavaLangInvokeHandleSignature
                MethodHandle ctor = lookup.findConstructor(ClientboundLevelParticlesPacket.class, MethodType.methodType(
                        void.class,
                        ParticleOptions.class, boolean.class,
                        double.class, double.class, double.class,
                        float.class, float.class, float.class, float.class,
                        int.class
                ));
                factory = (options, force, x, y, z, xDist, yDist, zDist, maxSpeed, count) ->
                        (ClientboundLevelParticlesPacket) ctor.invokeExact(
                                options, force, x, y, z, xDist, yDist, zDist, maxSpeed, count
                        );
            } catch (NoSuchMethodException | IllegalAccessException ex) {
                LOGGER.log(Level.WARNING, "Could not find ClientboundLevelParticlesPacket constructor. " +
                        "NMS packet bundling will be unavailable.", ex);
            }
        }

        PACKET_FACTORY = factory;
    }

    /**
     * Returns true if NMS packet sending is available on this server version.
     */
    public static boolean isAvailable() {
        return PACKET_FACTORY != null;
    }

    // ---- Instance fields (set up in constructor, used across begin/renderPoint/end) ----

    private final List<ServerPlayer> serverPlayers;
    private final double baseX, baseY, baseZ;

    private final boolean force;
    private final float defaultOffsetX, defaultOffsetY, defaultOffsetZ;
    private final int defaultCount;
    private final float speed;

    /**
     * Pre-computed NMS options for the default particle (used when no modifier changed
     * particle/data).
     */
    private final ParticleOptions defaultOptions;
    // Converted options for particle data set by modifiers, keyed by the data object's identity.
    // Modifiers such as gradients hand out the same few data objects for every point.
    private final Map<Object, ParticleOptions> optionsCache = new IdentityHashMap<>();
    private org.bukkit.Particle cachedOptionsParticle;

    private List<Packet<? super ClientGamePacketListener>> chunk;

    // the context to use for rendering
    private final ParticleRenderContext context;

    /**
     * Creates a renderer instance for one draw call.
     *
     * @param particle   the particle configuration (also stored as defaults in the context)
     * @param recipients pre-filtered list of players
     * @param baseLoc    world-space base location for point offsets
     */
    public NMSParticleRenderer(ParticleEffect particle, List<Player> recipients, Location baseLoc) {
        this.baseX = baseLoc.getX();
        this.baseY = baseLoc.getY();
        this.baseZ = baseLoc.getZ();

        this.force = particle.force();
        this.defaultOffsetX = (float) particle.offsetX();
        this.defaultOffsetY = (float) particle.offsetY();
        this.defaultOffsetZ = (float) particle.offsetZ();
        this.defaultCount = particle.count();
        this.speed = (float) particle.speedX();
        this.defaultOptions = CraftParticle.createParticleParam(particle.particle(), particle.data());

        this.serverPlayers = new ArrayList<>(recipients.size());
        for (Player player : recipients) {
            this.serverPlayers.add(((CraftPlayer) player).getHandle());
        }

        this.context = new ParticleRenderContext(particle);
    }

    @Override
    public ParticleRenderContext getContext() {
        return context;
    }

    @Override
    public void begin(int totalPoints) {
        chunk = new ArrayList<>(Math.min(Math.max(totalPoints, 1), MAX_BUNDLE_SIZE));
    }

    @Override
    public void renderPoint(ParticleRenderContext context) {
        if (!context.visible) return;

        float offsetX = defaultOffsetX;
        float offsetY = defaultOffsetY;
        float offsetZ = defaultOffsetZ;
        int count = defaultCount;

        double px = baseX + context.x + context.displacementX;
        double py = baseY + context.y + context.displacementY;
        double pz = baseZ + context.z + context.displacementZ;

        // Motion override
        if (context.hasMotion) {
            offsetX = context.motionX;
            offsetY = context.motionY;
            offsetZ = context.motionZ;
            count = 0;
        }

        // Use default cached options if no modifier changed the particle; recompute otherwise.
        ParticleOptions options = context.isDefaultParticle()
                ? defaultOptions
                : optionsFor(context.particle, context.data);

        ClientboundLevelParticlesPacket pkt = createPacket(
                options, force, px, py, pz, offsetX, offsetY, offsetZ, speed, count
        );
        // the packet could not be created; the failure has already been logged
        if (pkt == null) return;
        chunk.add(pkt);

        if (chunk.size() >= MAX_BUNDLE_SIZE) {
            sendChunkToPlayers(chunk, serverPlayers);
            chunk = new ArrayList<>(MAX_BUNDLE_SIZE);
        }
    }

    @Override
    public void end() {
        if (!chunk.isEmpty()) {
            sendChunkToPlayers(chunk, serverPlayers);
        }
    }

    /**
     * Converts a particle and its data to NMS options, reusing the result for data objects seen before.
     */
    private ParticleOptions optionsFor(org.bukkit.Particle particle, @Nullable Object data) {
        if (data == null)
            return CraftParticle.createParticleParam(particle, null);
        // the cache is keyed by data alone, so it only holds options for one particle type at a time
        if (particle != cachedOptionsParticle) {
            optionsCache.clear();
            cachedOptionsParticle = particle;
        }
        ParticleOptions options = optionsCache.get(data);
        if (options == null) {
            options = CraftParticle.createParticleParam(particle, data);
            if (optionsCache.size() < MAX_CACHED_OPTIONS)
                optionsCache.put(data, options);
        }
        return options;
    }

    /**
     * Creates a particle packet using the version-appropriate constructor.
     */
    private static ClientboundLevelParticlesPacket createPacket(
            ParticleOptions options, boolean force,
            double x, double y, double z,
            float xDist, float yDist, float zDist, float maxSpeed, int count) {
        try {
            assert PACKET_FACTORY != null;
            return PACKET_FACTORY.create(options, force, x, y, z, xDist, yDist, zDist, maxSpeed, count);
        } catch (Throwable e) {
            LOGGER.log(Level.WARNING, "Failed to create particle packet", e);
            return null;
        }
    }

    /**
     * Sends a chunk of packets to all players as a single bundle each.
     */
    private static void sendChunkToPlayers(List<Packet<? super ClientGamePacketListener>> chunk, List<ServerPlayer> players) {
        ClientboundBundlePacket bundle = new ClientboundBundlePacket(chunk);
        for (ServerPlayer player : players) {
            player.connection.send(bundle);
        }
    }
}
