graph TD
    %% Definitions for styling
    classDef external fill:#f9f,stroke:#333,stroke-width:2px,color:black;
    classDef api fill:#d4e157,stroke:#333,stroke-width:2px,color:black;
    classDef service fill:#4db6ac,stroke:#333,stroke-width:2px,color:white;
    classDef ml fill:#ff7043,stroke:#333,stroke-width:2px,color:white;
    classDef storage fill:#90a4ae,stroke:#333,stroke-width:2px,color:black;

    %% External Client
    Client([📱 Chatbot / Frontend]):::external -->|1. POST /predict (JSON Data)| API_Gateway

    %% FastAPI Application Boundary
    subgraph "FastAPI Backend Application (Component 2)"
        API_Gateway[Endpoint Router (routes.py)]:::api -->|2. Validate Data| Pydantic[Pydantic Schemas]:::api
        Pydantic -->|3. Validated Object| PredService[Prediction Service (predictor.py)]:::service

        %% Service Logic Workflow
        subgraph "Prediction Service Logic"
            PredService -->|4. Preprocess & Scale Input| ArtifactsLoader
            ArtifactsLoader -->|5. Get Scaler & Encoders| Preprocessor[Data Preprocessor]:::service
            Preprocessor -->|6. Normalized Tensor (1, 1, 12)| ML_Engine
        end

        %% Core AI Model Engine
        subgraph "Core AI Engine (Keras/TensorFlow)"
            ML_Engine[LSTM + Attention Model]:::ml
        end

        ML_Engine -->|7. Raw Probabilities [0.1, 0.8, 0.1]| PostProcessor[Post-Processor]:::service
        PostProcessor -->|8. Final Risk Class & Confidence| API_Gateway
    end

    %% Storage Layer (Artifacts on Disk)
    subgraph "Storage (model_artifacts/)"
        ArtifactsLoader -.->|Load on Startup| SavedModel[risk_model.keras]:::storage
        ArtifactsLoader -.->|Load on Startup| SavedScaler[scaler.pkl]:::storage
        ArtifactsLoader -.->|Load on Startup| SavedEncoders[encoders.pkl]:::storage
    end

    %% Final Response
    API_Gateway -->|9. JSON Response (High Risk)| Client