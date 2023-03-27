package com.sovdee.skriptparticle.elements;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;

import javax.annotation.Nullable;

public class TypeStyle {
    static {

        Classes.registerClass(new ClassInfo<>(Shape.Style.class, "shapestyle")
                .user("shapestyles?")
                .name("Shape Style")
                .description("Represents the way the shape is drawn. Outlined is a wireframe representation, Surface is filling in all the surfaces of the shape, and Filled is filling in the entire shape.")
                .parser(new Parser<>() {
                    @Override
                    public @Nullable Shape.Style parse(String s, ParseContext context) {
                        s = s.toUpperCase();
                        if (s.matches("OUTLINE(D)?") || s.matches("WIREFRAME")) {
                            return Shape.Style.OUTLINE;
                        } else if (s.matches("SURFACE") || s.matches("HOLLOW") {
                            return Shape.Style.SURFACE;
                        } else if (s.matches("FILL(ED)?") || s.matches("SOLID")) {
                            return Shape.Style.FILL;
                        }
                        return null;
                    }

                    @Override
                    public boolean canParse(ParseContext context) {
                        return true;
                    }

                    @Override
                    public String toString(Shape.Style style, int i) {
                        return style.toString();
                    }

                    @Override
                    public String toVariableNameString(Shape.Style style) {
                        return "shapestyle:" + style.toString();
                    }
                }));
    }
}
