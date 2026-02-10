package com.sovdee.skriptparticles.elements.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.shapes.shapes.CutoffShape;
import com.sovdee.shapes.shapes.LWHShape;
import com.sovdee.shapes.shapes.PolyShape;
import com.sovdee.shapes.shapes.RadialShape;
import com.sovdee.shapes.shapes.Shape;
import org.jetbrains.annotations.Nullable;

public class ShapeTypes {
    static {
        // Shape — register the library shape directly
        Classes.registerClass(new ClassInfo<>(Shape.class, "shape")
                .user("shapes?")
                .name("Shape")
                .description("Represents an abstract particle shape. E.g. circle, line, etc.")
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
                        return "shape:" + shape.getPointSampler().getUUID();
                    }
                })
                .cloner(Shape::clone)
        );

        // RadialShape
        Classes.registerClass(new ClassInfo<>(RadialShape.class, "radialshape")
                .user("radial ?shapes?")
                .name("Radial Shape")
                .description("Represents an abstract particle shape that has a radius. E.g. circle, sphere, etc.")
                .parser(new Parser<>() {
                    @Override
                    public RadialShape parse(String input, ParseContext context) { return null; }
                    @Override
                    public boolean canParse(ParseContext context) { return false; }
                    @Override
                    public String toString(RadialShape o, int flags) { return o.toString(); }
                    @Override
                    public String toVariableNameString(RadialShape shape) { return "shape:" + shape.getPointSampler().getUUID(); }
                })
        );

        // LWHShape
        Classes.registerClass(new ClassInfo<>(LWHShape.class, "lwhshape")
                .user("lwh ?shapes?")
                .name("Length/Width/Height Shape")
                .description("Represents an abstract particle shape that has a length, width, and/or height. E.g. cube, cylinder, ellipse, etc.")
                .parser(new Parser<>() {
                    @Override
                    public LWHShape parse(String input, ParseContext context) { return null; }
                    @Override
                    public boolean canParse(ParseContext context) { return false; }
                    @Override
                    public String toString(LWHShape o, int flags) { return o.toString(); }
                    @Override
                    public String toVariableNameString(LWHShape shape) { return "shape:" + shape.getPointSampler().getUUID(); }
                })
        );

        // CutoffShape
        Classes.registerClass(new ClassInfo<>(CutoffShape.class, "cutoffshape")
                .user("cutoff ?shapes?")
                .name("Cutoff Shape")
                .description("Represents an abstract particle shape that has a cutoff angle. E.g. arc, spherical cap, etc.")
                .parser(new Parser<>() {
                    @Override
                    public CutoffShape parse(String input, ParseContext context) { return null; }
                    @Override
                    public boolean canParse(ParseContext context) { return false; }
                    @Override
                    public String toString(CutoffShape o, int flags) { return o.toString(); }
                    @Override
                    public String toVariableNameString(CutoffShape shape) { return "shape:" + shape.getPointSampler().getUUID(); }
                })
        );

        // PolyShape
        Classes.registerClass(new ClassInfo<>(PolyShape.class, "polyshape")
                .user("poly ?shapes?")
                .name("Polygonal/Polyhedral Shape")
                .description(
                        "Represents an abstract particle shape that is a polygon or polyhedron, with a side length and side count.\n" +
                                "Irregular shapes are included in this category, but do not support changing either side count or side length."
                )
                .parser(new Parser<>() {
                    @Override
                    public PolyShape parse(String input, ParseContext context) { return null; }
                    @Override
                    public boolean canParse(ParseContext context) { return false; }
                    @Override
                    public String toString(PolyShape o, int flags) { return o.toString(); }
                    @Override
                    public String toVariableNameString(PolyShape shape) { return "shape:" + shape.getPointSampler().getUUID(); }
                })
        );

        // Style — use standalone SamplingStyle enum
        Classes.registerClass(new ClassInfo<>(SamplingStyle.class, "shapestyle")
                .user("shape ?styles?")
                .name("Shape Style")
                .description("Represents the way the shape is drawn. Outlined is a wireframe representation, Surface is filling in all the surfaces of the shape, and Filled is filling in the entire shape.")
                .parser(new Parser<>() {
                    @Override
                    public @Nullable SamplingStyle parse(String s, ParseContext context) {
                        s = s.toUpperCase();
                        if (s.matches("OUTLINE(D)?") || s.matches("WIREFRAME")) {
                            return SamplingStyle.OUTLINE;
                        } else if (s.matches("SURFACE") || s.matches("HOLLOW")) {
                            return SamplingStyle.SURFACE;
                        } else if (s.matches("FILL(ED)?") || s.matches("SOLID")) {
                            return SamplingStyle.FILL;
                        }
                        return null;
                    }

                    @Override
                    public boolean canParse(ParseContext context) {
                        return true;
                    }

                    @Override
                    public String toString(SamplingStyle style, int i) {
                        return style.toString();
                    }

                    @Override
                    public String toVariableNameString(SamplingStyle style) {
                        return "shapestyle:" + style;
                    }
                }));
    }
}
