package io.probedock.client.itf;

import io.probedock.client.common.config.Configuration;
import io.probedock.client.core.filters.FilterDefinition;
import io.probedock.client.core.filters.FilterDefinitionImpl;
import io.probedock.jee.itf.TestController;
import io.probedock.jee.itf.filters.Filter;
import io.probedock.jee.itf.listeners.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.List;
import java.util.Map;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.probedock.jee.itf.rest.FilterDefinitionTO;
import io.probedock.jee.itf.rest.LaunchConfigurationTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Expose the method to start the integration tests
 *
 * @author Laurent Prevost <laurent.prevost@probedock.io>
 */
public abstract class ProbeDockAbstractTestResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProbeDockAbstractTestResource.class);

    /**
     * Start the test through the integration test controller
     *
     * @param launchConfiguration The configuration to launch the test run
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response run(LaunchConfigurationTO launchConfiguration) {
        // Retrieve the test controller
        TestController testController = getController();

        // Setup the filters
        Map<String, Filter> itfFilters = new HashMap<>();
        List<FilterDefinition> filterDefinitions = getAdditionalFilters();
        if (launchConfiguration.getFilters() != null) {
            for (FilterDefinitionTO fd : launchConfiguration.getFilters()) {
                filterDefinitions.add(new FilterDefinitionImpl(fd.getType(), fd.getText()));
            }
        }
        itfFilters.put("filter", new ItfFilter(filterDefinitions));

        // Setup the listeners
        ItfListener defaultListener = new ItfListener();
        Map<String, Listener> listeners = new HashMap<>();
        listeners.put("listener", defaultListener);
        for (Entry<String, Listener> listener : getAdditionalListeners().entrySet()) {
            listeners.put(listener.getKey(), listener.getValue());
        }

        Long seed = launchConfiguration.getSeed();

        // Retrieve seed from configuration
        if (Configuration.getInstance().getGeneratorSeed() != null) {
            seed = Configuration.getInstance().getGeneratorSeed();
        }

        // Generate default seed
        else if (seed == null) {
            seed = System.currentTimeMillis();
        }

        // Run the integration tests
        LOGGER.info("Generator seed: {}", testController.run(itfFilters, listeners, seed));

        return Response.ok().build();
    }

    /**
     * @return Retrieve the integration test controller
     */
    public abstract TestController getController();

    /**
     * @return More filters to add
     */
    public List<FilterDefinition> getAdditionalFilters() {
        return new ArrayList<>();
    }

    /**
     * @return More listeners to use
     */
    public Map<String, Listener> getAdditionalListeners() {
        return new HashMap<>();
    }
}
