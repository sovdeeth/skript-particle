package com.sovdee.skriptparticles.elements.types;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.SimpleJavaFunction;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.DefaultClasses;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import org.jetbrains.annotations.Nullable;

public class RotationTypes {
    static {
        if (Classes.getExactClassInfo(Quaternionf.class) == null) {
            Classes.registerClass(new ClassInfo<>(Quaternionf.class, "quaternion")
                    .user("quaternionf?s?")
                    .name("Quaternion")
                    .description("Quaternions can be used for shape rotations. They're composed of four values, w, x, y, and z. " +
                            "See the Quaternion and AxisAngle functions for ways to create them.")
                    .since("1.0.0")
                    .parser(new Parser<Quaternionf>() {
                        public boolean canParse(ParseContext context) {
                            return false;
                        }

                        @Override
                        public String toString(Quaternionf quaternion, int flags) {
                            return "w:" + Skript.toString(quaternion.w()) + ", x:" + Skript.toString(quaternion.x()) + ", y:" + Skript.toString(quaternion.y()) + ", z:" + Skript.toString(quaternion.z());
                        }

                        @Override
                        public String toVariableNameString(Quaternionf quaternion) {
                            return quaternion.w() + "," + quaternion.x() + "," + quaternion.y() + "," + quaternion.z();
                        }
                    })
                    .defaultExpression(new EventValueExpression<>(Quaternionf.class))
                    .cloner(quaternion -> {
                        try {
                            // Implements cloneable, but doesn't return a Quaternionf.
                            // org.joml improperly override. Returns Object.
                            return (Quaternionf) quaternion.clone();
                        } catch (CloneNotSupportedException e) {
                            return null;
                        }
                    }));

            Functions.registerFunction(new SimpleJavaFunction<>("quaternion", new Parameter[]{
                    new Parameter<>("x", DefaultClasses.NUMBER, true, new SimpleLiteral<Number>(1, true)),
                    new Parameter<>("y", DefaultClasses.NUMBER, true, new SimpleLiteral<Number>(0, true)),
                    new Parameter<>("z", DefaultClasses.NUMBER, true, new SimpleLiteral<Number>(0, true)),
                    new Parameter<>("w", DefaultClasses.NUMBER, true, new SimpleLiteral<Number>(0, true))
            }, Classes.getExactClassInfo(Quaternionf.class), true) {
                @SuppressWarnings("NullableProblems")
                @Override
                public @Nullable Quaternion[] executeSimple(Object[][] params) {
                    float w = ((Number) params[0][0]).floatValue();
                    float x = ((Number) params[1][0]).floatValue();
                    float y = ((Number) params[2][0]).floatValue();
                    float z = ((Number) params[3][0]).floatValue();
                    return new Quaternion[]{new Quaternion(w, x, y, z)};
                }
            }
                    .description("Returns a quaternion from the given w, x, y and z parameters.")
                    .examples("set {_v} to quaternion(1,0,0,0)")
                    .since("1.0.0"));

            Functions.registerFunction(new SimpleJavaFunction<>("axisAngle", new Parameter[]{
                    new Parameter<>("axis", DefaultClasses.VECTOR, true, null),
                    new Parameter<>("angle", DefaultClasses.NUMBER, true, null)
            }, Classes.getExactClassInfo(Quaternionf.class), true) {
                @SuppressWarnings("NullableProblems")
                @Override
                public @Nullable Quaternionf[] executeSimple(Object[][] params) {
                    Vector vector = ((Vector) params[0][0]);
                    float angle = ((Number) params[1][0]).floatValue() * (float) Math.PI / 180;
                    AxisAngle4f axisAngle4f = new AxisAngle4f(angle, (float) vector.getX(), (float) vector.getY(), (float) vector.getZ());
                    return new Quaternionf[]{new Quaternionf(axisAngle4f)};
                }
            }
                    .description("Returns a quaternion from the given axis and angle parameters. The axis is a vector, and the angle is the rotation around that axis, in degrees.")
                    .examples("set {_v} to axisAngle(0.25,0,0,1)")
                    .since("1.0.0"));
        }
    }
}
