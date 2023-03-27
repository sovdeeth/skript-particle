package com.sovdee.skriptparticles.elements.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import com.sovdee.skriptparticles.shapes.Shape;
import com.sovdee.skriptparticles.shapes.Sphere;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;

public class ShapeTypes {
    static {
        // Sphere
        Classes.registerClass(new ClassInfo<>(Sphere.class, "particlesphere")
                .user("particlespheres?")
                .name("Sphere")
                .description("Represents a sphere particle shape.")
                .examples("on load:", "\tset {_sphere} to a sphere of radius 2")
                .serializer(new Serializer<>() {

                    @Override
                    public Fields serialize(Sphere sphere) {
                        Fields fields = new Fields();
                        fields.putPrimitive("radius", sphere.getRadius());
                        sphere.serialize(fields);
                        return fields;
                    }

                    @Override
                    public Sphere deserialize(Fields fields) throws StreamCorruptedException {
                        double radius = fields.getPrimitive("radius", Double.class);
                        Sphere sphere = new Sphere(radius);
                        Shape.deserialize(fields, sphere);
                        return sphere;
                    }

                    @Override
                    public void deserialize(Sphere sphere, Fields fields) throws StreamCorruptedException, NotSerializableException {
                        assert false;
                    }

                    @Override
                    public boolean mustSyncDeserialization() {
                        return false;
                    }

                    @Override
                    protected boolean canBeInstantiated() {
                        return false;
                    }

                }));


    }
}
