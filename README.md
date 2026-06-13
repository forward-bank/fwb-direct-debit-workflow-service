# FWB Direct Debit Workflow Service

A Spring Boot + Camunda BPM 7 workflow engine that orchestrates the end-to-end processing of Direct Debit (pain.008) payment files. Incoming files are validated, checked for duplicates, sent for syntax validation and debulking via IBM MQ, and finally dispatched to the SepaDDO orchestration service.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Workflow Diagram](#workflow-diagram)
3. [Workflow Steps in Detail](#workflow-steps-in-detail)
4. [Project Structure](#project-structure)
5. [Task Registry — tasks.json](#task-registry--tasksjson)
6. [Service Tasks](#service-tasks)
7. [Receive Tasks](#receive-tasks)
8. [How Camunda Processes the Workflow Internally](#how-camunda-processes-the-workflow-internally)
9. [IBM MQ Integration](#ibm-mq-integration)
10. [Spring & Camunda Configuration](#spring--camunda-configuration)
11. [Running the Service](#running-the-service)
12. [Dev Utilities](#dev-utilities)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    FWB Direct Debit Workflow Service                         │
│                                                                             │
│  IBM MQ                    Spring Boot + Camunda BPM 7          External    │
│  ─────────────────────     ───────────────────────────────      ─────────   │
│  FIRST.TEST.QUEUE      ──► MQMessageListener                               │
│                              │ triggers process instance                    │
│                              ▼                                              │
│                        CamundaProcessExecutor                               │
│                        (ThreadPool core=5/max=20)                           │
│                              │                                              │
│                              ▼                                              │
│                        Camunda BPMN Engine ────────────────────────────────►│
│                        direct-debit-process                 PostgreSQL DB   │
│                              │                                              │
│            ┌─────────────────┼──────────────────┐                          │
│            ▼                 ▼                  ▼                           │
│     ServiceTask         ReceiveTask         Gateways                        │
│     (CamundaTaskExecutor)  (MQ Listeners)  (conditions)                    │
│            │                 │                                              │
│            ▼                 ▼                                              │
│     tasks.json lookup   correlate message ──────────────────────────────►  │
│     → TaskExecutor      back to process        SepaDDO Orchestration       │
│     → TaskDefinition                           Service (REST :8081)         │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Workflow Diagram

```
                             ┌─────────────────────────────────────────────────────────────────┐
                             │                   direct-debit-process                          │
                             └─────────────────────────────────────────────────────────────────┘

  ┌──────────┐    ┌────────────────────┐    ┌────────────────────────┐
  │  Start   │───►│ message_validation │───►│  [is_message_valid?]   │
  │ (MQ Msg) │    │       _task        │    │   Exclusive Gateway    │
  └──────────┘    │  (Service Task)    │    └────────────────────────┘
                  └────────────────────┘             │         │
                                                     │ valid   │ invalid
                                                     ▼         ▼
                                          ┌──────────────┐   ┌─────────────────┐
                                          │  customer_   │   │ End (Invalid    │
                                          │ validation   │   │    Message)     │
                                          │   _task      │   └─────────────────┘
                                          │(Service Task)│
                                          └──────────────┘
                                                  │
                                      ┌───────────┴───────────┐
                                      │  [is_customer_valid?] │
                                      │   Exclusive Gateway   │
                                      └───────────────────────┘
                                           │           │
                                      valid│           │invalid
                                           ▼           ▼
                               ┌───────────────┐  ┌─────────────────┐
                               │[is_file_      │  │ End (Invalid    │
                               │ encrypted?]   │  │   Customer)     │
                               │  Gateway      │  └─────────────────┘
                               └───────────────┘
                              not encrypted│    │encrypted
                                           ▼    ▼
                               ┌──────────────┐ ┌──────────────────┐
                               │  syntax_     │ │  End (Encrypted  │
                               │ validation   │ │     File)        │
                               │ _request_    │ └──────────────────┘
                               │   task       │
                               │(Service Task)│
                               │  sends to    │
                               │    MQ ───────┼──► SYNTAX.VALIDATION
                               └──────────────┘    .REQUEST.QUEUE
                                      │
                               ┌──────────────┐
                               │  syntax_     │◄── SYNTAX.VALIDATION
                               │ validation   │    .RESPONSE.QUEUE
                               │ _response_   │    (MQ Correlation)
                               │   task       │
                               │(Receive Task)│
                               └──────────────┘
                                      │
                             ┌────────┴────────┐
                             │ [is_syntax_     │
                             │  valid?]        │
                             │  Gateway        │
                             └────────┬────────┘
                              valid   │    invalid
                                      ▼    ▼
                           ┌────────────────┐  ┌──────────────────┐
                           │ duplicate_     │  │  End (Invalid    │
                           │ check_task     │  │    Syntax)       │
                           │(Service Task)  │  └──────────────────┘
                           └────────────────┘
                                   │
                        ┌──────────┴──────────┐
                        │  [is_file_          │
                        │   duplicate?]       │
                        │    Gateway          │
                        └──────────┬──────────┘
                     not dup.      │       duplicate
                                   ▼       ▼
                        ┌────────────────┐  ┌────────────────────┐
                        │ debulking_     │  │  End (Duplicate    │
                        │ request_task   │  │     File)          │
                        │(Service Task)  │  └────────────────────┘
                        │  sends to  ───────► FILE.PROCESS.SERVICE
                        │    MQ      │        .REQUEST.QUEUE
                        └────────────────┘
                                 │
                        ┌────────────────┐
                        │ debulking_     │◄─── FILE.PROCESS.SERVICE
                        │ response_task  │     .RESPONSE.QUEUE
                        │(Receive Task)  │     (MQ Correlation)
                        └────────────────┘
                                 │
                      ┌──────────┴──────────┐
                      │ [is_debulking_      │
                      │  completed?]        │
                      │   Gateway           │
                      └──────────┬──────────┘
                  completed      │      FSN raised
                                 ▼      ▼
                   ┌──────────────────────┐  ┌────────────────────┐
                   │ sddo_orchestration   │  │  End (FSN during   │
                   │ _dispatching_task    │  │    Debulking)      │
                   │  (Service Task)      │  └────────────────────┘
                   │  calls REST :8081 ───┼──► SepaDDO Orchestration
                   └──────────────────────┘   Service
                                 │
                          ┌─────────────┐
                          │    End      │
                          └─────────────┘
```

**Process variables used as gateway conditions:**

| Variable               | Type    | Set by task                        | Controls gateway                     |
|------------------------|---------|------------------------------------|--------------------------------------|
| `is_message_valid`     | boolean | `MessageValidationTaskDefinition`  | Route after message validation       |
| `is_customer_valid`    | boolean | `CustomerValidationTaskDefinition` | Route after customer validation      |
| `is_file_encrypted`    | boolean | `CustomerValidationTaskDefinition` | Skip to End if file is encrypted     |
| `is_syntax_valid`      | boolean | `SyntaxValidationResponseTaskDef`  | Route after syntax validation result |
| `is_file_duplicate`    | boolean | `DuplicateCheckTaskDefinition`     | Route after duplicate check          |
| `is_debulking_completed`| boolean| `DebulkingResponseTaskDefinition`  | Route after debulking result         |

---

## Workflow Steps in Detail

### 1. Start Event — Incoming MQ Message

A message arrives on `FIRST.TEST.QUEUE`. `MQMessageListener.onMessage()` extracts the JMS metadata and message body into a flat `Map<String, Object>`, then submits a `BusinessProcessExecutionThread` to `CamundaProcessExecutor`. The thread calls `runtimeService.startProcessInstanceByKey("direct-debit-process", businessKey, vars)`, creating a new process instance. The JMS message ID becomes the `businessKey`.

Key process variables set at startup:
- `incomingMessage` — the raw JSON body (an `InputMessage`)
- `jmsMessageId` — used as `correlationId` for downstream MQ calls
- `sourceQueue`, `jmsTimestamp`, `jmsPriority`

---

### 2. message_validation_task (Service Task)

**Executor:** `MessageValidationTaskExecutor` → `MessageValidationTaskDefinition`

Parses the `incomingMessage` variable as an `InputMessage` JSON record:
```json
{
  "fileDataSeq": 1001,
  "channelRef": "REF-001",
  "outputChannelCode": "DIRECT",
  "fileS3Path": "s3://bucket/path/to/file.csv"
}
```
Sets `is_message_valid = true/false` and stores the parsed object in `TRIGGER_MESSAGE`. If parsing fails, throws an exception and sets `is_message_valid = false`.

**Gateway:** routes to `customer_validation_task` if valid, or to `End (Invalid Message)` if not.

---

### 3. customer_validation_task (Service Task)

**Executor:** `CustomerValidationTaskExecutor` → `CustomerValidationTaskDefinition`

Validates the customer. Sets:
- `is_customer_valid = true`
- `is_file_encrypted = false`

**Gateway (customer):** routes to encryption check if valid, or to `End (Invalid Customer)` if not.

**Gateway (encryption):** if `is_file_encrypted = true`, goes to `End (Encrypted File)`. Otherwise proceeds to syntax validation.

---

### 4. syntax_validation_request_task (Service Task)

**Executor:** `SyntaxValidationRequestTaskExecutor` → `SyntaxValidationRequestTaskDefinition`

Sends the payment XML path to the external syntax validation service via IBM MQ:
- **Queue:** `SYNTAX.VALIDATION.REQUEST.QUEUE`
- **Payload:** `{"paymentXmlPath": "<s3-path>"}`
- **JMS Correlation ID:** set to `jmsMessageId` so the response can be matched back

Also writes `correlationId` into the process variables for the Camunda message correlation.

---

### 5. syntax_validation_response_task (Receive Task)

**BPMN Message:** `syntax_validation_response_message`

The process **suspends** here — Camunda persists the state to PostgreSQL and waits. When a response arrives on `SYNTAX.VALIDATION.RESPONSE.QUEUE`, `SyntaxValidationResponseListener.onMessage()`:

1. Reads the JMS correlation ID
2. Creates a `MessageExecutionContextImpl` and calls `SyntaxValidationResponseTaskExecutor.executeMessage()` → `SyntaxValidationResponseTaskDefinition.execute()`, which sets `is_syntax_valid = true`
3. Calls `runtimeService.createMessageCorrelation("syntax_validation_response_message").processInstanceVariableEquals("correlationId", correlationId).setVariables(vars).correlate()`
4. If correlation fails with `MismatchingMessageCorrelationException` (receive task not yet committed by Camunda), retries up to 10 times with 500ms backoff

**Gateway:** routes to `duplicate_check_task` if syntax is valid, or to `End (Invalid Syntax)` if not.

---

### 6. duplicate_check_task (Service Task)

**Executor:** `DuplicateCheckTaskExecutor` → `DuplicateCheckTaskDefinition`

Checks whether the file has already been processed. Sets `is_file_duplicate = false`.

**Gateway:** routes to `debulking_request_task` if not a duplicate, or to `End (Duplicate File)` if it is.

---

### 7. debulking_request_task (Service Task)

**Executor:** `DebulkingRequestTaskExecutor` → `DebulkingRequestTaskDefinition`

Sends a `DebulkingPain008Request` payload to the File Process service via IBM MQ:
- **Queue:** `FILE.PROCESS.SERVICE.REQUEST.QUEUE`
- **Payload:**
```json
{
  "custId": 123456,
  "fileId": 7890123,
  "fileS3Path": "FWB_DIRECT_DEBIT/PAYMENT_FILES/.../I1234567890123.FWB.pain00800108...xml"
}
```
- **JMS Correlation ID:** same `jmsMessageId` used throughout

---

### 8. debulking_response_task (Receive Task)

**BPMN Message:** `debulking_response_message`

The process **suspends** again. `DebulkingResponseListener` on `FILE.PROCESS.SERVICE.RESPONSE.QUEUE` follows the exact same pattern as the syntax validation listener — executes `DebulkingResponseTaskDefinition`, sets `is_debulking_completed = true`, and correlates back to the waiting process instance with retry logic.

**Gateway:** routes to `sddo_orchestration_dispatching_task` if debulking completed, or to `End (FSN during Debulking)` if a failure notice (FSN) was raised.

---

### 9. sddo_orchestration_dispatching_task (Service Task)

**Executor:** `OrchestrationWorkflowDispatchTaskExecutor` → `OrchestrationWorkflowDispatchTaskDefinition`

Calls the SepaDDO Orchestration Service via REST:
- **URL:** `POST http://localhost:8081/v1/triggerToMq`
- **Client:** `FileProcessServiceClient` (Spring bean, `RestTemplate`)
- **Request:** `DispatchRequest(custId, fileId)`
- **Response:** `DispatchResponse(numberOfTxnDispatched)`

After this completes, the process reaches the **End Event**.

---

## Project Structure

```
src/main/java/com/forward/direct/debit/
│
├── DirectDebitWorkflowApplication.java          # Spring Boot entry point
│
├── camunda/
│   ├── CamundaBPMHelper.java                    # Static executor lookup + cache
│   ├── CamundaSetup.java                        # Singleton; reads tasks.json at startup
│   └── model/
│       ├── BusinessProcess.java                 # Process variable holder
│       ├── DebulkingPain008Request.java          # MQ request payload model
│       └── InputMessage.java                    # Incoming message record
│
├── camunda/task/
│   ├── common/
│   │   ├── ExecutionContext.java                # Interface: get/set process variables
│   │   ├── ExecutionContextImpl.java            # Wraps Camunda DelegateExecution
│   │   └── MessageExecutionContextImpl.java     # Standalone context for MQ receive
│   │
│   ├── definition/                              # Business logic (what to do)
│   │   ├── ServiceTaskDefinition.java           # Abstract base for service tasks
│   │   ├── MessageReceiveTaskDefinition.java    # Abstract base for receive tasks
│   │   ├── MessageValidationTaskDefinition.java
│   │   ├── CustomerValidationTaskDefinition.java
│   │   ├── SyntaxValidationRequestTaskDefinition.java
│   │   ├── SyntaxValidationResponseTaskDefinition.java
│   │   ├── DuplicateCheckTaskDefinition.java
│   │   ├── DebulkingRequestTaskDefinition.java
│   │   ├── DebulkingResponseTaskDefinition.java
│   │   └── OrchestrationWorkflowDispatchTaskDefinition.java
│   │
│   └── executor/                                # Camunda → TaskDefinition bridge (how to dispatch)
│       ├── CamundaTaskExecutor.java             # Single BPMN delegate for ALL service tasks
│       ├── TaskExecutor.java                    # Interface: executeTask(context, appCtx)
│       ├── MessageExecutor.java                 # Interface: executeMessage(context, message)
│       ├── ServiceTaskExecutor.java             # Abstract; calls getTaskDefinition().execute()
│       ├── ReceiveTaskExecutor.java             # Abstract; calls getTaskDefinition().execute(message)
│       ├── MessageValidationTaskExecutor.java
│       ├── CustomerValidationTaskExecutor.java
│       ├── SyntaxValidationRequestTaskExecutor.java
│       ├── SyntaxValidationResponseTaskExecutor.java
│       ├── DuplicateCheckTaskExecutor.java
│       ├── DebulkingRequestTaskExecutor.java
│       ├── DebulkingResponseTaskExecutor.java
│       └── OrchestrationWorkflowDispatchTaskExecutor.java
│
├── executor/
│   ├── CamundaProcessExecutor.java              # Thread pool; submits BusinessProcessExecutionThread
│   └── BusinessProcessExecutionThread.java      # Callable; starts Camunda process instance
│
├── listener/
│   ├── MQConnectionManager.java                 # IBM MQ Connection + Session factory
│   ├── MQMessageListener.java                   # Inbound listener on FIRST.TEST.QUEUE
│   ├── MQConfig.java                            # MQ config POJO (syntax validation)
│   ├── DebulkingMQConfig.java                   # MQ config POJO (debulking)
│   ├── SyntaxValidationResponseListener.java    # Listens on SYNTAX.VALIDATION.RESPONSE.QUEUE
│   └── DebulkingResponseListener.java           # Listens on FILE.PROCESS.SERVICE.RESPONSE.QUEUE
│
├── config/
│   ├── ApplicationContextHolder.java            # Static Spring context holder
│   ├── MQListenerConfiguration.java             # Wires all MQ listeners as Spring beans
│   └── RestTemplateConfig.java                  # RestTemplate bean
│
├── controller/
│   └── CleanupController.java                   # DELETE /dev/cleanup-instances
│
└── integrations/fileprocess/
    ├── FileProcessServiceClient.java            # REST client → SepaDDO :8081
    └── model/
        ├── DispatchRequest.java
        └── DispatchResponse.java

src/main/resources/
├── direct-debit-workflow.bpmn                   # BPMN 2.0 process definition
├── tasks.json                                   # Task ID → Executor class registry
├── application.properties                       # DB + Camunda config
└── META-INF/processes.xml                       # Camunda process archive config
```

---

## Task Registry — tasks.json

`CamundaSetup` reads this file at startup (before the application context is ready) and builds three lookup maps used at runtime. It is the central wiring between BPMN activity IDs and Java executor classes.

```json
{
  "ServiceTasks": [
    {
      "taskName": "message_validation_task",
      "executorClass": "com.forward.direct.debit.camunda.task.executor.MessageValidationTaskExecutor"
    },
    {
      "taskName": "customer_validation_task",
      "executorClass": "com.forward.direct.debit.camunda.task.executor.CustomerValidationTaskExecutor"
    },
    {
      "taskName": "syntax_validation_request_task",
      "executorClass": "com.forward.direct.debit.camunda.task.executor.SyntaxValidationRequestTaskExecutor"
    },
    {
      "taskName": "duplicate_check_task",
      "executorClass": "com.forward.direct.debit.camunda.task.executor.DuplicateCheckTaskExecutor"
    },
    {
      "taskName": "debulking_request_task",
      "executorClass": "com.forward.direct.debit.camunda.task.executor.DebulkingRequestTaskExecutor"
    },
    {
      "taskName": "sddo_orchestration_dispatching_task",
      "executorClass": "com.forward.direct.debit.camunda.task.executor.OrchestrationWorkflowDispatchTaskExecutor"
    }
  ],
  "ReceiveTasks": [
    {
      "queueName": "SYNTAX.VALIDATION.RESPONSE.QUEUE",
      "messageName": "syntax_validation_response_message",
      "executorClass": "com.forward.direct.debit.camunda.task.executor.SyntaxValidationResponseTaskExecutor"
    },
    {
      "queueName": "FILE.PROCESS.SERVICE.RESPONSE.QUEUE",
      "messageName": "debulking_response_message",
      "executorClass": "com.forward.direct.debit.camunda.task.executor.DebulkingResponseTaskExecutor"
    }
  ]
}
```

**Maps built from this file:**

| Map | Key | Value |
|-----|-----|-------|
| `taskMap` | BPMN activity ID (e.g. `message_validation_task`) | Executor class name |
| `queueMessageNameMap` | MQ queue name | BPMN message name |
| `messageNameExecutorMap` | BPMN message name | Executor class name |

Executor instances are cached in `CamundaBPMHelper.executorInstanceMap` (a `ConcurrentHashMap`) so each class is instantiated only once via reflection.

---

## Service Tasks

All six service tasks in the BPMN file share a single Camunda delegate class:

```xml
camunda:class="com.forward.direct.debit.camunda.task.executor.CamundaTaskExecutor"
```

### How CamundaTaskExecutor dispatches work

```
Camunda Engine calls CamundaTaskExecutor.execute(ActivityExecution)
        │
        ├─ reads currentActivityId (e.g. "message_validation_task")
        │
        ├─ CamundaBPMHelper.getTaskExecutor(activityId)
        │         │
        │         └─ looks up activityId in CamundaSetup.taskMap
        │            → returns cached/new executor instance
        │
        ├─ builds ExecutionContextImpl(DelegateExecution)
        │
        ├─ taskExecutor.executeTask(executionContext, applicationContext)
        │         │
        │         └─ ServiceTaskExecutor.executeTask()
        │                    │
        │                    └─ getTaskDefinition(context, appCtx).execute()
        │                               │
        │                               └─ concrete business logic runs
        │                                  (read/write process variables,
        │                                   call MQ, call REST, etc.)
        │
        └─ leave(execution)   ← advances the process to the next node
```

### Service Task Class Hierarchy

```
TaskExecutor (interface)
    └── ServiceTaskExecutor (abstract)
            ├── MessageValidationTaskExecutor
            ├── CustomerValidationTaskExecutor
            ├── SyntaxValidationRequestTaskExecutor
            ├── DuplicateCheckTaskExecutor
            ├── DebulkingRequestTaskExecutor
            └── OrchestrationWorkflowDispatchTaskExecutor

ServiceTaskDefinition (abstract) — holds ExecutionContext + ApplicationContext
    ├── MessageValidationTaskDefinition       → parses InputMessage, sets is_message_valid
    ├── CustomerValidationTaskDefinition      → sets is_customer_valid, is_file_encrypted
    ├── SyntaxValidationRequestTaskDefinition → sends to SYNTAX.VALIDATION.REQUEST.QUEUE
    ├── DuplicateCheckTaskDefinition          → sets is_file_duplicate
    ├── DebulkingRequestTaskDefinition        → sends DebulkingPain008Request to FILE.PROCESS.SERVICE.REQUEST.QUEUE
    └── OrchestrationWorkflowDispatchTaskDefinition → calls FileProcessServiceClient REST
```

### ExecutionContext

`ExecutionContextImpl` wraps Camunda's `DelegateExecution`. Variable reads and writes go through it:

```java
// Reading a process variable
String incoming = (String) executionContext.getVariable("incomingMessage");

// Writing a process variable (also propagates to Camunda engine)
executionContext.setVariable("is_message_valid", true);
```

The `ApplicationContext` is injected into task definitions via `ApplicationContextHolder.getApplicationContext()` — a static holder populated at startup. This is necessary because `CamundaTaskExecutor` is instantiated by Camunda's reflection, not by Spring, so `@Autowired` is not available there.

---

## Receive Tasks

Receive tasks pause the workflow until a correlated BPMN message arrives. There are two in this workflow.

### How message correlation works

```
External system sends response to MQ queue
        │
        ▼
ResponseListener.onMessage(Message)              ← runs on JMS listener thread
        │
        ├─ reads JMSCorrelationID from message
        ├─ looks up messageName from queueMessageNameMap
        ├─ gets MessageExecutor from CamundaBPMHelper
        ├─ creates MessageExecutionContextImpl with seed vars
        ├─ executor.executeMessage(context, message)
        │         └─ ReceiveTaskExecutor.executeMessage()
        │                  └─ MessageReceiveTaskDefinition.execute(message)
        │                           └─ reads message body, sets variables
        │                              (e.g. is_syntax_valid = true)
        │
        └─ scheduleCorrelationAttempt(...)       ← non-blocking, off listener thread
                  │
                  └─ runtimeService
                       .createMessageCorrelation(messageName)
                       .processInstanceVariableEquals("correlationId", correlationId)
                       .setVariables(vars)
                       .correlate()
                                │
                                └─ Camunda finds the suspended process instance,
                                   injects variables, and resumes execution
                                   past the Receive Task
```

### Retry on MismatchingMessageCorrelationException

There is an inherent race condition: the MQ response can arrive before Camunda has committed the `receiveTask` state to the database (because process startup happens on a worker thread and the receive task is only persisted once the engine transaction commits). Both listeners handle this with an exponential retry scheduler:

- Up to **10 attempts**
- **500ms delay** between attempts
- Implemented as a `ScheduledExecutorService` with 2 threads
- Listener thread is freed immediately; retries happen asynchronously

### Receive Task Class Hierarchy

```
MessageExecutor (interface)
    └── ReceiveTaskExecutor (abstract)
            ├── SyntaxValidationResponseTaskExecutor
            └── DebulkingResponseTaskExecutor

MessageReceiveTaskDefinition (abstract) — holds ExecutionContext
    ├── SyntaxValidationResponseTaskDefinition → sets is_syntax_valid = true
    └── DebulkingResponseTaskDefinition        → sets is_debulking_completed = true
```

### BPMN Message Declarations

```xml
<bpmn:message id="Message_2kvsq39" name="syntax_validation_response_message" />
<bpmn:message id="Message_02nc2qb" name="debulking_response_message" />
```

These names must match exactly the `messageName` values in `tasks.json` and the `messageName` passed to `createMessageCorrelation()`.

---

## How Camunda Processes the Workflow Internally

### 1. Process Deployment

On startup, `@EnableProcessApplication` triggers Camunda's auto-deployment. `META-INF/processes.xml` declares the `direct-debit-process` archive. Camunda scans the classpath for `*.bpmn` files and deploys them to the engine, storing the parsed process definition in PostgreSQL (`act_re_procdef`, `act_ge_bytearray`).

### 2. Process Instance Creation

```java
runtimeService.startProcessInstanceByKey(
    "direct-debit-process",   // process definition key from BPMN id attribute
    businessKey,               // JMS message ID — uniquely identifies this file
    processVariables           // initial vars from MQ message
);
```

Camunda creates a row in `act_ru_execution` (the active execution) and `act_ru_variable` (all process variables). These are written in a single database transaction.

### 3. Synchronous Task Execution

For service tasks, Camunda calls `CamundaTaskExecutor.execute(ActivityExecution)` **synchronously on the process thread**. When the method returns and `leave(execution)` is called, Camunda evaluates the outgoing sequence flows (gateway conditions) and advances to the next node — all within the same transaction.

The `AbstractBpmnActivityBehavior` base class gives access to `leave(execution)`, which is the correct way to advance past a service task. This is different from `JavaDelegate` (which advances automatically) — using `AbstractBpmnActivityBehavior` allows for more control, including async signals.

### 4. Waiting at Receive Tasks

When Camunda reaches a Receive Task, it creates an event subscription (`act_ru_event_subscr`) linked to the BPMN message name and the process instance, then **suspends execution**. The process thread is released. The execution state (`act_ru_execution`) remains in the database, waiting for a `correlate()` call.

### 5. Message Correlation Resumes Execution

`runtimeService.createMessageCorrelation(messageName).processInstanceVariableEquals("correlationId", ...).correlate()` finds the matching event subscription, injects the provided variables into `act_ru_variable`, removes the subscription, and re-enters the process at the point after the Receive Task — all in a new transaction.

### 6. Exclusive Gateways

Camunda evaluates gateway conditions using JUEL (Java Unified Expression Language). Conditions like `${is_message_valid}` are evaluated against the current process variables. The first condition that evaluates to `true` wins; if no condition matches, the process throws an exception.

### 7. History and Persistence

`camunda.bpm.job-execution.enabled=true` enables the Job Executor for timer/async tasks. `camunda:historyTimeToLive="180"` (set on the process in the BPMN) means historical data for completed instances is retained for 180 days. `camunda.bpm.database.schema-update=true` automatically creates or updates the ~50 Camunda tables on startup.

### 8. Thread Pool

`CamundaProcessExecutor` extends `ThreadPoolTaskExecutor`:

| Property | Default | Config key |
|----------|---------|------------|
| Core pool size | 5 | `camunda.executor.core.pool.size` |
| Max pool size | 20 | `camunda.executor.max.pool.size` |
| Queue capacity | 100 | `camunda.executor.queue.capacity` |
| Keep-alive seconds | 60 | `camunda.executor.keep.alive.seconds` |
| Thread prefix | `camunda-process-worker-` | `camunda.executor.thread.name.prefix` |

If the pool and queue are both full, `CallerRunsPolicy` kicks in — the MQ listener thread itself runs the task, providing natural back-pressure.

---

## IBM MQ Integration

### Queues

| Queue | Direction | Used by |
|-------|-----------|---------|
| `FIRST.TEST.QUEUE` | Inbound | `MQMessageListener` — triggers new process instances |
| `SYNTAX.VALIDATION.REQUEST.QUEUE` | Outbound | `SyntaxValidationRequestTaskDefinition` |
| `SYNTAX.VALIDATION.RESPONSE.QUEUE` | Inbound | `SyntaxValidationResponseListener` |
| `FILE.PROCESS.SERVICE.REQUEST.QUEUE` | Outbound | `DebulkingRequestTaskDefinition` |
| `FILE.PROCESS.SERVICE.RESPONSE.QUEUE` | Inbound | `DebulkingResponseListener` |

### MQ Connection Configuration

All three listeners use the same MQ server. Connection parameters come from `application.properties` (via `MQListenerConfiguration`):

```properties
ibm.mq.host=localhost
ibm.mq.port=1414
ibm.mq.channel=SYSTEM.DEF.SVRCONN
ibm.mq.queueManager=MY.TEST.QMNGR
ibm.mq.queue=FIRST.TEST.QUEUE
```

Outbound sends (from task definitions) currently use hardcoded values matching the above defaults. Move these to properties for environment-specific deployments.

### Correlation Pattern

The `JMSCorrelationID` is used to match responses back to the correct process instance:
1. Inbound message ID (`jmsMessageId`) is stored as a process variable
2. Outbound request sets `JMSCorrelationID` to that same value
3. Response arrives with the same `JMSCorrelationID`
4. Camunda correlation uses `processInstanceVariableEquals("correlationId", correlationId)` to find the exact waiting instance

---

## Spring & Camunda Configuration

### application.properties

```properties
# PostgreSQL — Camunda uses this as its persistence store
spring.datasource.url=jdbc:postgresql://localhost:5432/camunda
spring.datasource.username=camunda
spring.datasource.password=camunda
spring.datasource.driver-class-name=org.postgresql.Driver

# Camunda — auto-create/update schema, enable async job execution
camunda.bpm.database.schema-update=true
camunda.bpm.job-execution.enabled=true

# Process definition key (used by MQListenerConfiguration)
camunda.process.definition.key=direct-debit-process

# Thread pool for process instance creation
camunda.executor.core.pool.size=5
camunda.executor.max.pool.size=20
camunda.executor.queue.capacity=100
camunda.executor.keep.alive.seconds=60
camunda.executor.thread.name.prefix=camunda-process-worker-

# IBM MQ
ibm.mq.host=localhost
ibm.mq.port=1414
ibm.mq.channel=SYSTEM.DEF.SVRCONN
ibm.mq.queueManager=MY.TEST.QMNGR
ibm.mq.queue=FIRST.TEST.QUEUE
```

### processes.xml (META-INF)

```xml
<process-application xmlns="http://www.camunda.org/schema/1.0/ProcessApplication">
  <process-archive name="direct-debit-process">
    <process-engine>default</process-engine>
    <properties>
      <property name="isDeleteUponUndeploy">false</property>
      <property name="isScanForProcessDefinitions">true</property>
    </properties>
  </process-archive>
</process-application>
```

`isScanForProcessDefinitions=true` tells Camunda to scan the classpath for `*.bpmn` files and deploy them automatically. `isDeleteUponUndeploy=false` preserves historical data when the app is redeployed.

### Spring Beans and Lifecycle

`MQListenerConfiguration` registers all three listeners as Spring beans with explicit lifecycle callbacks:

```java
@Bean(initMethod = "init",  destroyMethod = "shutdown")
MQListenerService mqListenerService(...)

@Bean(initMethod = "start", destroyMethod = "stop")
SyntaxValidationResponseListener syntaxValidationResponseListener(...)

@Bean(initMethod = "start", destroyMethod = "stop")
DebulkingResponseListener debulkingResponseListener(...)
```

Spring calls `init()`/`start()` after the application context is fully wired and `shutdown()`/`stop()` on graceful shutdown — ensuring MQ connections are closed cleanly.

### ApplicationContextHolder

Because `CamundaTaskExecutor` is instantiated by Camunda's reflection engine (not by Spring), it cannot use `@Autowired`. `ApplicationContextHolder` solves this:

```java
@Component
public class ApplicationContextHolder implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        applicationContext = context;  // Spring sets this after context loads
    }

    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }
}
```

Task definitions that need Spring beans (e.g. `OrchestrationWorkflowDispatchTaskDefinition` needing `FileProcessServiceClient`) fetch them via `applicationContext.getBean(...)` passed in from `CamundaTaskExecutor`.

---

## Running the Service

### Prerequisites

- Java 21
- Maven 3.8+
- PostgreSQL running on `localhost:5432` with database `camunda`, user `camunda`, password `camunda`
- IBM MQ Queue Manager `MY.TEST.QMNGR` on `localhost:1414` with channel `SYSTEM.DEF.SVRCONN`
- SepaDDO Orchestration Service running on `localhost:8081` (for the final dispatch step)

### Build and Run

```bash
mvn clean package -DskipTests
java -jar target/fwb-direct-debit-workflow-service-2-1.0-SNAPSHOT.jar
```

### Trigger a Process Instance Manually

Send a `TextMessage` to `FIRST.TEST.QUEUE` with body:

```json
{
  "fileDataSeq": 1001,
  "channelRef": "REF-001",
  "outputChannelCode": "DIRECT",
  "fileS3Path": "s3://bucket/path/to/pain008.xml"
}
```

The service will pick it up, start a `direct-debit-process` instance, and begin executing the workflow.

### Camunda Web Application

The `camunda-bpm-spring-boot-starter-webapp` dependency includes the Cockpit, Tasklist, and Admin UIs at:

```
http://localhost:8080/camunda/app/cockpit
```

Default credentials: `admin` / `admin` (configurable).

---

## Dev Utilities

### Delete all running process instances

```http
DELETE http://localhost:8080/dev/cleanup-instances
```

Handled by `CleanupController`. Deletes all active `direct-debit-process` instances — **development only**, never run in production.
