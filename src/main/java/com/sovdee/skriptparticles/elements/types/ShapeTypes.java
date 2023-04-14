package com.sovdee.skriptparticles.elements.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import com.sovdee.skriptparticles.shapes.CutoffShape;
import com.sovdee.skriptparticles.shapes.LWHShape;
import com.sovdee.skriptparticles.shapes.RadialShape;
import com.sovdee.skriptparticles.shapes.Shape;

public class ShapeTypes {
    static {
        // Shape
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
                        return "shape:" + shape.getUUID();
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
                    public RadialShape parse(String input, ParseContext context) {
                        return null;
                    }

                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }

                    @Override
                    public String toString(RadialShape o, int flags) {
                        return o.toString();
                    }

                    @Override
                    public String toVariableNameString(RadialShape shape) {
                        return "shape:" + shape.getUUID();
                    }
                })
        );

        // LWHShape
        Classes.registerClass(new ClassInfo<>(LWHShape.class, "lwhshape")
                .user("lwh ?shapes?")
                .name("Length/Width/Height Shape")
                .description("Represents an abstract particle shape that has a length, width, and/or height. E.g. cube, cylinder, ellipse, etc.")
                .parser(new Parser<>() {

                    @Override
                    public LWHShape parse(String input, ParseContext context) {
                        return null;
                    }

                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }

                    @Override
                    public String toString(LWHShape o, int flags) {
                        return o.toString();
                    }

                    @Override
                    public String toVariableNameString(LWHShape shape) {
                        return "shape:" + shape.getUUID();
                    }
                })

        );

        // CutoffShape
        Classes.registerClass(new ClassInfo<>(CutoffShape.class, "cutoffshape")
                .user("cutoff ?shapes?")
                .name("Cutoff Shape")
                .description("Represents an abstract particle shape that has a cutoff angle. E.g. arc, spherical cap, etc.")
                .parser(new Parser<>() {

                    @Override
                    public CutoffShape parse(String input, ParseContext context) {
                        return null;
                    }

                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }

                    @Override
                    public String toString(CutoffShape o, int flags) {
                        return o.toString();
                    }

                    @Override
                    public String toVariableNameString(CutoffShape shape) {
                        return "shape:" + shape.getUUID();
                    }
                })
        );
    }
}
