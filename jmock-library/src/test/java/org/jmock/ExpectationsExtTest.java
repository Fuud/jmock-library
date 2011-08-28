package org.jmock;

import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;


@SuppressWarnings({"RedundantStringConstructorCall", "unchecked"})
public class ExpectationsExtTest {
    @Test
    public void testEqual() throws Exception {
        final String originalValue = "Hello World";
        Matcher matches = ExpectationsExt.equal(originalValue);

        assertThat(originalValue, matches);
        assertThat(new String(originalValue), matches);

        assertThat("Buy-buy", not(matches));
    }

    @Test
    public void testSame() throws Exception {
        final String originalValue = "Hello World";
        Matcher matches = ExpectationsExt.same(originalValue);

        assertThat(originalValue, matches);

        assertThat(new String(originalValue), not(matches));
        assertThat("Buy-buy", not(matches));
    }

    @Test
    public void testAny() throws Exception {
        Matcher matches = ExpectationsExt.any(String.class);

        assertThat("Hello world", matches);
        assertThat(null, matches);

        assertThat(42, not(matches));
    }

    @Test
    public void testANull() throws Exception {
        Matcher matches = ExpectationsExt.aNull(String.class);

        assertThat(null, matches);

        assertThat("Hello world", not(matches));
        assertThat(42, not(matches));
    }

    @Test
    public void testANonNull() throws Exception {
        Matcher matches = ExpectationsExt.aNonNull(String.class);

        assertThat("Hello world", matches);

        assertThat(null, not(matches));
        assertThat(42, not(matches));
    }

    @Test
    public void testAnything() throws Exception {
        Matcher matches = ExpectationsExt.anything();

        assertThat("Hello world", matches);
        assertThat(null, matches);
        assertThat(42, matches);
    }
}
