package io.probedock.client.itf;

import io.probedock.client.core.filters.FilterDefinition;
import io.probedock.client.core.filters.FilterUtils;
import io.probedock.jee.itf.filters.Filter;
import io.probedock.jee.itf.model.Description;

import java.util.List;

/**
 * Filter to run only part of the tests
 *
 * @author Laurent Prevost <laurent.prevost@probedock.io>
 */
public class ItfFilter implements Filter {
    /**
     * Define the filters to apply
     */
    private List<FilterDefinition> filters;

    public ItfFilter(List<FilterDefinition> filters) {
        this.filters = filters;
    }

    @Override
    public boolean isRunnable(Description description) {
        // The test is deactivated or delegate the filtering to the filter utils
        return description.isRunnable() && FilterUtils.isRunnable(description.getTestClass(), description.getMethod(), filters);
    }
}