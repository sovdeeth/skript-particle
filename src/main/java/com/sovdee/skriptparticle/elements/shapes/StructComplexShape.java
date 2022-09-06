package com.sovdee.skriptparticle.elements.shapes;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.util.ContextlessEvent;
import com.sovdee.skriptparticle.particles.CustomParticle;
import com.sovdee.skriptparticle.shapes.ComplexShape;
import com.sovdee.skriptparticle.shapes.ShapePosition;
import com.sovdee.skriptparticle.util.FlaggedExpressionStructureEntryData;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.skriptlang.skript.lang.structure.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.lang.structure.StructureEntryValidator;
import org.skriptlang.skript.lang.structure.util.TriggerStructureEntryData;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StructComplexShape extends Structure {

    private static final Map<String, ComplexShape> CUSTOM_SHAPES = new ConcurrentHashMap<>();

    static {
        Skript.registerStructure(
                StructComplexShape.class,
                StructureEntryValidator.builder()
                        .addEntryData(new FlaggedExpressionStructureEntryData<>("particle", null, true, CustomParticle.class, SkriptParser.ALL_FLAGS, ContextlessEvent.class))
                        .addEntryData(new FlaggedExpressionStructureEntryData<>("normal vector", null, true, Vector.class, SkriptParser.ALL_FLAGS, ContextlessEvent.class))
                        .addEntryData(new FlaggedExpressionStructureEntryData<>("offset vector", null, true, Vector.class, SkriptParser.ALL_FLAGS, ContextlessEvent.class))
                        .addEntryData(new FlaggedExpressionStructureEntryData<>("orientation", null, true, Number.class, SkriptParser.ALL_FLAGS, ContextlessEvent.class))
                        .addEntryData(new FlaggedExpressionStructureEntryData<>("center location", null, true, Location.class, SkriptParser.ALL_FLAGS, ContextlessEvent.class))
                        .addEntryData(new TriggerStructureEntryData("shapes", null,false, ContextlessEvent.class))
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
        Expression<Vector> normalExpr = ((Expression<Vector>) entryContainer.getOptional("normal vector", Expression.class, true));
        Expression<Vector> offsetExpr = ((Expression<Vector>) entryContainer.getOptional("offset vector", Expression.class, true));
        Expression<Number> orientationExpr = ((Expression<Number>) entryContainer.getOptional("orientation", Expression.class, true));
        Expression<Location> centerExpr = ((Expression<Location>) entryContainer.getOptional("center location", Expression.class, true));
        Expression<CustomParticle> particleExpr = ((Expression<CustomParticle>) entryContainer.getOptional("particle", Expression.class, true));

        ShapePosition position = new ShapePosition(
                normalExpr == null ? new Vector(0, 1, 0) : normalExpr.getSingle(ContextlessEvent.get()),
                offsetExpr == null ? new Vector(0, 0, 0) : offsetExpr.getSingle(ContextlessEvent.get()),
                orientationExpr == null ? 0 : (orientationExpr.getSingle(ContextlessEvent.get())) == null ? 0 : orientationExpr.getSingle(ContextlessEvent.get()).doubleValue()
                );

        shape = new ComplexShape(
                position,
                particleExpr == null ? null : particleExpr.getSingle(ContextlessEvent.get()),
                centerExpr == null ? null : centerExpr.getSingle(ContextlessEvent.get()));

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
