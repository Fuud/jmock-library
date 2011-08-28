package org.jmock.internal.matcher;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class MethodMatcher extends TypeSafeMatcher<Method> {
    private Method expectedMethod;

    public MethodMatcher(Method expectedMethod) {
        super(Method.class);
        this.expectedMethod = expectedMethod;
    }

	/**
	 * It's not at all clear to me that the call to
	 * {@link #sameResolvedMethod(Method, Method)} is necessary. By my reading
	 * of the {@link Class#getDeclaredMethod(String, Class[])} documentation
	 * <code>sameSignature</code> and <code>sameResolvedMethod</code> would only
	 * differ in a class with co-variant return types, which cannot be written
	 * directly in Java. They are allowed in the JVM and can be introduced by
	 * "bridge methods" (whatever they are).
	 * 
	 * Similarly, the call to <code>sameSignature</code> is unnecessary for
	 * correctness, but since this method is called for many expectations, and
	 * since the {@link Class#getDeclaredMethod(String, Class...)} call seems
	 * somewhat involved, it may make sense to use it as a filter.
	 * 
	 * @see org.hamcrest.TypeSafeMatcher#matchesSafely(java.lang.Object)
	 */
	@Override
	public boolean matchesSafely(Method m) {
		return sameSignature(expectedMethod, m)
				&& sameResolvedMethod(expectedMethod, m);
	}

	private static boolean sameSignature(Method a, Method b) {
		return a.getName().equals(b.getName())
				&& Arrays.equals(a.getParameterTypes(), b.getParameterTypes());
	}

	private static boolean sameResolvedMethod(Method a, Method b) {
		try {
			return a.getDeclaringClass().getDeclaredMethod(b.getName(),
					b.getParameterTypes()).equals(a);
		} catch (NoSuchMethodException e) {
			return false;
		}
	}

    @Override
    protected void describeMismatchSafely(Method m, Description mismatchDescription) {
        mismatchDescription.appendText("was ").appendText(m.getName());
    }

    public void describeTo(Description description) {
        description.appendText(expectedMethod.getName());
    }

}
