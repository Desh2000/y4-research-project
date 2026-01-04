package com.research.mano.exception;

public class ClusterNotInitializedException extends ManoException {
    public ClusterNotInitializedException() {
        super("CLUSTER_NOT_INITIALIZED", "Cluster system not properly initialized");
    }
}
