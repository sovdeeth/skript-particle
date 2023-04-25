package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.SkriptParticle;
import com.sovdee.skriptparticles.particles.Particle;
import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.ParticleUtil;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractShape implements Shape {

    protected Set<Vector> points;
    protected Style style;
    protected Quaternion orientation;
    protected Quaternion lastOrientation;
    protected double scale;
    protected Vector offset;
    protected DynamicLocation location;
    private final UUID uuid;
    protected Particle particle;
    protected double particleDensity = 0.25;

    protected State lastState;
    protected Location lastLocation;
    protected boolean needsUpdate = false;

    protected boolean drawLocalAxes = false;
    protected boolean drawGlobalAxes = false;

    public AbstractShape() {
        this.style = Style.OUTLINE;
        this.points = new HashSet<>();

        this.orientation = Quaternion.IDENTITY.clone();

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
    @Override
    public Set<Vector> getPoints() {
        return getPoints(this.orientation);
    }

    @Override
    public Set<Vector> getPoints(Quaternion orientation) {
        State state = getState(orientation);
        if (needsUpdate || !lastState.equals(state) || points.isEmpty()) {
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
    @Override
    public void setPoints(Set<Vector> points) {
        this.points = points;
    }

    /*
     * Generates the points for the shape. Depends on the set style.
     * @return A set of points that make up the shape.
     */
    @Override
    public Set<Vector> generatePoints() {
        return switch (style) {
            case OUTLINE -> generateOutline();
            case SURFACE -> generateSurface();
            case FILL -> generateFilled();
        };
    }

    /*
     * Generates the points for the shape, if the style is set to SURFACE.
     * Default implementation is to return the same as generateOutline().
     * @return A set of points that make up the shape.
     */
    @Override
    public Set<Vector> generateSurface(){
        return generateOutline();
    }

    /*
     * Generates the points for the shape, if the style is set to FILL.
     * Default implementation is to return the same as generateSurface().
     * @return A set of points that make up the shape.
     */
    @Override
    public Set<Vector> generateFilled(){
        return generateSurface();
    }


    /*
     * Draws a shape at a location, given a starting orientation and a particle to use
     * Caches the last orientation used to draw the shape, so that it can be updated if the orientation changes.
     */
    @Override
    public void draw(@Nullable Location location, @Nullable Quaternion baseOrientation, @Nullable Particle particle, @Nullable Collection<Player> recipients) {
        // catch null values
        if (particle == null) {
            particle = (Particle) new Particle(org.bukkit.Particle.FLAME).parent(this).extra(0);
        }
        if (baseOrientation == null) {
            baseOrientation = Quaternion.IDENTITY;
        }
        if (location == null) {
            if (this.location == null) {
                // consider adding a warning here
                SkriptParticle.warning("Shape " + this + " has no location set, and no location was provided to draw the shape at.");
                return;
            }
            location = this.location.getLocation();
        }

        // cache the last location and orientation used to draw the shape
        lastLocation = location.clone();
        lastOrientation = (Quaternion) baseOrientation.clone().mul(orientation);

        // If the particle doesn't override the shape's particle, use the shape's particle
        if (this.particle != null && !particle.override()) {
            this.particle.parent(this);
            particle = this.particle;
            // update the gradient if needed
            if (particle.gradient() != null && particle.gradient().isLocal()) {
                particle.gradient().setOrientation(lastOrientation);
            }
        }

//        SkriptParticle.info("Drawing shape " + this.getClass().getSimpleName() + " at " + location + " with orientation " + lastOrientation + " and particle " + particle);
//        SkriptParticle.info("Shape has " + getPoints(lastOrientation).size() + " points:");

        particle.receivers(recipients);
        for (Vector point : getPoints(lastOrientation)) {
            particle.spawn(point);
        }

        if (drawLocalAxes) {
            ParticleUtil.drawAxes(location.clone().add(offset), lastOrientation, recipients);
        }
        if (drawGlobalAxes) {
            ParticleUtil.drawAxes(location.clone().add(offset), Quaternion.IDENTITY, recipients);
        }
    }

    /*
     * Draws a shape at a location, given a starting orientation and a particle to use
     * The provided consumer is run before the shape is drawn, allowing for the shape to be modified before drawing.
     * This method should not be called by a complex shape, but only by the user via EffSecDrawShape.
     */
    @Override
    public void draw(@Nullable Location location, Consumer<Shape> consumer, @Nullable Collection<Player> recipients) {
        consumer.accept(this);
        draw(location, this.orientation, this.particle, recipients);
    }

    /*
     * @Returns the relative X axis of the shape, either using its default orientation, or the last orientation used to draw the shape.
     */
    @Override
    public Vector getRelativeXAxis(boolean useLastOrientation) {
        return (useLastOrientation ? lastOrientation : orientation).transform(new Vector(1, 0, 0));
    }

    /*
     * @Returns the relative Y axis of the shape, either using its default orientation, or the last orientation used to draw the shape.
     */
    @Override
    public Vector getRelativeYAxis(boolean useLastOrientation) {
        return (useLastOrientation ? lastOrientation : orientation).transform(new Vector(0, 1, 0));
    }

    /*
     * @Returns the relative Z axis of the shape, either using its default orientation, or the last orientation used to draw the shape.
     */
    @Override
    public Vector getRelativeZAxis(boolean useLastOrientation) {
        return (useLastOrientation ? lastOrientation : orientation).transform(new Vector(0, 0, 1));
    }

    /*
     * Sets whether the shape will draw its local axes.
     * @param show Whether the shape should draw its local axes.
     */
    @Override
    public void showLocalAxes(boolean show) {
        drawLocalAxes = show;
    }

    /*
     * @Returns whether the shape will draw its local axes.
     */
    @Override
    public boolean showLocalAxes() {
        return drawLocalAxes;
    }

    /*
     * Sets whether the shape will draw its global axes.
     * @param show Whether the shape should draw its global axes.
     */
    @Override
    public void showGlobalAxes(boolean show) {
        drawGlobalAxes = show;
    }

    /*
     * @Returns whether the shape will draw its global axes.
     */
    @Override
    public boolean showGlobalAxes() {
        return drawGlobalAxes;
    }

    /*
     * @Returns the last location used to draw the shape.
     */
    @Override
    public Location getLastLocation() {
        return lastLocation;
    }

    /*
     * @Returns the style of the shape.
     */
    @Override
    public Style getStyle() {
        return style;
    }

    /*
     * Sets the style of the shape. Marks the shape as needing an update.
     */
    @Override
    public void setStyle(Style style) {
        this.style = style;
        needsUpdate = true;
    }

    /*
     * @Returns the orientation of the shape. Changes to the orientation will
     * cause the shape to update upon the next getPoints() call.
     */
    @Override
    public Quaternion getOrientation() {
        return new Quaternion(orientation);
    }

    /*
     * Sets the orientation of the shape. Marks the shape as needing an update.
     */
    @Override
    public void setOrientation(Quaternionf orientation) {
        this.orientation.set(orientation);
        needsUpdate = true;
    }

    /*
     * @Returns the scale of the shape.
     */
    @Override
    public double getScale() {
        return scale;
    }

    /*
     * Sets the scale of the shape. Marks the shape as needing an update.
     */
    @Override
    public void setScale(double scale) {
        this.scale = scale;
        needsUpdate = true;
    }

    /*
     * @Returns the offset of the shape.
     */
    @Override
    public Vector getOffset() {
        return offset.clone();
    }

    /*
     * Sets the offset of the shape. Marks the shape as needing an update.
     */
    @Override
    public void setOffset(Vector offset) {
        this.offset = offset.clone();
        needsUpdate = true;
    }

    /*
     * Sets the location of the shape. This is used as a fallback if the shape is drawn without a location.
     */
    @Override
    public void setLocation(DynamicLocation location) {
        this.location = location.clone();
    }

    /*
     * @Returns the location of the shape.
     */
    @Override
    public DynamicLocation getLocation() {
        return location.clone();
    }

    /*
     * @Returns the UUID of the shape. Used for uniqueness during serialization.
     */
    @Override
    public UUID getUUID() {
        return uuid;
    }

    /*
     * @Returns the particle of the shape.
     */
    @Override
    public Particle getParticle() {
        return particle.clone();
    }

    /*
     * Sets the particle of the shape.
     */
    @Override
    public void setParticle(Particle particle) {
        this.particle = particle.clone();
    }

    /*
     * @Returns the particle density of the shape.
     */
    @Override
    public double getParticleDensity() {
        return particleDensity;
    }

    /*
     * Sets the particle density of the shape. Marks the shape as needing an update.
     */
    @Override
    public void setParticleDensity(double particleDensity) {
        this.particleDensity = particleDensity;
        needsUpdate = true;
    }

    /*
     * @Returns the number of points that the shape has.
     */
    @Override
    public int getParticleCount() {
        return getPoints().size();
    }

    /*
     * @Returns whether the shape needs an update.
     */
    @Override
    public boolean needsUpdate() {
        return needsUpdate;
    }

    /*
     * Marks the shape as needing an update or not.
     */
    @Override
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
    @Override
    public Shape copyTo(Shape shape){
        shape.setOrientation(this.orientation);
        shape.setScale(this.scale);
        shape.setOffset(this.offset);
        if (this.particle != null)
            shape.setParticle(this.particle);
        if (this.location != null)
            shape.setLocation(this.location);
        shape.setParticleDensity(this.particleDensity);
        shape.setStyle(this.style);
        shape.showLocalAxes(this.drawLocalAxes);
        shape.showGlobalAxes(this.drawGlobalAxes);
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
    @Override
    public State getState() {
        return new State(style, orientation.hashCode(), scale, offset.hashCode(), particleDensity);
    }

    /*
     * Gets the physical state of a shape, but with a custom orientation.
     * @Param orientation The orientation to use for the state.
     * @Returns A state object that represents the shape's physical state.
     */
    @Override
    public State getState(Quaternion orientation) {
        return new State(style, orientation.hashCode(), scale, offset.hashCode(), particleDensity);
    }

    @Override
    public void setLastState(State state) {
        this.lastState = state;
    }

    //    /*
//     * Serializes the shape for storage in variables.
//     */
//    public void serialize(Fields fields) {
//        orientation.serialize(fields, "o");
//        fields.putPrimitive("scale", scale);
//        fields.putObject("offset", offset);
//        fields.putPrimitive("density", particleDensity);
//    }
//
//    /*
//     * Deserializes the shape from a variable.
//     */
//    public static AbstractShape deserialize(Fields fields, AbstractShape shape) throws StreamCorruptedException {
//        shape.orientation = Quaternion.deserialize(fields, "o");
//        shape.scale = fields.getPrimitive("scale", double.class);
//        shape.offset = fields.getObject("offset", Vector.class);
//        shape.particleDensity = fields.getPrimitive("density", double.class);
//        return shape;
//    }

}
