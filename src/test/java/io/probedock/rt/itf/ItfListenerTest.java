package io.probedock.rt.itf;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import io.probedock.client.annotations.ProbeTest;
import io.probedock.client.common.model.v1.TestResult;
import io.probedock.jee.itf.model.Description;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.junit.Before;
import org.junit.Test;

import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * @author Laurent Prevost <laurent.prevost@probedock.io>
 */
public class ItfListenerTest {
    @Mock
    private Logger mockLogger = LoggerFactory.getLogger(ItfListener.class);

    private ItfListener listener;
    private List<TestResult> results;


    static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        // remove final modifier from field
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }

    @Before
    public void createDescription() {
        MockitoAnnotations.initMocks(this);

        listener = new ItfListener();
        results = new ArrayList<>();
        Whitebox.setInternalState(listener, "results", results);

        try {
            setFinalStatic(ItfListener.class.getDeclaredField("LOGGER"), mockLogger);
        } catch (Exception e) {
        }
    }

    /**
     * This method is never run. It is used only to create the description object to test the Filter that allows to
     * run test by key, tag, ticket or name.
     */
    @ProbeTest(key = "dummyKey", tags = "dummyTag", tickets = "dummyTicket")
    @io.probedock.jee.itf.annotations.Test
    public Description dummyMethod(Description description) {
        return description.pass();
    }

    /**
     * This method is never run. It is used only to create the description object to test the Filter that allows to
     * run test by key, tag, ticket or name.
     */
    @io.probedock.jee.itf.annotations.Test
    public Description dummyMethodWithoutAnnotation(Description description) {
        return description.pass();
    }

    @Test
    @ProbeTest(key = "6eb18c16a7ce")
    public void theTestListenerShouldContainOneResultAfterOneTestNotification() {
        Description description = null;

        try {
            Method m = ItfListenerTest.class.getMethod("dummyMethod", Description.class);

            io.probedock.jee.itf.annotations.Test a =
                m.getAnnotation(io.probedock.jee.itf.annotations.Test.class);

            description = new Description("groupName", a, ItfListenerTest.class, m);
        } catch (NoSuchMethodException | SecurityException nme) {
        }

        listener.testEnd(description.pass());

        assertEquals("The listener does not contain any result where it should", results.size(), 1);
    }

    @Test
    @ProbeTest(key = "3f51abb8ece5")
    public void whenDataIsAddedToDescriptionItShouldAppearInTheTestResult() {
        Description description = null;
        results.clear();

        try {
            Method m = ItfListenerTest.class.getMethod("dummyMethod", Description.class);

            io.probedock.jee.itf.annotations.Test a =
                m.getAnnotation(io.probedock.jee.itf.annotations.Test.class);

            description = new Description("groupName", a, ItfListenerTest.class, m);
            description.addData("test", "Test");
        } catch (NoSuchMethodException | SecurityException nme) {
        }

        listener.testEnd(description.pass());

        assertEquals("The listener does not contain any result where it should", 1, results.size());
        assertEquals("The data should contains five elements", 5, results.get(0).getData().size());
        assertNotNull("The test in the results should contain the custom data", results.get(0).getData().get("test"));
    }
}
