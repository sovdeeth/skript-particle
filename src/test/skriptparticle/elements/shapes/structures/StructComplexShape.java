package com.sovdee.skriptparticle.elements.shapes.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.util.ContextlessEvent;
import com.destroystokyo.paper.ParticleBuilder;
import com.sovdee.skriptparticle.elements.shapes.types.ComplexShape;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;
import org.skriptlang.skript.lang.entry.util.TriggerEntryData;
import org.skriptlang.skript.lang.structure.Structure;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StructComplexShape extends Structure {

    private static final Map<String, ComplexShape> CUSTOM_SHAPES = new ConcurrentHashMap<>();

    static {
        Skript.registerStructure(
                StructComplexShape.class,
                EntryValidator.builder()
                        .addEntryData(new ExpressionEntryData<>("particle", null, true, ParticleBuilder.class, ContextlessEvent.class))
                        .addEntryData(new ExpressionEntryData<>("offset vector", null, true, Vector.class, ContextlessEvent.class))
                        .addEntryData(new ExpressionEntryData<>("rotation", null, true, Quaternion.class, ContextlessEvent.class))
                        .addEntryData(new ExpressionEntryData<>("center location", null, true, Location.class, ContextlessEvent.class))
                        .addEntryData(new TriggerEntryData("shapes", null, false, ContextlessEvent.class))
                        .build(),
                "[a] [new] complex shape [named|with [the] name|with [the] id] %string%"
        );
    }

    private String shapeName;
    private ComplexShape shape;

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult, EntryContainer entryContainer) {
        shapeName = ((Literal<String>) args[0]).getSingle();
        if (CUSTOM_SHAPES.containsKey(shapeName)) {
            Skript.warning("Custom shape with name " + shapeName + " already exists, overwriting...");
        }
        return true;
    }

    @Override
    public boolean load() {
        EntryContainer entryContainer = getEntryContainer();
        Expression<Vector> offsetExpr = ((Expression<Vector>) entryContainer.getOptional("offset vector", Expression.class, true));
        Expression<Quaternion> rotationExpr = ((Expression<Quaternion>) entryContainer.getOptional("rotation", Expression.class, true));
        Expression<Location> centerExpr = ((Expression<Location>) entryContainer.getOptional("center location", Expression.class, true));
        Expression<ParticleBuilder> particleExpr = ((Expression<ParticleBuilder>) entryContainer.getOptional("particle", Expression.class, true));

        shape = new ComplexShape();
        if (offsetExpr != null) {
            shape.offset(offsetExpr.getSingle(ContextlessEvent.get()));
        }
        if (rotationExpr != null) {
            shape.orientation(rotationExpr.getSingle(ContextlessEvent.get()));
        }
        if (centerExpr != null) {
            shape.center(centerExpr.getSingle(ContextlessEvent.get()));
        }
        if (particleExpr != null) {
            shape.particle(particleExpr.getSingle(ContextlessEvent.get()));
        }
        CUSTOM_SHAPES.put(shapeName, shape);
        entryContainer.get("shapes", Trigger.class, true).execute(ContextlessEvent.get());
        return true;
    }

    @Override
    public boolean postLoad() {
        EntryContainer entryContainer = getEntryContainer();
        Expression<ParticleBuilder> particleExpr = ((Expression<ParticleBuilder>) entryContainer.getOptional("particle", Expression.class, true));
        if (particleExpr != null) {
            shape.particle(particleExpr.getSingle(ContextlessEvent.get()));
        }
        return super.postLoad();
    }

    @Override
    public void unload() {
        CUSTOM_SHAPES.remove(shapeName);
        super.unload();
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "Custom shape";
    }

    public ComplexShape getComplexShape() {
        return shape;
    }

    public static Map<String, ComplexShape> getCustomShapes() {
        return CUSTOM_SHAPES;
    }
}
