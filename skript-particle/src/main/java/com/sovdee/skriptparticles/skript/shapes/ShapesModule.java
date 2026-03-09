package com.sovdee.skriptparticles.skript.shapes;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.SimpleJavaFunction;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.DefaultClasses;
import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.shapes.shapes.CutoffShape;
import com.sovdee.shapes.shapes.LWHShape;
import com.sovdee.shapes.shapes.PolyShape;
import com.sovdee.shapes.shapes.RadialShape;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.skriptparticles.skript.shapes.expressions.ExprCurrentShape;
import com.sovdee.skriptparticles.skript.shapes.expressions.ExprRotation;
import com.sovdee.skriptparticles.skript.shapes.expressions.ExprShapeCopy;
import com.sovdee.skriptparticles.rendering.Particle;
import com.sovdee.skriptparticles.skript.shapes.constructors.ExprArc;
import com.sovdee.skriptparticles.skript.shapes.constructors.ExprBezierCurve;
import com.sovdee.skriptparticles.skript.shapes.constructors.ExprCircle;
import com.sovdee.skriptparticles.skript.shapes.constructors.ExprCuboid;
import com.sovdee.skriptparticles.skript.shapes.constructors.ExprEllipse;
import com.sovdee.skriptparticles.skript.shapes.constructors.ExprEllipsoid;
import com.sovdee.skriptparticles.skript.shapes.constructors.ExprEllipticalArc;
import com.sovdee.skriptparticles.skript.shapes.constructors.ExprHeart;
import com.sovdee.skriptparticles.skript.shapes.constructors.ExprHelix;
import com.sovdee.skriptparticles.skript.shapes.constructors.ExprIrregularPolygon;
import com.sovdee.skriptparticles.skript.shapes.constructors.ExprLine;
import com.sovdee.skriptparticles.skript.shapes.constructors.ExprRectangle;
import com.sovdee.skriptparticles.skript.shapes.constructors.ExprRegularPolygon;
import com.sovdee.skriptparticles.skript.shapes.constructors.ExprRegularPolyhedron;
import com.sovdee.skriptparticles.skript.shapes.constructors.ExprSphere;
import com.sovdee.skriptparticles.skript.shapes.constructors.ExprSphericalCap;
import com.sovdee.skriptparticles.skript.shapes.constructors.ExprStar;
import com.sovdee.skriptparticles.skript.shapes.effects.EffRotateShape;
import com.sovdee.skriptparticles.skript.shapes.effects.EffToggleAxes;
import com.sovdee.skriptparticles.skript.shapes.properties.ExprHelixWindingRate;
import com.sovdee.skriptparticles.skript.shapes.properties.ExprShapeCutoffAngle;
import com.sovdee.skriptparticles.skript.shapes.properties.ExprShapeLWH;
import com.sovdee.skriptparticles.skript.shapes.properties.ExprShapeLocations;
import com.sovdee.skriptparticles.skript.shapes.properties.ExprShapeModifiers;
import com.sovdee.skriptparticles.skript.shapes.properties.ExprShapeNormal;
import com.sovdee.skriptparticles.skript.shapes.properties.ExprShapeOffset;
import com.sovdee.skriptparticles.skript.shapes.properties.ExprShapeOrientation;
import com.sovdee.skriptparticles.skript.shapes.properties.ExprShapeParticle;
import com.sovdee.skriptparticles.skript.shapes.properties.ExprShapeParticleDensity;
import com.sovdee.skriptparticles.skript.shapes.properties.ExprShapePoints;
import com.sovdee.skriptparticles.skript.shapes.properties.ExprShapeRadius;
import com.sovdee.skriptparticles.skript.shapes.properties.ExprShapeRelativeAxis;
import com.sovdee.skriptparticles.skript.shapes.properties.ExprShapeScale;
import com.sovdee.skriptparticles.skript.shapes.properties.ExprShapeSideLength;
import com.sovdee.skriptparticles.skript.shapes.properties.ExprShapeSides;
import com.sovdee.skriptparticles.skript.shapes.properties.ExprShapeStyle;
import com.sovdee.skriptparticles.skript.shapes.properties.ExprStarPoints;
import com.sovdee.skriptparticles.skript.shapes.properties.ExprStarRadii;
import com.sovdee.skriptparticles.util.Quaternion;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.lang.converter.Converters;

public class ShapesModule extends HierarchicalAddonModule {

    public ShapesModule(AddonModule parentModule) {
        super(parentModule);
    }

    @Override
    protected void initSelf(SkriptAddon addon) {
        registerShapeTypes();
        registerParticleTypes();
        registerRotationTypes();
    }

    private static void registerShapeTypes() {
        Classes.registerClass(new ClassInfo<>(Shape.class, "shape")
                .user("shapes?")
                .name("Shape")
                .description("Represents an abstract particle shape. E.g. circle, line, etc.")
                .parser(new Parser<>() {
                    @Override
                    public Shape parse(String input, ParseContext context) { return null; }
                    @Override
                    public boolean canParse(ParseContext context) { return false; }
                    @Override
                    public String toString(Shape o, int flags) { return o.toString(); }
                    @Override
                    public String toVariableNameString(Shape shape) { return "shape:" + shape.getPointSampler().getUUID(); }
                })
                .cloner(Shape::clone)
        );

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
                    public boolean canParse(ParseContext context) { return true; }
                    @Override
                    public String toString(SamplingStyle style, int i) { return style.toString(); }
                    @Override
                    public String toVariableNameString(SamplingStyle style) { return "shapestyle:" + style; }
                }));
    }

    private static void registerParticleTypes() {
        Classes.registerClass(new ClassInfo<>(Particle.class, "customparticle")
                .user("customparticles?")
                .name("Custom Particle")
                .description("Represents a particle with extra shape-related data.")
                .parser(new Parser<>() {
                    @Nullable
                    @Override
                    public Particle parse(String s, ParseContext context) { return null; }
                    @Override
                    public boolean canParse(ParseContext context) { return false; }
                    @Override
                    public String toString(Particle particle, int flags) {
                        return particle.toString(ContextlessEvent.get(), false);
                    }
                    @Override
                    public String toVariableNameString(Particle particle) {
                        return "particle:" + toString(particle, 0);
                    }
                })
        );

        Converters.registerConverter(ParticleEffect.class, Particle.class, Particle::of);
    }

    private static void registerRotationTypes() {
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

    @Override
    protected void loadSelf(SkriptAddon addon) {
        register(addon,
                // shape
                ExprCurrentShape::register,
                // shape factory expressions
                ExprCircle::register,
                ExprSphere::register,
                ExprCuboid::register,
                ExprLine::register,
                ExprArc::register,
                ExprEllipse::register,
                ExprEllipsoid::register,
                ExprRegularPolygon::register,
                ExprRegularPolyhedron::register,
                ExprHelix::register,
                ExprBezierCurve::register,
                ExprIrregularPolygon::register,
                ExprRectangle::register,
                ExprHeart::register,
                ExprSphericalCap::register,
                ExprStar::register,
                ExprEllipticalArc::register,
                // misc shape expressions
                ExprShapeCopy::register,
                ExprRotation::register,
                // shape property expressions
                ExprShapeRadius::register,
                ExprShapeParticle::register,
                ExprShapeStyle::register,
                ExprShapeScale::register,
                ExprShapeOffset::register,
                ExprShapeOrientation::register,
                ExprShapeNormal::register,
                ExprShapeParticleDensity::register,
                ExprShapeRelativeAxis::register,
                ExprShapeLWH::register,
                ExprShapeCutoffAngle::register,
                ExprShapeSides::register,
                ExprShapeSideLength::register,
                ExprShapePoints::register,
                ExprShapeLocations::register,
                ExprShapeModifiers::register,
                ExprHelixWindingRate::register,
                ExprStarPoints::register,
                ExprStarRadii::register,
                // shape effects
                EffRotateShape::register,
                EffToggleAxes::register
        );
    }

    @Override
    public String name() {
        return "shapes";
    }

}
