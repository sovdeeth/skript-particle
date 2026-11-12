package com.sovdee.skriptparticles.skript.shapes;

import ch.njol.skript.classes.Changer.ChangeMode;
import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.shapes.shapes.Shape;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

/**
 * Handlers for Skript's common type properties, so Skript's own {@code style of} and {@code scale of}
 * work for shapes without competing with other types' versions of those expressions.
 */
final class ShapeProperties {

    private ShapeProperties() {}

    static final String STYLE_DESCRIPTION = """
            How a shape is drawn: as an outline (wireframe), as a surface (hollow), or filled (solid).
            See the shape style type for the full list of names.""";

    static final String SCALE_DESCRIPTION = """
            How much a shape is scaled up or down around its center. Scaling does not change the number of particles, \
            so a scaled-up shape looks sparser. A negative scale flips the shape inside out. \
            Deleting it sets it back to 1.""";

    static final class StyleHandler implements ExpressionPropertyHandler<Shape, SamplingStyle> {

        @Override
        public @Nullable SamplingStyle convert(Shape shape) {
            return shape.getPointSampler().getStyle();
        }

        @Override
        public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
            return mode == ChangeMode.SET ? new Class[]{SamplingStyle.class} : null;
        }

        @Override
        public void change(Shape shape, Object @Nullable [] delta, ChangeMode mode) {
            if (mode == ChangeMode.SET && delta != null && delta.length > 0)
                shape.getPointSampler().setStyle((SamplingStyle) delta[0]);
        }

        @Override
        public Class<SamplingStyle> returnType() {
            return SamplingStyle.class;
        }
    }

    static final class ScaleHandler implements ExpressionPropertyHandler<Shape, Number> {

        @Override
        public Number convert(Shape shape) {
            return shape.getScale();
        }

        @Override
        public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
            return switch (mode) {
                case SET, ADD, REMOVE, RESET, DELETE -> new Class[]{Number.class};
                case REMOVE_ALL -> null;
            };
        }

        @Override
        public void change(Shape shape, Object @Nullable [] delta, ChangeMode mode) {
            double value = delta != null && delta.length > 0 ? ((Number) delta[0]).doubleValue() : 1;
            switch (mode) {
                case SET -> shape.setScale(value);
                case ADD -> shape.setScale(shape.getScale() + value);
                case REMOVE -> shape.setScale(shape.getScale() - value);
                case RESET, DELETE -> shape.setScale(1);
                default -> {}
            }
        }

        @Override
        public Class<Number> returnType() {
            return Number.class;
        }
    }
}
