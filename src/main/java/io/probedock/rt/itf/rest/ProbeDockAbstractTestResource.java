package io.probedock.rt.itf.rest;

import io.probedock.client.common.config.Configuration;
import io.probedock.jee.itf.TestController;
import io.probedock.jee.itf.filters.DefaultFilter;
import io.probedock.jee.itf.filters.Filter;
import io.probedock.jee.itf.listeners.Listener;
import io.probedock.rt.itf.ItfListener;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Expose the method to start the integration tests
 *
 * @author Laurent Prevost <laurent.prevost@probedock.io>
 */
public abstract class ProbeDockAbstractTestResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProbeDockAbstractTestResource.class);

    protected static final String DEFAULT_CATEGORY = "integration";

    /**
     * Start the test through the integration test controller
     *
     * @param configuration The configuration to launch the test run
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response run(ProbeDockLaunchConfigurationTO configuration) {
        // Logging
        LOGGER.info(configuration.toString());

//        // Parse additional options
//        parseOptions(configuration.getOptions());

        // Retrieve the test controller
        TestController testController = getController();

        Map<String, Listener> listeners = new HashMap<>();
        Map<String, Filter> filters = new HashMap<>();

        ItfListener defaultListener;

        // Configure the listener with the category
        if (configuration.hasCategory()) {
            defaultListener = new ItfListener(configuration.getCategory());
        } else {
            defaultListener = new ItfListener(DEFAULT_CATEGORY);
        }

        // Add more filters
        List<FilterDefinitionTO> augmentedFilters = configuration.getFilters() == null ? new ArrayList<FilterDefinitionTO>() : configuration.getFilters();
        augmentedFilters.addAll(getAdditionalFilters());

        // Configure filters and default listener
        Map<String, Filter> itfFilters = new HashMap<>();
        itfFilters.put("nameFilter", new DefaultFilter(augmentedFilters));
        listeners.put("listener", defaultListener);

        // Add more listeners
        for (Entry<String, Listener> listener : getAdditionalListeners(configuration.hasCategory() ? configuration.getCategory() : DEFAULT_CATEGORY, configuration.getProjectApiId()).entrySet()) {
            listeners.put(listener.getKey(), listener.getValue());
        }

        Long seed = configuration.getSeed();

        // Retrieve seed from configuration
        if (Configuration.getInstance().getGeneratorSeed() != null) {
            seed = Configuration.getInstance().getGeneratorSeed();
        }

        // Generate default seed
        else if (seed == null) {
            seed = System.currentTimeMillis();
        }

        // Run the integration tests
        LOGGER.info("Generator seed: {}", testController.run(filters, listeners, seed));

        return Response.ok().build();
    }

    /**
     * @return Retrieve the integration test controller
     */
    public abstract TestController getController();

    /**
     * @return More filters to add
     */
    public List<FilterDefinitionTO> getAdditionalFilters() {
        return new ArrayList<>();
    }

    /**
     * @param category The category
     * @param projectApiId The project API ID
     * @return More listeners to use
     */
    public Map<String, Listener> getAdditionalListeners(String category, String projectApiId) {
        return new HashMap<>();
    }
}
