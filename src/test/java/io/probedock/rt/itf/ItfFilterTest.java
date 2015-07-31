package io.probedock.rt.itf;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.probedock.client.annotations.ProbeTest;
import io.probedock.client.core.filters.FilterDefinition;
import io.probedock.client.core.filters.FilterDefinitionImpl;
import io.probedock.jee.itf.model.Description;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Laurent Prevost <laurent.prevost@probedock.io>
 */
public class ItfFilterTest {

    Description description;

    @Before
    public void createDescription() {
        try {
            Method m = ItfFilterTest.class.getMethod("dummyMethod", Description.class);

            io.probedock.jee.itf.annotations.Test a =
                m.getAnnotation(io.probedock.jee.itf.annotations.Test.class);

            description = new Description("groupName", a, ItfFilterTest.class, m);
        } catch (NoSuchMethodException | SecurityException nme) {
        }
    }

    /**
     * This method is never run. It is used only to create the description object to test the rox Filter that allows to
     * run test by key, tag, ticket or name.
     */
    @ProbeTest(key = "dummyKey", tags = "dummyTag", tickets = "dummyTicket")
    @io.probedock.jee.itf.annotations.Test
    public Description dummyMethod(Description description) {
        return description;
    }

    private List<FilterDefinition> createOneElementList(String type, String text) {
        return new ArrayList<>(Arrays.asList(new FilterDefinition[] { new FilterDefinitionImpl(type, text) }));
    }

    @Test
    @ProbeTest(key = "e2454cc2ae17")
    public void descriptionShouldBeRunnableWhenNoFilterIsSpecified() {
        ItfFilter rf = new ItfFilter(null);
        assertTrue("The test is not runnable when it must be", rf.isRunnable(description));
    }

    @Test
    @ProbeTest(key = "e7109fbc50c8")
    public void descriptionShouldBeRunnableWhenValidKeyIsSpecified() {
        ItfFilter rf = new ItfFilter(createOneElementList("key", "dummyKey"));
        assertTrue("The test is not runnable when it must be", rf.isRunnable(description));
    }

    @Test
    @ProbeTest(key = "4d8120dd710f")
    public void descriptionShouldNotBeRunnableWhenInvalidKeyIsSpecified() {
        ItfFilter rf = new ItfFilter(createOneElementList("key", "noKey"));
        assertFalse("The test is runnable when it should not", rf.isRunnable(description));
    }

    @Test
    @ProbeTest(key = "b8d91204cf3a")
    public void descriptionShouldBeRunnableWhenValidNameIsSpecified() {
        ItfFilter rf = new ItfFilter(createOneElementList("name", "dummyMethod"));
        assertTrue("The test is not runnable when it must be", rf.isRunnable(description));
    }

    @Test
    @ProbeTest(key = "aa1dd5da0982")
    public void descriptionShouldNotBeRunnableWhenInvalidNameIsSpecified() {
        ItfFilter rf = new ItfFilter(createOneElementList("name", "noMethod"));
        assertFalse("The test is runnable when it should not", rf.isRunnable(description));
    }

    @Test
    @ProbeTest(key = "93aa5a8efb13")
    public void descriptionShouldBeRunnableWhenValidTagIsSpecified() {
        ItfFilter rf = new ItfFilter(createOneElementList("tag", "dummyTag"));
        assertTrue("The test is not runnable when it must be", rf.isRunnable(description));
    }

    @Test
    @ProbeTest(key = "22f05a5eb161")
    public void descriptionShouldNotBeRunnableWhenInvalidTagIsSpecified() {
        ItfFilter rf = new ItfFilter(createOneElementList("tag", "noTag"));
        assertFalse("The test is runnable when it should not", rf.isRunnable(description));
    }

    @Test
    @ProbeTest(key = "0c9e59a9df76")
    public void descriptionShouldBeRunnableWhenValidTicketIsSpecified() {
        ItfFilter rf = new ItfFilter(createOneElementList("ticket", "dummyTicket"));
        assertTrue("The test is not runnable when it must be", rf.isRunnable(description));
    }

    @Test
    @ProbeTest(key = "fcd1200ff07d")
    public void descriptionShouldNotBeRunnableWhenInvalidTicketIsSpecified() {
        ItfFilter rf = new ItfFilter(createOneElementList("name", "noTicket"));
        assertFalse("The test is runnable when it should not", rf.isRunnable(description));
    }

    @Test
    @ProbeTest(key = "0f860a46a204")
    public void descriptionShouldBeRunnableWhenValidGenericFilterIsSpecified() {
        ItfFilter rf = new ItfFilter(createOneElementList(null, "dummyMethod"));
        assertTrue("The test is not runnable when it must be", rf.isRunnable(description));
    }

    @Test
    @ProbeTest(key = "267c2c16d511")
    public void descriptionShouldNotBeRunnableWhenInvalidGenericFilterIsSpecified() {
        ItfFilter rf = new ItfFilter(createOneElementList(null, "noMethod"));
        assertFalse("The test is runnable when it should not", rf.isRunnable(description));
    }
}