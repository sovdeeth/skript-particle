package com.sovdee.skriptparticle.shapes;

import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilID;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.StreamCorruptedException;

@YggdrasilID("ShapePosition")
public class ShapePosition implements Cloneable {
    private Vector normal;
    private Vector backupNormal;
    private Vector offset;
    private double rotation;

    public ShapePosition() {
        this.normal = new Vector(0, 1, 0);
        this.backupNormal = new Vector(0, 1, 0);
        this.offset = new Vector(0, 0, 0);
        this.rotation = 0;
    }

    public ShapePosition(Vector normal, Vector offsetVector, double rotation) {
        this.normal = normal;
        this.backupNormal = normal.clone();
        this.offset = offsetVector;
        this.rotation = rotation;
    }

    public Vector getNormal() {
        return normal;
    }

    public void setNormal(Vector normal) {
        this.normal = normal;
    }

    public Vector getBackupNormal() {
        return backupNormal;
    }

    public void setBackupNormal(Vector backupNormal) {
        this.backupNormal = backupNormal;
    }

    public void updateBackupNormal() {
        this.backupNormal = normal.clone();
    }

    public Vector getOffsetVector() {
        return offset;
    }

    public void setOffsetVector(Vector offsetVector) {
        this.offset = offsetVector;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public ShapePosition clone() {
        return new ShapePosition(normal.clone(), offset.clone(), rotation);
    }

    public void serialize(Fields fields) {
        fields.putObject("normal", normal);
        fields.putObject("offset", offset);
        fields.putPrimitive("rotation", rotation);
    }

    public static ShapePosition deserialize(@NonNull Fields fields) throws StreamCorruptedException {
        return new ShapePosition(fields.getObject("normal", Vector.class), fields.getObject("offset", Vector.class), fields.getPrimitive("rotation", double.class));
    }


}
