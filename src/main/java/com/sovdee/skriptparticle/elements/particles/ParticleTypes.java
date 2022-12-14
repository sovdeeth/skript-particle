package com.sovdee.skriptparticle.elements.particles;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.SimpleJavaFunction;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.registrations.DefaultClasses;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Timespan;
import com.destroystokyo.paper.ParticleBuilder;
import com.sovdee.skriptparticle.util.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Vibration;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

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
                    .description("Represents a particle which can be used in the 'Particle Spawn' effect.",
                            "Some particles require extra data, these are distinguished by their data type within the square brackets.",
                            "DustOption, DustTransition and Vibration each have their own functions to build the appropriate data for these particles.")
                    .usage(ParticleUtil.getNamesAsString())
                    .examples("play 1 of soul at location of player",
                            "play 10 of dust using dustOption(green, 10) at location of player",
                            "play 3 of item using player's tool at location of player",
                            "play 1 of block using dirt at location of player",
                            "play 1 of dust_color_transition using dustTransition(blue, green, 3) at location of player",
                            "play 1 of vibration using vibration({loc1}, {loc2}, 1 second) at {loc1}")
                    .since("1.9.0")
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
        if (Classes.getExactClassInfo(ParticleBuilder.class) == null) {
            Classes.registerClass(new ClassInfo<>(ParticleBuilder.class, "customparticle")
                    .user("customparticles?")
                    .name("Custom Particle")
                    .description("Represents a particle with extra data, including offset, count, data, and more.")
                    .parser(new Parser<>() {

                        @SuppressWarnings("NullableProblems")
                        @Nullable
                        @Override
                        public ParticleBuilder parse(String s, ParseContext context) {
                            Particle p = ParticleUtil.parse(s.replace(" ", "_"));
                            if (p == null)
                                return null;
                            return new ParticleBuilder(p).count(1).extra(0);
                        }

                        @Override
                        public @NotNull String toString(ParticleBuilder particle, int flags) {
                            return "" + ParticleUtil.getName(particle.particle());
                        }

                        @Override
                        public @NotNull String toVariableNameString(ParticleBuilder particle) {
                            return "particle:" + toString(particle, 0);
                        }
                    })
            );
        }

        Converters.registerConverter(ParticleBuilder.class, Particle.class, ParticleBuilder::particle);
        Converters.registerConverter(Particle.class, ParticleBuilder.class, ParticleBuilder::new);

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
            }.description("Creates a new dust option to be used with 'dust' particle. Color can either be a regular color or an RGB color using",
                            "Skript's rgb() function. Size is the size the particle will be.")
                    .examples("set {_c} to dustOption(red, 1.5)", "set {_c} to dustOption(rgb(1, 255, 1), 3)")
                    .since("1.9.0"));
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
            }.description("Creates a new dust transition to be used with 'dust_color_transition' particle.",
                            "Color can either be a regular color or an RGB color using Skript's rgb() function.",
                            "Size is the size the particle will be. Requires MC 1.17+")
                    .examples("set {_d} to dustTransition(red, green, 10)", "set {_d} to dustTransition(blue, rgb(1,1,1), 5)")
                    .since("1.11.1"));
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
                    Location destination = (Location) params[0][0];
                    int arrivalTime = (int) ((Timespan) params[1][0]).getTicks_i();
                    Vibration vibration = new Vibration( new Vibration.Destination.BlockDestination(destination), arrivalTime);
                    return new Vibration[]{vibration};
                }
            }.description("Creates a new vibration to be used with 'vibration' particle.",
                            "TO = the destination location the particle will travel to.",
                            "ARRIVAL TIME = the time it will take to arrive at the destination location. Requires MC 1.17+")
                    .examples("set {_v} to vibration({loc1}, 10 seconds)")
                    .since("1.11.1"));
        }











    }
}
