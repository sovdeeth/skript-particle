package com.sovdee.skriptparticle.elements.shapes;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.events.bukkit.ScriptEvent;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.lang.util.SimpleLiteral;
import com.sovdee.skriptparticle.shapes.ComplexShape;
import org.bukkit.Particle;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.skriptlang.skript.lang.structure.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.lang.structure.StructureEntryValidator;
import org.skriptlang.skript.lang.structure.util.ExpressionStructureEntryData;
import org.skriptlang.skript.lang.structure.util.LiteralStructureEntryData;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StructComplexShape extends Structure {

    private static final Map<String, ComplexShape> CUSTOM_SHAPES = new ConcurrentHashMap<>();

    static {
        Skript.registerStructure(
                StructComplexShape.class,
                StructureEntryValidator.builder()
                        .addEntryData(new LiteralStructureEntryData<>("particle", null, true, Particle.class))
                        .addEntryData(new ExpressionStructureEntryData<>("normal", new SimpleLiteral<>(new Vector(0,1,0), true), true, Vector.class, Event.class))
                        .addEntryData(new LiteralStructureEntryData<>("orientation", 0, true, Number.class))
                        .addSection("shapes", false)
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
        shape = new ComplexShape(
                ((Expression<Vector>) entryContainer.getOptional("normal", Expression.class, true)).getSingle(new ScriptEvent()),
                entryContainer.getOptional("orientation", Number.class, true).doubleValue(),
                entryContainer.getOptional("particle", Particle.class, true)
        );

        CUSTOM_SHAPES.put(shapeName, shape);

        SectionNode shapeNode = entryContainer.get("shapes", SectionNode.class, true);
        Trigger shapeTrigger = new Trigger(this.getParser().getCurrentScript(), "shape:" + shapeName, new SimpleEvent(), ScriptLoader.loadItems(shapeNode));
        shapeTrigger.execute(new ScriptEvent());

        return true;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "Custom shape";
    }

    public ComplexShape getComplexShape() {
        return shape;
    }

    public static Map<String, ComplexShape> getCustomShapes() {
        return CUSTOM_SHAPES;
    }
}
