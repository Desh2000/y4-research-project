package com.research.mano.exception;

public class ClusterNotFoundException extends ManoException {
    public ClusterNotFoundException(String clusterIdentifier) {
        super("CLUSTER_NOT_FOUND", "Cluster not found: " + clusterIdentifier);
    }

    public ClusterNotFoundException(Long clusterId) {
        super("CLUSTER_NOT_FOUND", "Cluster not found with ID: " + clusterId);
    }
}
