package com.sovdee.skriptparticle.elements.shapes.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.util.ContextlessEvent;
import com.destroystokyo.paper.ParticleBuilder;
import com.sovdee.skriptparticle.elements.shapes.types.ComplexShape;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.skriptlang.skript.lang.structure.EntryContainer;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.lang.structure.StructureEntryValidator;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StructComplexShape extends Structure {

    private static final Map<String, ComplexShape> CUSTOM_SHAPES = new ConcurrentHashMap<>();

    static {
        Skript.registerStructure(
                StructComplexShape.class,
                StructureEntryValidator.builder()
                        .addEntryData(new ExpressionEntryData<>("particle", null, true, ParticleBuilder.class, ContextlessEvent.class))
//                        .addEntryData(new ExpressionEntryData<>("normal vector", null, true, Vector.class, ContextlessEvent.class))
                        .addEntryData(new ExpressionEntryData<>("offset vector", null, true, Vector.class, ContextlessEvent.class))
//                        .addEntryData(new ExpressionEntryData<>("orientation", null, true, Number.class, ContextlessEvent.class))
                        .addEntryData(new ExpressionEntryData<>("center location", null, true, Location.class, ContextlessEvent.class))
                        .addEntryData(new TriggerEntryData("shapes", null, false, ContextlessEvent.class))
//                        .addSection("shapes", false)
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
//        Expression<Vector> normalExpr = ((Expression<Vector>) entryContainer.getOptional("normal vector", Expression.class, true));
        Expression<Vector> offsetExpr = ((Expression<Vector>) entryContainer.getOptional("offset vector", Expression.class, true));
//        Expression<Number> orientationExpr = ((Expression<Number>) entryContainer.getOptional("orientation", Expression.class, true));
        Expression<Location> centerExpr = ((Expression<Location>) entryContainer.getOptional("center location", Expression.class, true));
        Expression<ParticleBuilder> particleExpr = ((Expression<ParticleBuilder>) entryContainer.getOptional("particle", Expression.class, true));


        shape = new ComplexShape();
        if (offsetExpr != null) {
            shape.offset(offsetExpr.getSingle(ContextlessEvent.get()));
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
