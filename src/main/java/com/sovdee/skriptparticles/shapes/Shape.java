package com.sovdee.skriptparticles.shapes;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import com.sovdee.skriptparticles.SkriptParticle;
import com.sovdee.skriptparticles.particles.Particle;
import com.sovdee.skriptparticles.util.ParticleUtil;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.StreamCorruptedException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class Shape {

    protected Set<Vector> points;
    protected Style style;
    protected Quaternion orientation;
    protected Quaternion lastOrientation;
    protected double scale;
    protected Vector offset;
    private final UUID uuid;
    protected Particle particle;
    protected double particleDensity = 0.25;

    protected State lastState;
    protected Location lastLocation;
    protected boolean needsUpdate = false;

    protected boolean drawLocalAxes = false;
    protected boolean drawGlobalAxes = false;

    public Shape() {
        this.style = Style.OUTLINE;
        this.points = new HashSet<>();

        this.orientation = new Quaternion(1, 0, 0, 0);

        this.scale = 1;
        this.offset = new Vector(0, 0, 0);

        this.uuid = UUID.randomUUID();

        this.lastState = getState();
    }

    /*
     * Gets the points for the shape.
     * Attempts to use cached points if possible, by checking if the shape has been modified.
     * @return A set of points that make up the shape.
     */
    public Set<Vector> getPoints() {
        return getPoints(this.orientation);
    }

    public Set<Vector> getPoints(Quaternion orientation) {
        State state = getState(orientation);
        if (needsUpdate || !lastState.equals(state) || points.isEmpty()) {
            SkriptParticle.info("Updating shape " + this + " for reason: " + (needsUpdate ? "needsUpdate" : points.isEmpty() ? "points.isEmpty()" : "lastState != getState()"));
            points = generatePoints();
            for (Vector point : points) {
                orientation.transform(point);
                point.multiply(scale);
                point.add(offset);
            }

            lastState = state;
            needsUpdate = false;
        }
        return points;
    }

    /*
     * Sets the points for the shape.
     */
    public void setPoints(Set<Vector> points) {
        this.points = points;
    }

    /*
     * Generates the points for the shape. Depends on the set style.
     * @return A set of points that make up the shape.
     */
    public Set<Vector> generatePoints() {
        return switch (style) {
            case OUTLINE -> generateOutline();
            case SURFACE -> generateSurface();
            case FILL -> generateFilled();
        };
    }

    /*
     * Generates the points for the shape, if the style is set to OUTLINE.
     * @return A set of points that make up the shape.
     */
    public abstract Set<Vector> generateOutline();

    /*
     * Generates the points for the shape, if the style is set to SURFACE.
     * Default implementation is to return the same as generateOutline().
     * @return A set of points that make up the shape.
     */
    public Set<Vector> generateSurface(){
        return generateOutline();
    }

    /*
     * Generates the points for the shape, if the style is set to FILL.
     * Default implementation is to return the same as generateSurface().
     * @return A set of points that make up the shape.
     */
    public Set<Vector> generateFilled(){
        return generateSurface();
    }


    /*
     * Draws a shape at a location, given a starting orientation and a particle to use
     * Caches the last orientation used to draw the shape, so that it can be updated if the orientation changes.
     */
    public void draw(Location location, @Nullable Quaternion baseOrientation, @Nullable Particle particle, @Nullable Collection<Player> recipients) {
        // catch null values
        if (particle == null) {
            particle = (Particle) new Particle(org.bukkit.Particle.FLAME).parent(this).extra(0);
        }
        if (baseOrientation == null) {
            baseOrientation = Quaternion.IDENTITY;
        }
        // cache the last location and orientation used to draw the shape
        lastLocation = location.clone();
        lastOrientation = baseOrientation.clone().multiply(orientation);

        // If the particle doesn't override the shape's particle, use the shape's particle
        if (this.particle != null && !particle.override()) {
            this.particle.parent(this);
            particle = this.particle;
            // update the gradient if needed
            if (particle.gradient() != null && particle.gradient().isLocal()) {
                particle.gradient().setOrientation(lastOrientation);
            }
        }

        SkriptParticle.info("Drawing shape " + this.getClass().getSimpleName() + " at " + location + " with orientation " + lastOrientation + " and particle " + particle);
        SkriptParticle.info("Shape has " + getPoints(lastOrientation).size() + " points:");

        particle.receivers(recipients);
        for (Vector point : getPoints(lastOrientation)) {
            particle.spawn(point);
        }

        if (drawLocalAxes) {
            ParticleUtil.drawAxes(location, lastOrientation);
        }
        if (drawGlobalAxes) {
            ParticleUtil.drawAxes(location, Quaternion.IDENTITY);
        }
    }

    /*
     * Draws a shape at a location, given a starting orientation and a particle to use
     * The provided consumer is run before the shape is drawn, allowing for the shape to be modified before drawing.
     * This method should not be called by a complex shape, but only by the user via EffSecDrawShape.
     */
    public void draw(Location location, Consumer<Shape> consumer, @Nullable Collection<Player> recipients) {
        consumer.accept(this);
        draw(location, this.orientation, this.particle, recipients);
    }

    /*
     * @Returns the relative X axis of the shape, either using its default orientation, or the last orientation used to draw the shape.
     */
    public Vector getRelativeXAxis(boolean useLastOrientation) {
        return (useLastOrientation ? lastOrientation : orientation).transform(new Vector(1, 0, 0));
    }

    /*
     * @Returns the relative Y axis of the shape, either using its default orientation, or the last orientation used to draw the shape.
     */
    public Vector getRelativeYAxis(boolean useLastOrientation) {
        return (useLastOrientation ? lastOrientation : orientation).transform(new Vector(0, 1, 0));
    }

    /*
     * @Returns the relative Z axis of the shape, either using its default orientation, or the last orientation used to draw the shape.
     */
    public Vector getRelativeZAxis(boolean useLastOrientation) {
        return (useLastOrientation ? lastOrientation : orientation).transform(new Vector(0, 0, 1));
    }

    /*
     * Sets whether the shape will draw its local axes.
     * @param show Whether the shape should draw its local axes.
     */
    public void showLocalAxes(boolean show) {
        drawLocalAxes = show;
    }

    /*
     * @Returns whether the shape will draw its local axes.
     */
    public boolean showLocalAxes() {
        return drawLocalAxes;
    }

    /*
     * Sets whether the shape will draw its global axes.
     * @param show Whether the shape should draw its global axes.
     */
    public void showGlobalAxes(boolean show) {
        drawGlobalAxes = show;
    }

    /*
     * @Returns whether the shape will draw its global axes.
     */
    public boolean showGlobalAxes() {
        return drawGlobalAxes;
    }

    /*
     * @Returns the last location used to draw the shape.
     */
    public Location getLastLocation() {
        return lastLocation;
    }

    /*
     * @Returns the style of the shape.
     */
    public Style getStyle() {
        return style;
    }

    /*
     * Sets the style of the shape. Marks the shape as needing an update.
     */
    public void setStyle(Style style) {
        this.style = style;
        needsUpdate = true;
    }

    /*
     * @Returns the orientation of the shape. Changes to the orientation will
     * cause the shape to update upon the next getPoints() call.
     */
    public Quaternion getOrientation() {
        return orientation.clone();
    }

    /*
     * Sets the orientation of the shape. Marks the shape as needing an update.
     */
    public void setOrientation(Quaternion orientation) {
        this.orientation = orientation.clone();
        needsUpdate = true;
    }

    /*
     * @Returns the scale of the shape.
     */
    public double getScale() {
        return scale;
    }

    /*
     * Sets the scale of the shape. Marks the shape as needing an update.
     */
    public void setScale(double scale) {
        this.scale = scale;
        needsUpdate = true;
    }

    /*
     * @Returns the offset of the shape.
     */
    public Vector getOffset() {
        return offset.clone();
    }

    /*
     * Sets the offset of the shape. Marks the shape as needing an update.
     */
    public void setOffset(Vector offset) {
        this.offset = offset.clone();
        needsUpdate = true;
    }

    /*
     * @Returns the UUID of the shape. Used for uniqueness during serialization.
     */
    public UUID getUUID() {
        return uuid;
    }

    /*
     * @Returns the particle of the shape.
     */
    public Particle getParticle() {
        return particle;
    }

    /*
     * Sets the particle of the shape.
     */
    public void setParticle(Particle particle) {
        this.particle = particle;
    }

    /*
     * @Returns the particle density of the shape.
     */
    public double getParticleDensity() {
        return particleDensity;
    }

    /*
     * Sets the particle density of the shape. Marks the shape as needing an update.
     */
    public void setParticleDensity(double particleDensity) {
        this.particleDensity = particleDensity;
        needsUpdate = true;
    }

    /*
     * Sets the number of points that the shape should have. Will not always be accurate, but should be close.
     * Marks the shape as needing an update.
     */
    public abstract void setParticleCount(int particleCount);

    /*
     * @Returns the number of points that the shape has.
     */
    public int getParticleCount() {
        return getPoints().size();
    }

    /*
     * @Returns whether the shape needs an update.
     */
    public boolean needsUpdate() {
        return needsUpdate;
    }

    /*
     * Marks the shape as needing an update or not.
     */
    public void setNeedsUpdate(boolean needsUpdate) {
        this.needsUpdate = needsUpdate;
    }

    /*
     * @Returns a copy of the shape.
     */
    public abstract Shape clone();

    /*
     * Used for copying the shape's properties to a new shape. Intended to be used in the clone() method.
     * @Returns the updated new shape.
     */
    public Shape copyTo(Shape shape){
        shape.setOrientation(this.orientation);
        shape.setScale(this.scale);
        shape.setOffset(this.offset);
        shape.setParticle(this.particle);
        shape.setParticleDensity(this.particleDensity);
        shape.setStyle(this.style);
        // ensure that the shape's points are updated, so we don't have to recalculate them unless we change the copy.
        shape.setPoints(this.getPoints());
        shape.setNeedsUpdate(this.needsUpdate);
        shape.setLastState(this.lastState);
        return shape;
    };

    /*
     * Gets the physical state of a shape, represented by its style, orientation, scale, offset, and particle density.
     * Used for checking if a shape has changed since the last time it was drawn.
     * @Returns A state object that represents the shape's physical state.
     */
    private State getState() {
        return new State(style, orientation.hashCode(), scale, offset.hashCode(), particleDensity);
    }

    /*
     * Gets the physical state of a shape, but with a custom orientation.
     * @Param orientation The orientation to use for the state.
     * @Returns A state object that represents the shape's physical state.
     */
    private State getState(Quaternion orientation) {
        return new State(style, orientation.hashCode(), scale, offset.hashCode(), particleDensity);
    }

    private void setLastState(State state) {
        this.lastState = state;
    }

    protected record State(Style style, int orientationHash, double scale, int offsetHash, double particleDensity) {

        /*
         * @Returns whether the state is equal to another state.
         */
        public boolean equals(State state) {
            return state.style() == style &&
                    state.orientationHash() == orientationHash &&
                    state.scale() == scale &&
                    state.offsetHash() == offsetHash &&
                    state.particleDensity() == particleDensity;
        }
    }

    public enum Style {
        OUTLINE,
        SURFACE,
        FILL;

        public String toString() {
            return name().toLowerCase();
        }
    }

    /*
     * Serializes the shape for storage in variables.
     */
    public void serialize(Fields fields) {
        orientation.serialize(fields, "o");
        fields.putPrimitive("scale", scale);
        fields.putObject("offset", offset);
        fields.putPrimitive("density", particleDensity);
    }

    /*
     * Deserializes the shape from a variable.
     */
    public static Shape deserialize(Fields fields, Shape shape) throws StreamCorruptedException {
        shape.orientation = Quaternion.deserialize(fields, "o");
        shape.scale = fields.getPrimitive("scale", double.class);
        shape.offset = fields.getObject("offset", Vector.class);
        shape.particleDensity = fields.getPrimitive("density", double.class);
        return shape;
    }

    static {
        Classes.registerClass(new ClassInfo<>(Shape.class, "shape")
                .user("shapes?")
                .name("Shape")
                .description("Represents an abstract particle shape. See various shapes for implementations. eg: circle, line, etc.")
                .parser(new Parser<>() {

                    @Override
                    public Shape parse(String input, ParseContext context) {
                        return null;
                    }

                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }

                    @Override
                    public String toString(Shape o, int flags) {
                        return o.toString();
                    }

                    @Override
                    public String toVariableNameString(Shape shape) {
                        return "shape:" + shape.getUUID();
                    }
                })
        );

    }
}
