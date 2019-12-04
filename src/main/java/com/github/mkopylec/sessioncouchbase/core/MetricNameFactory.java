package com.github.mkopylec.sessioncouchbase.core;

import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public class MetricNameFactory {

    protected static final String METRIC_NAME_DELIMITER = ".";
    protected static final String METRIC_NAME_PREFIX = "session_couchbase";

    public String create(OperationMetricName operationName) {
        return createMetricName(operationName);
    }

    public String create(OperationMetricName operationName, ResultMetricName resultName) {
        return createMetricName(operationName, resultName);
    }

    private String createMetricName(Enum<?>... names) {
        String name = stream(names)
                .map(n -> n.name().toLowerCase())
                .collect(joining(METRIC_NAME_DELIMITER));
        return join(METRIC_NAME_DELIMITER, METRIC_NAME_PREFIX, name);
    }
}
