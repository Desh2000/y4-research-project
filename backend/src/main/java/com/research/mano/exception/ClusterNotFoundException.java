package com.research.mano.exception;

/**
 * Exception thrown when a cluster group is not found
 */
public class ClusterNotFoundException extends ManoException {

    public ClusterNotFoundException(String message) {
        super("CLUSTER_NOT_FOUND", message);
    }

    public ClusterNotFoundException(Long clusterId) {
        super("CLUSTER_NOT_FOUND", "Cluster not found with ID: " + clusterId);
    }

    public ClusterNotFoundException(String field, String value) {
        super("CLUSTER_NOT_FOUND",
                String.format("Cluster not found with %s: %s", field, value));
    }
}