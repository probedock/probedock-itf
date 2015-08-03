package io.probedock.itf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import io.probedock.client.ProbeRuntimeException;
import io.probedock.client.common.config.Configuration;
import io.probedock.client.common.model.v1.*;
import io.probedock.client.core.connector.Connector;
import io.probedock.client.core.storage.FileStore;
import io.probedock.jee.itf.model.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener to send results to Probe Dock Server
 *
 * @author Laurent Prevost <laurent.prevost@probedock.io>
 */
public class ItfListener extends AbstractItfListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItfListener.class);

    /**
     * Probe Dock JUnit and version
     */
    private static final String PROBE_DOCK_NAME = "ITF";
    private static final String PROBE_DOCK_VERSION = ResourceBundle.getBundle("version").getString("version");

    /**
     * The execution context
     */
    protected Context context;

    /**
     * The test run to publish
     */
    protected TestRun testRun;

    /**
     * Store the list of the tests executed
     */
    private List<TestResult> results = new ArrayList<>();

    public ItfListener() {
        super("Probe Dock ITF Listener");
    }

    @Override
    public void testRunStart() {
        super.testRunStart();

        context = ModelFactory.createContext();
        Probe probe = ModelFactory.createProbe(PROBE_DOCK_NAME, PROBE_DOCK_VERSION);

        testRun = ModelFactory.createTestRun(
            Configuration.getInstance(),
            context,
            probe,
            configuration.getProjectApiId(),
            configuration.getProjectVersion(),
            configuration.getPipeline(),
            configuration.getStage(),
            null,
            null
        );

    }

    @Override
    public void testRunEnd() {
        super.testRunEnd();

        // Ensure there is nothing to do when disabled
        if (configuration.isDisabled()) {
            return;
        }

        if (!results.isEmpty()) {
            try {
                ModelFactory.enrichContext(context);
                long runEndedDate = System.currentTimeMillis();
                testRun.setDuration(runEndedDate - startDate);
                testRun.addTestResults(results);

                publishTestResult();
            } catch (ProbeRuntimeException | IOException e) {
                LOGGER.warn("Could not publish test results", e);
            }
        }
    }

    @Override
    public void testEnd(Description description) {
        super.testEnd(description);

        // Ensure there is nothing to do when disabled
        if (configuration.isDisabled()) {
            return;
        }

        TestResult tr = createTestResult(getFingerprint(description), description, getMethodAnnotation(description), getClassAnnotation(description));

        LOGGER.info(tr.toString());

        results.add(tr);
    }

    /**
     * Publish the test results
     *
     * @throws IOException I/O errors
     */
    private void publishTestResult() throws IOException {
        if (configuration.isPublish() || configuration.isSave()) {
            if (configuration.isSave()) {
                new FileStore(configuration).save(testRun);
            }

            if (configuration.isPublish()) {
                new Connector(configuration).send(testRun);
            }
        }
    }
}