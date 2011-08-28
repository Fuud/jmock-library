package org.jmock.test.acceptance;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import org.jmock.ExpectationsExt;
import org.jmock.api.Imposteriser;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.internal.matcher.MethodMatcher;
import org.jmock.lib.JavaReflectionImposteriser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class MethodMatcherAcceptanceTest {
    @Rule
    public final JUnitRuleMockery mockery = new JUnitRuleMockery();
    private final Imposteriser imposteriser;

    @Parameterized.Parameters
    public static Collection data() {
        Object[][] data = new Object[][]{
                {new JavaReflectionImposteriser()},
                {ClassImposteriser.INSTANCE}};
        return Arrays.asList(data);
    }

    public MethodMatcherAcceptanceTest(Imposteriser imposteriser) {
        this.imposteriser = imposteriser;
        mockery.setImposteriser(imposteriser);
    }

    @Test
    public void testOverridenMethodExpectations() throws Exception {

        final Circle circle = mockery.mock(Circle.class);
        final CircleHolder holder = mockery.mock(CircleHolder.class);

        mockery.checking(new ExpectationsExt() {
            @Override
            protected void expect() throws Exception {
                oneOf(holder).getShape();
                will(returnValue(circle));

                oneOf(circle).getName();
                will(returnValue("mocked circle"));
            }
        });

        Thing thing = new Thing(holder);
        Assert.assertEquals("mocked circle", thing.getShapeName());
    }

    @Test
    public void methodResolutionCharacterizationTest() throws Exception {
        final Circle circleA = mockery.mock(Circle.class, "circleA");
        final Circle circleB = mockery.mock(Circle.class, "circleB");
        final Shape shape = mockery.mock(Shape.class);

        final CircleHolderImpl circleHolder = new CircleHolderImpl();
        final Holder alias = circleHolder;

        circleHolder.setShape(circleA);
        circleHolder.setShape(shape);
        alias.setShape(circleB);

        assertSame(circleA, circleHolder.circle);
        assertSame(circleB, circleHolder.shape);
        assertSame(circleA, circleHolder.getShape());
        assertSame(circleA, alias.getShape());
    }

    @Test
    public void getMethodCharacterizationTest() throws Exception {
        Method shapeGetShape = getMethod(Holder.class, "getShape");
        Method circleGetShape = getMethod(CircleHolder.class, "getShape");

        Method shapeSetShapeWithCircle = getMethod(Holder.class, "setShape",
                Circle.class);
        Method shapeSetShapeWithShape = getMethod(Holder.class, "setShape",
                Shape.class);
        Method circleSetShapeWithCircle = getMethod(CircleHolder.class,
                "setShape", Circle.class);
        Method circleSetShapeWithShape = getMethod(CircleHolder.class,
                "setShape", Shape.class);

        assertFalse(shapeGetShape.equals(circleGetShape));
        Assert.assertEquals(shapeGetShape.getName(), circleGetShape.getName());
        assertArrayEquals(shapeGetShape.getTypeParameters(), circleGetShape
                .getTypeParameters());

        assertNull(shapeSetShapeWithCircle);
        assertNotNull(shapeSetShapeWithShape);
        Assert.assertEquals(shapeSetShapeWithShape, circleSetShapeWithShape);
        assertFalse(circleSetShapeWithCircle.equals(circleSetShapeWithShape));
    }

    @Test
    public void testGetShapeMatchesSafely() throws Exception {
        Method shapeGetShape = getMethod(Holder.class, "getShape");
        Method circleGetShape = getMethod(CircleHolder.class, "getShape");

        assertTrue(new MethodMatcher(circleGetShape)
                .matchesSafely(circleGetShape));
        assertTrue(new MethodMatcher(circleGetShape)
                .matchesSafely(shapeGetShape));

    }

    @Test
    public void testAccessToProtectedMethods() throws Exception {
        if (!imposteriser.canImposterise(CircleHolderImpl.class)){//
            return;
        }
        final Shape shape = mockery.mock(Shape.class);
        final CircleHolderImpl holder = mockery.mock(CircleHolderImpl.class);
        mockery.checking(new ExpectationsExt() {
            @Override
            protected void expect() throws Exception {
                oneOf(holder).shapeLogger(shape);
            }
        });
        holder.shapeLogger(shape);
    }

    @Test
    public void testSetShapeMatchesSafely() throws Exception {
        Method shapeSetShapeWithShape = getMethod(Holder.class, "setShape",
                Shape.class);
        Method circleSetShapeWithCircle = getMethod(CircleHolder.class,
                "setShape", Circle.class);
        Method circleSetShapeWithShape = getMethod(CircleHolder.class,
                "setShape", Shape.class);

        assertTrue(new MethodMatcher(shapeSetShapeWithShape)
                .matchesSafely(shapeSetShapeWithShape));
        assertTrue(new MethodMatcher(circleSetShapeWithShape)
                .matchesSafely(shapeSetShapeWithShape));
        assertFalse(new MethodMatcher(circleSetShapeWithCircle)
                .matchesSafely(shapeSetShapeWithShape));
    }

    Method getMethod(Class<?> theClass, String name, Class<?>... parameterTypes) {
        try {
            return theClass.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

}

interface Shape {
    String getName();
}

interface Circle extends Shape {
}

interface Holder {
    Shape getShape();

    void setShape(Shape shape);
}

interface CircleHolder extends Holder {
    Circle getShape();

    void setShape(Circle shape);
}

class HolderImpl implements Holder {

    Shape lastShape;
    Shape shape;

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        shapeLogger(shape);
        this.shape = shape;
    }

    protected void shapeLogger(Shape shape) {
        lastShape = shape;
    }

}

class CircleHolderImpl extends HolderImpl implements CircleHolder {

    Circle circle;

    public void setShape(Circle circle) {
        this.circle = circle;
    }

    public Circle getShape() {
        return circle;
    }
}

class Thing {
    Holder holder;

    public Thing(Holder holder) {
        this.holder = holder;
    }

    public String getShapeName() {
        return holder.getShape().getName();
    }
}