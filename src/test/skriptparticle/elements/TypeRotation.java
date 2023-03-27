package com.sovdee.skriptparticle.elements;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import com.sovdee.skriptparticles.util.Quaternion;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class TypeRotation {
    static {
        Classes.registerClass(new ClassInfo<>(Quaternion.class, "rotation")
                .user("rotations?")
                .name("Rotation")
                .description("Represents a specific rotation of a shape. Can be defined by a vector and an angle of rotation around that vector." +
                        " The angle is in degrees by default, but can be specified as radians. The vector is normalized automatically." +
                        " The rotation is a quaternion, so it can also be defined by a w, x, y, and z component.")
                .parser(new Parser<>() {

                    @SuppressWarnings("NullableProblems")
                    @Nullable
                    @Override
                    public Quaternion parse(String s, ParseContext context) {
                        return null;
                    }

                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }

                    @Override
                    public @NotNull String toString(Quaternion quaternion, int flags) {
                        return "rotation of vector " + quaternion.getAxis() + " and angle " + quaternion.getAngle();
                    }

                    @Override
                    public @NotNull String toVariableNameString(Quaternion particle) {
                        return "particle:" + toString(particle, 0);
                    }
                }));
    }
}
