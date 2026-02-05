package com.forward.direct.debit;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.boot.CommandLineRunner;

/**
 * Camunda Application configured with PostgreSQL database
 * Configuration is externalized in application.properties
 * Implements CommandLineRunner for command-line execution pattern
 */
public class DirectDebitWorkflowApplication implements CommandLineRunner {

    private ProcessEngine processEngine;
    private DatabaseConfig config;

    // Constructor that initializes configuration and process engine
    public DirectDebitWorkflowApplication() {
        this.config = new DatabaseConfig();
        this.processEngine = createProcessEngine(config);
    }

    /**
     * Main method - entry point of the application
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        DirectDebitWorkflowApplication app = new DirectDebitWorkflowApplication();
        try {
            System.out.println("Starting Camunda Application...");
            app.run(args);
            System.out.println("Application completed successfully!");
        } catch (Exception e) {
            System.err.println("Application failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Implementation of CommandLineRunner interface
     * Executes the main application logic
     *
     * @param args Command line arguments
     * @throws Exception if any error occurs during execution
     */
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=".repeat(80));
        System.out.println("Camunda Process Engine created successfully with PostgreSQL!");
        System.out.println("=".repeat(80));

        // Deploy Process
        deployProcess(processEngine, config);
        System.out.println("Process deployed successfully!");

        // Start Process Instance
        startProcessInstance(processEngine, config);
        // Close engine gracefully
        closeEngine(processEngine);
    }

    /**
     * Creates and configures the Camunda Process Engine with PostgreSQL
     *
     * @param config Database configuration
     * @return Configured ProcessEngine instance
     */
    private ProcessEngine createProcessEngine(DatabaseConfig config) {
        try {
            String schemaUpdate = config.isSchemaUpdateEnabled()
                    ? ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE
                    : ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE;

            ProcessEngine engine = ProcessEngineConfiguration
                    .createStandaloneProcessEngineConfiguration()
                    .setDatabaseSchemaUpdate(schemaUpdate)
                    .setJdbcUrl(config.getJdbcUrl())
                    .setJdbcUsername(config.getJdbcUsername())
                    .setJdbcPassword(config.getJdbcPassword())
                    .setJdbcDriver(config.getJdbcDriver())
                    .setJobExecutorActivate(config.isJobExecutorActivateEnabled())
                    // PostgreSQL specific optimizations
                    .setJdbcMaxActiveConnections(config.getMaxPoolSize())
                    .setJdbcMaxIdleConnections(config.getMinPoolSize())
                    .buildProcessEngine();

            System.out.println("Process Engine initialized with:");
            System.out.println("  - Database URL: " + config.getJdbcUrl());
            System.out.println("  - Max Connections: " + config.getMaxPoolSize());
            System.out.println("  - Job Executor Active: " + config.isJobExecutorActivateEnabled());

            return engine;
        } catch (Exception e) {
            System.err.println("Failed to create Process Engine: " + e.getMessage());
            throw new RuntimeException("Process Engine initialization failed", e);
        }
    }

    /**
     * Deploys the BPMN process definition
     *
     * @param processEngine The process engine instance
     * @param config Database configuration
     */
    private void deployProcess(ProcessEngine processEngine, DatabaseConfig config) {
        try {
            String processResource = config.getProcessResource();
            System.out.println("Deploying process from: " + processResource);

            processEngine.getRepositoryService()
                    .createDeployment()
                    .addClasspathResource(processResource)
                    .deploy();

        } catch (Exception e) {
            System.err.println("Failed to deploy process: " + e.getMessage());
            throw new RuntimeException("Process deployment failed", e);
        }
    }

    /**
     * Starts a new process instance
     *
     * @param processEngine The process engine instance
     * @param config Database configuration
     */
    private void startProcessInstance(ProcessEngine processEngine, DatabaseConfig config) {
        try {
            RuntimeService runtimeService = processEngine.getRuntimeService();
            String processKey = config.getProcessDefinitionKey();

            System.out.println("Starting process instance for key: " + processKey);

            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey(processKey);

            System.out.println("-".repeat(80));
            System.out.println("Process Instance Started:");
            System.out.println("  Instance ID: " + processInstance.getId());
            System.out.println("  Process Definition Key: " + processInstance.getProcessDefinitionKey());
            System.out.println("  Process Definition ID: " + processInstance.getProcessDefinitionId());
            System.out.println("  Business Key: " + processInstance.getBusinessKey());
            System.out.println("  Is Ended: " + processInstance.isEnded());
            System.out.println("  Is Suspended: " + processInstance.isSuspended());
            System.out.println("-".repeat(80));

        } catch (Exception e) {
            System.err.println("Failed to start process instance: " + e.getMessage());
            throw new RuntimeException("Process instance start failed", e);
        }
    }

    /**
     * Closes the process engine gracefully
     *
     * @param processEngine The process engine to close
     */
    private void closeEngine(ProcessEngine processEngine) {
        try {
            if (processEngine != null) {
                processEngine.close();
                System.out.println("Process Engine closed successfully!");
            }
        } catch (Exception e) {
            System.err.println("Error while closing Process Engine: " + e.getMessage());
        }
    }

    /**
     * Get the process engine instance
     *
     * @return ProcessEngine instance
     */
    public ProcessEngine getProcessEngine() {
        return processEngine;
    }

    /**
     * Get the configuration
     *
     * @return DatabaseConfig instance
     */
    public DatabaseConfig getConfig() {
        return config;
    }
}
