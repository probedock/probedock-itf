package io.probedock.itf;

import io.probedock.client.common.utils.TestResultDataUtils;
import io.probedock.jee.itf.annotations.NoRollback;
import io.probedock.jee.itf.listeners.DefaultListener;
import io.probedock.jee.itf.model.Description;
import io.probedock.client.annotations.ProbeTest;
import io.probedock.client.annotations.ProbeTestClass;
import io.probedock.client.common.config.Configuration;
import io.probedock.client.common.utils.Inflector;
import io.probedock.client.common.model.v1.TestResult;
import io.probedock.client.common.model.v1.ModelFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared code to handle the test results in different related listeners
 *
 * @author Laurent Prevost <laurent.prevost@probedock.io>
 */
public abstract class AbstractItfListener extends DefaultListener {
    /**
     * Configuration
     */
    protected static final Configuration configuration = Configuration.getInstance();

    /**
     * Default category when none is specified
     */
    protected static final String DEFAULT_CATEGORY = "Integration";

    /**
     *
     * @param listenerName
     */
    public AbstractItfListener(String listenerName) {
        super(listenerName);
    }

    /**
     * Try to retrieve the {@link ProbeTest} annotation of the test method
     *
     * @param description The representation of the test
     * @return The annotation found, or null if not found
     */
    protected ProbeTest getMethodAnnotation(Description description) {
        return description.getMethod().getAnnotation(ProbeTest.class);
    }

    /**
     * Try to retrieve the {@link ProbeTestClass} annotation of the test class
     *
     * @param description The representation of the test
     * @return The annotation found, or null if not found
     */
    protected ProbeTestClass getClassAnnotation(Description description) {
        return description.getMethod().getClass().getAnnotation(ProbeTestClass.class);
    }

    /**
     * Create a test based on the different information gathered from class, method and description
     *
     * @param description jUnit test description
     * @param methodAnnotation Method annotation
     * @param classAnnotation Class annotation
     * @return The test created from all the data available
     */
    protected TestResult createTestResult(String fingerprint, Description description, ProbeTest methodAnnotation, ProbeTestClass classAnnotation) {
        Map<String, String> data = new HashMap<>();
        if (description.getData() != null && description.getData().size() > 0) {
            data.putAll(description.getData());
        }

        data.put("package", description.getMethod().getClass().getPackage().getName());
        data.put("class", description.getMethod().getClass().getSimpleName());
        data.put("method", description.getSimpleName());
        data.put("rollback", "" + (description.getMethod().getAnnotation(NoRollback.class) == null));

        return ModelFactory.createTestResult(
            TestResultDataUtils.getKey(methodAnnotation),
            fingerprint,
            Inflector.forgeName(description.getTestClass(), description.getMethod().getName(), methodAnnotation),
            TestResultDataUtils.getCategory(configuration, classAnnotation, methodAnnotation, getCategory()),
            description.getDuration(),
            description.getMessage(),
            description.isPassed(),
            TestResultDataUtils.isActive(methodAnnotation),
            TestResultDataUtils.getContributors(configuration, classAnnotation, methodAnnotation),
            TestResultDataUtils.getTags(configuration, classAnnotation, methodAnnotation),
            TestResultDataUtils.getTickets(configuration, classAnnotation, methodAnnotation),
            data
        );
    }

    /**
     * Retrieve the fingerprint of a test based on its description
     *
     * @param description The description
     * @return The fingerprint
     */
    protected final String getFingerprint(Description description) {
        return TestResultDataUtils.getFingerprint(description.getTestClass(), description.getMethod().getName());
    }

    /**
     * Retrive the category
     *
     * @return The category
     */
    protected final String getCategory() {
        return configuration.getCategory() != null && !configuration.getCategory().isEmpty() ? configuration.getCategory() : DEFAULT_CATEGORY;
    }

}