package com.sovdee.skriptparticle.elements;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import com.sovdee.skriptparticle.util.Style;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class TypeStyle {
    static {

        Classes.registerClass(new ClassInfo<>(Style.class, "shapestyle")
                .user("shapestyles?")
                .name("Shape Style")
                .description("Represents the way the shape is drawn. Outlined is a wireframe representation, Surface is filling in all the surfaces of the shape, and Filled is filling in the entire shape.")
                .parser(new Parser<>() {
                    @Override
                    public @Nullable Style parse(String s, ParseContext context) {
                        s = s.toUpperCase();
                        if (s.matches("OUTLINE(D)?")) {
                            return Style.OUTLINE;
                        } else if (s.matches("SURFACE")) {
                            return Style.SURFACE;
                        } else if (s.matches("FILL(ED)?")) {
                            return Style.FILL;
                        }
                        return null;
                    }

                    @Override
                    public boolean canParse(ParseContext context) {
                        return true;
                    }

                    @Override
                    public String toString(Style style, int i) {
                        return style.toString();
                    }

                    @Override
                    public String toVariableNameString(Style style) {
                        return "shapestyle:" + style.toString();
                    }
                }));
    }
}
