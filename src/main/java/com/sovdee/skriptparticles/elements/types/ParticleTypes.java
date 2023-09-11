package com.sovdee.skriptparticles.elements.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.SimpleJavaFunction;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.DefaultClasses;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Timespan;
import com.sovdee.skriptparticles.particles.ParticleMotion;
import com.sovdee.skriptparticles.util.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Vibration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;

/*
 * Thanks to ShaneBee at SkBee for the original particle code.
 * This is meant to fill the gap when SkBee isn't installed.
 */
public class ParticleTypes {
    static {
        if (Classes.getExactClassInfo(Particle.class) == null) {
            Classes.registerClass(new ClassInfo<>(Particle.class, "particle")
                    .user("particles?")
                    .name("Particle")
                    .description("A mirror of SkBee's Particle type. For use when SkBee is not installed.",
                            "Represents a particle which can be used as a shape's particle, or in the Draw Particle and Particle Spawn effects.",
                            "Some particles require extra data, these are distinguished by their data type within the square brackets.",
                            "DustOption, DustTransition and Vibration each have their own functions to build the appropriate data for these particles.")
                    .usage(ParticleUtil.getNamesAsString())
                    .examples("draw 1 of soul at location of player",
                            "draw 10 of dust using dustOption(green, 10) at location of player",
                            "draw 3 of item using player's tool at location of player",
                            "draw 1 of block using dirt at location of player",
                            "draw 1 of dust_color_transition using dustTransition(blue, green, 3) at location of player",
                            "draw 1 of vibration using vibration({loc1}, {loc2}, 1 second) at {loc1}")
                    .since("1.0.0")
                    .parser(new Parser<>() {

                        @SuppressWarnings("NullableProblems")
                        @Nullable
                        @Override
                        public Particle parse(String s, ParseContext context) {
                            return ParticleUtil.parse(s.replace(" ", "_"));
                        }

                        @Override
                        public @NotNull String toString(Particle particle, int flags) {
                            return "" + ParticleUtil.getName(particle);
                        }

                        @Override
                        public @NotNull String toVariableNameString(Particle particle) {
                            return "particle:" + toString(particle, 0);
                        }
                    }));
        }

        // Particle Builder class
        Classes.registerClass(new ClassInfo<>(com.sovdee.skriptparticles.particles.Particle.class, "customparticle")
                .user("customparticles?")
                .name("Custom Particle")
                .description("Represents a particle with extra data, including offset, count, data, and more.")
                .parser(new Parser<>() {

                    @SuppressWarnings("NullableProblems")
                    @Nullable
                    @Override
                    public com.sovdee.skriptparticles.particles.Particle parse(String s, ParseContext context) {
                        return null;
                    }

                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }

                    @Override
                    public @NotNull String toString(com.sovdee.skriptparticles.particles.Particle particle, int flags) {
                        return "" + ParticleUtil.getName(particle.particle());
                    }

                    @Override
                    public @NotNull String toVariableNameString(com.sovdee.skriptparticles.particles.Particle particle) {
                        return "particle:" + toString(particle, 0);
                    }
                })
        );

        // Particle motion class
        Classes.registerClass(new ClassInfo<>(ParticleMotion.class, "particlemotion")
                .user("particle ?motions?")
                .name("Particle Motion")
                .description("Represents the motion of a particle relative to a shape, ie: inwards, outwards, clockwise, etc.")
                .parser(new Parser<>() {

                    @SuppressWarnings("NullableProblems")
                    @Nullable
                    @Override
                    public ParticleMotion parse(String s, ParseContext context) {
                        return switch (s.toLowerCase()) {
                            case "inwards motion" -> ParticleMotion.INWARDS;
                            case "outwards motion" -> ParticleMotion.OUTWARDS;
                            case "clockwise motion" -> ParticleMotion.CLOCKWISE;
                            case "counterclockwise motion" -> ParticleMotion.COUNTERCLOCKWISE;
                            case "no motion" -> ParticleMotion.NONE;
                            default -> null;
                        };
                    }

                    @Override
                    public boolean canParse(ParseContext context) {
                        return true;
                    }

                    @Override
                    public @NotNull String toString(ParticleMotion particleMotion, int flags) {
                        return particleMotion.toString();
                    }

                    @Override
                    public @NotNull String toVariableNameString(ParticleMotion particle) {
                        return "particle motion:" + toString(particle, 0);
                    }
                })
        );

        Converters.registerConverter(Particle.class, com.sovdee.skriptparticles.particles.Particle.class, (particle) -> (com.sovdee.skriptparticles.particles.Particle) new com.sovdee.skriptparticles.particles.Particle(particle).count(1).extra(0));
        Converters.registerConverter(com.sovdee.skriptparticles.particles.Particle.class, Particle.class, com.sovdee.skriptparticles.particles.Particle::particle);

        if (Classes.getExactClassInfo(DustOptions.class) == null) {
            Classes.registerClass(new ClassInfo<>(DustOptions.class, "dustoption")
                    .name(ClassInfo.NO_DOC).user("dust ?options?"));
            // Function to create DustOptions
            //noinspection ConstantConditions
            Functions.registerFunction(new SimpleJavaFunction<>("dustOption", new Parameter[]{
                    new Parameter<>("color", DefaultClasses.COLOR, true, null),
                    new Parameter<>("size", DefaultClasses.NUMBER, true, null)
            }, Classes.getExactClassInfo(DustOptions.class), true) {
                @SuppressWarnings("NullableProblems")
                @Override
                public DustOptions[] executeSimple(Object[][] params) {
                    org.bukkit.Color color = ((Color) params[0][0]).asBukkitColor();
                    float size = ((Number) params[1][0]).floatValue();
                    return new DustOptions[]{new DustOptions(color, size)};
                }
            }.description("A mirror of SkBee's dustOption function. For use when SkBee is not installed.\n" +
                                    "Creates a new dust option to be used with 'dust' particle. Color can either be a regular color or an RGB color using",
                            "Skript's rgb() function. Size is the size the particle will be.")
                    .examples("set {_c} to dustOption(red, 1.5)", "set {_c} to dustOption(rgb(1, 255, 1), 3)")
                    .since("1.0.0"));
        }

        if (Classes.getExactClassInfo(DustTransition.class) == null) {
            Classes.registerClass(new ClassInfo<>(DustTransition.class, "dusttransition")
                    .name(ClassInfo.NO_DOC).user("dust ?transitions?"));

            // Function to create DustTransition
            //noinspection ConstantConditions
            Functions.registerFunction(new SimpleJavaFunction<>("dustTransition", new Parameter[]{
                    new Parameter<>("fromColor", DefaultClasses.COLOR, true, null),
                    new Parameter<>("toColor", DefaultClasses.COLOR, true, null),
                    new Parameter<>("size", DefaultClasses.NUMBER, true, null)
            }, Classes.getExactClassInfo(DustTransition.class), true) {
                @SuppressWarnings("NullableProblems")
                @Override
                public DustTransition[] executeSimple(Object[][] params) {
                    org.bukkit.Color fromColor = ((Color) params[0][0]).asBukkitColor();
                    org.bukkit.Color toColor = ((Color) params[1][0]).asBukkitColor();
                    float size = ((Number) params[2][0]).floatValue();
                    return new DustTransition[]{
                            new DustTransition(fromColor, toColor, size)
                    };
                }
            }.description("A mirror of SkBee's dustTransition function. For use when SkBee is not installed.\n" +
                                    "Creates a new dust transition to be used with 'dust_color_transition' particle.",
                            "Color can either be a regular color or an RGB color using Skript's rgb() function.",
                            "Size is the size the particle will be. Requires MC 1.17+")
                    .examples("set {_d} to dustTransition(red, green, 10)", "set {_d} to dustTransition(blue, rgb(1,1,1), 5)")
                    .since("1.0.0"));
        }
        if (Classes.getExactClassInfo(Vibration.class) == null) {
            Classes.registerClass(new ClassInfo<>(Vibration.class, "vibration")
                    .name(ClassInfo.NO_DOC).user("vibrations?"));

            // Function to create Vibration
            //noinspection ConstantConditions
            Functions.registerFunction(new SimpleJavaFunction<>("vibration", new Parameter[]{
                    new Parameter<>("to", DefaultClasses.LOCATION, true, null),
                    new Parameter<>("arrivalTime", DefaultClasses.TIMESPAN, true, null)
            }, Classes.getExactClassInfo(Vibration.class), true) {
                @SuppressWarnings("NullableProblems")
                @Override
                public Vibration[] executeSimple(Object[][] params) {
                    if (params[0].length == 0 || params[1].length == 0) {
                        return null;
                    }
                    Location origin = new Location(null, 0, 0, 0);
                    Location destination = (Location) params[0][0];
                    int arrivalTime = (int) ((Timespan) params[1][0]).getTicks_i();
                    Vibration vibration = new Vibration(origin, new Vibration.Destination.BlockDestination(destination), arrivalTime);
                    return new Vibration[]{vibration};
                }
            }.description("A mirror of SkBee's Vibration function. For use when SkBee is not installed.\n" +
                                    "Creates a new vibration to be used with 'vibration' particle.",
                            "TO = the destination location the particle will travel to.",
                            "ARRIVAL TIME = the time it will take to arrive at the destination location. Requires MC 1.17+")
                    .examples("set {_v} to vibration({loc1}, 10 seconds)")
                    .since("1.0.0"));
        }


    }
}
