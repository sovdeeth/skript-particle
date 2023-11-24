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
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.skriptlang.skript.lang.converter.Converters;

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
        }

        Converters.registerConverter(Quaternionf.class, Quaternion.class, Quaternion::new);

        if (Functions.getGlobalSignature("quaternion") == null) {
            Functions.registerFunction(new SimpleJavaFunction<>("quaternion", new Parameter[]{
                    new Parameter<>("x", DefaultClasses.NUMBER, true, new SimpleLiteral<Number>(0, true)),
                    new Parameter<>("y", DefaultClasses.NUMBER, true, new SimpleLiteral<Number>(0, true)),
                    new Parameter<>("z", DefaultClasses.NUMBER, true, new SimpleLiteral<Number>(0, true)),
                    new Parameter<>("w", DefaultClasses.NUMBER, true, new SimpleLiteral<Number>(1, true))
            }, Classes.getExactClassInfo(Quaternionf.class), true) {
                @Override
                public @Nullable Quaternionf[] executeSimple(Object[][] params) {
                    float w = ((Number) params[0][0]).floatValue();
                    float x = ((Number) params[1][0]).floatValue();
                    float y = ((Number) params[2][0]).floatValue();
                    float z = ((Number) params[3][0]).floatValue();
                    return new Quaternionf[]{new Quaternionf(x, y, z, w)};
                }
            }
                    .description("Returns a quaternion from the given x, y, z and w parameters.")
                    .examples("set {_v} to quaternion(0,0,0,1)")
                    .since("1.0.0"));
        }

        if (Functions.getGlobalSignature("axisAngle") == null) {
            Functions.registerFunction(new SimpleJavaFunction<>("axisAngle", new Parameter[]{
                    new Parameter<>("angle", DefaultClasses.NUMBER, true, null),
                    new Parameter<>("x", DefaultClasses.NUMBER, true, null),
                    new Parameter<>("y", DefaultClasses.NUMBER, true, null),
                    new Parameter<>("z", DefaultClasses.NUMBER, true, null)
            }, Classes.getExactClassInfo(Quaternionf.class), true) {
                @Override
                public @Nullable Quaternionf[] executeSimple(Object[][] params) {
                    float angle = ((Number) params[0][0]).floatValue();
                    float x = ((Number) params[1][0]).floatValue();
                    float y = ((Number) params[2][0]).floatValue();
                    float z = ((Number) params[3][0]).floatValue();
                    AxisAngle4f axisAngle4f = new AxisAngle4f(angle, x, y, z);
                    return new Quaternionf[]{new Quaternionf(axisAngle4f)};
                }
            }
                    .description("Returns a quaternion from the given axis and angle parameters. The axis is a vector composed of 3 numbers, x, y, and z, and the angle is the rotation around that axis, in radians.")
                    .examples("set {_v} to axisAngle(3.14, 1, 0, 0)")
                    .since("1.0.0"));
        }

        if (Functions.getGlobalSignature("axisAngleDegrees") == null) {
            Functions.registerFunction(new SimpleJavaFunction<>("axisAngleDegrees", new Parameter[]{
                    new Parameter<>("angle", DefaultClasses.NUMBER, true, null),
                    new Parameter<>("x", DefaultClasses.NUMBER, true, null),
                    new Parameter<>("y", DefaultClasses.NUMBER, true, null),
                    new Parameter<>("z", DefaultClasses.NUMBER, true, null)
            }, Classes.getExactClassInfo(Quaternionf.class), true) {
                @Override
                public @Nullable Quaternionf[] executeSimple(Object[][] params) {
                    float angle = ((Number) params[0][0]).floatValue() * (float) Math.PI / 180;
                    float x = ((Number) params[1][0]).floatValue();
                    float y = ((Number) params[2][0]).floatValue();
                    float z = ((Number) params[3][0]).floatValue();
                    AxisAngle4f axisAngle4f = new AxisAngle4f(angle, x, y, z);
                    return new Quaternionf[]{new Quaternionf(axisAngle4f)};
                }
            }
                    .description("Returns a quaternion from the given axis and angle parameters. The axis is a vector composed of 3 numbers, x, y, and z, and the angle is the rotation around that axis, in degrees.")
                    .examples("set {_v} to axisAngleDegrees(180, 1, 0, 0)")
                    .since("1.0.0"));
        }
    }
}
