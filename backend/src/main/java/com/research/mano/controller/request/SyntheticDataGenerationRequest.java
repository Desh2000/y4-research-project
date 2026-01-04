package com.research.mano.controller.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SyntheticDataGenerationRequest {
    private String sourceUserCluster;
    private String generationMethod;
    private String researcherId;
    private String researchPurpose;
    private Integer batchSize = 1;
}
