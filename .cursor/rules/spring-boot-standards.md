# Spring Boot Coding Standards & Best Practices

## 📋 Table of Contents
1. [SOLID Principles](#solid-principles)
2. [Architecture Patterns](#architecture-patterns)
3. [Code Organization](#code-organization)
4. [Dependency Injection](#dependency-injection)
5. [Exception Handling](#exception-handling)
6. [Validation](#validation)
7. [Transaction Management](#transaction-management)
8. [Logging](#logging)
9. [API Design](#api-design)
10. [Database Access](#database-access)
11. [Configuration Management](#configuration-management)
12. [Security](#security)
13. [Testing](#testing)
14. [Performance Optimization](#performance-optimization)
15. [Code Style & Naming](#code-style--naming)

---

## SOLID Principles

### Single Responsibility Principle (SRP)
- **Each class should have one reason to change**
- Controllers handle HTTP requests/responses only
- Services contain business logic only
- Repositories handle data access only
- Entities represent database tables only
- DTOs represent data transfer only

**Example:**
```java
// ✅ GOOD: Single responsibility
@Service
public class WorkflowService {
    public WorkflowResponse createWorkflow(CreateWorkflowRequest request) {
        // Business logic only
    }
}

// ❌ BAD: Multiple responsibilities
@Service
public class WorkflowService {
    public WorkflowResponse createWorkflow(CreateWorkflowRequest request) {
        // Business logic
        // HTTP handling
        // Database queries
        // Validation
    }
}
```

### Open/Closed Principle (OCP)
- **Open for extension, closed for modification**
- Use interfaces for services and repositories
- Use strategy pattern for different implementations
- Use factory pattern for object creation

**Example:**
```java
// ✅ GOOD: Extensible via interface
public interface NotificationChannel {
    void send(NotificationRequest request);
}

@Service
public class EmailChannel implements NotificationChannel { }
@Service
public class SmsChannel implements NotificationChannel { }

// ❌ BAD: Requires modification to add new channel
@Service
public class NotificationService {
    public void send(String type, NotificationRequest request) {
        if (type.equals("email")) { }
        else if (type.equals("sms")) { }
        // Must modify to add new type
    }
}
```

### Liskov Substitution Principle (LSP)
- **Subtypes must be substitutable for their base types**
- Implementations must honor interface contracts
- Don't throw exceptions not in interface
- Don't return null when interface expects object

### Interface Segregation Principle (ISP)
- **Clients should not depend on interfaces they don't use**
- Create specific interfaces instead of large general ones
- Split large interfaces into smaller, focused ones

**Example:**
```java
// ✅ GOOD: Segregated interfaces
public interface WorkflowReader {
    WorkflowResponse getWorkflow(String id);
    List<WorkflowResponse> listWorkflows();
}

public interface WorkflowWriter {
    WorkflowResponse createWorkflow(CreateWorkflowRequest request);
    WorkflowResponse updateWorkflow(String id, UpdateWorkflowRequest request);
    void deleteWorkflow(String id);
}

// ❌ BAD: Large interface
public interface WorkflowService {
    WorkflowResponse getWorkflow(String id);
    List<WorkflowResponse> listWorkflows();
    WorkflowResponse createWorkflow(CreateWorkflowRequest request);
    WorkflowResponse updateWorkflow(String id, UpdateWorkflowRequest request);
    void deleteWorkflow(String id);
    void executeWorkflow(String id);
    // Many more methods...
}
```

### Dependency Inversion Principle (DIP)
- **Depend on abstractions, not concretions**
- Use interfaces for dependencies
- Inject interfaces, not concrete classes
- High-level modules should not depend on low-level modules

---

## Architecture Patterns

### Layered Architecture
```
Controller Layer (HTTP handling)
    ↓
Service Layer (Business logic)
    ↓
Repository Layer (Data access)
    ↓
Entity Layer (Database mapping)
```

**Rules:**
- Controllers should NOT contain business logic
- Services should NOT contain HTTP-specific code
- Repositories should NOT contain business logic
- Entities should NOT contain business logic

### DTO Pattern
- **Always use DTOs for API communication**
- Never expose entities directly in API
- Use mappers (MapStruct) to convert between entities and DTOs
- Separate request DTOs from response DTOs

**Example:**
```java
// ✅ GOOD: Use DTOs
@PostMapping
public ResponseEntity<WorkflowResponse> createWorkflow(
    @Valid @RequestBody CreateWorkflowRequest request) {
    WorkflowResponse response = workflowService.createWorkflow(request);
    return ResponseEntity.ok(response);
}

// ❌ BAD: Expose entity directly
@PostMapping
public ResponseEntity<Workflow> createWorkflow(@RequestBody Workflow workflow) {
    Workflow saved = workflowService.save(workflow);
    return ResponseEntity.ok(saved);
}
```

### Repository Pattern
- Use Spring Data JPA repositories
- Extend `JpaRepository<T, ID>` for standard CRUD
- Create custom query methods when needed
- Use `@Query` for complex queries

**Example:**
```java
// ✅ GOOD: Repository pattern
@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, String> {
    List<Workflow> findByStatus(WorkflowStatus status);
    
    @Query("SELECT w FROM Workflow w WHERE w.name LIKE %:search%")
    List<Workflow> searchByName(@Param("search") String search);
}
```

---

## Code Organization

### Package Structure
```
com.notificationplatform/
├── controller/          # REST Controllers
├── service/            # Business logic (interfaces + implementations)
│   ├── workflow/
│   ├── notification/
│   └── ...
├── repository/         # Data access (Spring Data JPA)
├── entity/             # JPA Entities
├── dto/                # Data Transfer Objects
│   ├── request/
│   └── response/
├── mapper/             # MapStruct mappers
├── config/             # Configuration classes
├── exception/          # Custom exceptions
├── security/           # Security configuration
├── integration/        # Third-party integrations
└── util/               # Utility classes
```

### File Naming Conventions
- **Controllers**: `*Controller.java` (e.g., `WorkflowController.java`)
- **Services**: `*Service.java` (interface), `*ServiceImpl.java` (implementation)
- **Repositories**: `*Repository.java` (e.g., `WorkflowRepository.java`)
- **Entities**: `*.java` (e.g., `Workflow.java`)
- **DTOs**: `*Request.java`, `*Response.java` (e.g., `CreateWorkflowRequest.java`)
- **Mappers**: `*Mapper.java` (e.g., `WorkflowMapper.java`)
- **Exceptions**: `*Exception.java` (e.g., `ResourceNotFoundException.java`)

---

## Dependency Injection

### Constructor Injection (MANDATORY)
- **Always use constructor injection**
- Never use field injection (`@Autowired` on fields)
- Never use setter injection
- Make dependencies `final` and `private`

**Example:**
```java
// ✅ GOOD: Constructor injection
@Service
public class WorkflowServiceImpl implements WorkflowService {
    private final WorkflowRepository workflowRepository;
    private final WorkflowMapper workflowMapper;
    
    public WorkflowServiceImpl(WorkflowRepository workflowRepository,
                              WorkflowMapper workflowMapper) {
        this.workflowRepository = workflowRepository;
        this.workflowMapper = workflowMapper;
    }
}

// ❌ BAD: Field injection
@Service
public class WorkflowServiceImpl implements WorkflowService {
    @Autowired
    private WorkflowRepository workflowRepository;
    
    @Autowired
    private WorkflowMapper workflowMapper;
}
```

### Interface-Based Design
- **Always define interfaces for services**
- Implement interfaces in service classes
- Inject interfaces, not implementations

**Example:**
```java
// ✅ GOOD: Interface-based
public interface WorkflowService {
    WorkflowResponse createWorkflow(CreateWorkflowRequest request);
}

@Service
public class WorkflowServiceImpl implements WorkflowService {
    // Implementation
}

@RestController
public class WorkflowController {
    private final WorkflowService workflowService; // Interface, not implementation
}
```

---

## Exception Handling

### Global Exception Handler
- **Use `@ControllerAdvice` for global exception handling**
- Create custom exception classes
- Return consistent error response format
- Log all exceptions with context

**Example:**
```java
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex) {
        String requestId = UUID.randomUUID().toString();
        log.error("Resource not found [requestId={}]: {}", requestId, ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            new ErrorResponse.ErrorInfo(
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                Map.of(),
                LocalDateTime.now()
            ),
            requestId
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}
```

### Custom Exceptions
- Create specific exception classes for different error types
- Extend `RuntimeException` or appropriate base exception
- Include meaningful error messages
- Use exception hierarchy

**Example:**
```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resourceType, String id) {
        super(String.format("%s not found with id: %s", resourceType, id));
    }
}
```

### Exception Handling Rules
- **Never catch and ignore exceptions** (unless explicitly required)
- **Always log exceptions** with context (requestId, userId, etc.)
- **Don't expose internal errors** to clients
- **Return appropriate HTTP status codes**
- **Use checked exceptions only when caller must handle them**

---

## Validation

### Bean Validation (Jakarta Validation)
- **Use `@Valid` on controller method parameters**
- Use validation annotations on DTOs
- Validate at controller level, not service level
- Use custom validators for complex validation

**Example:**
```java
// Controller
@PostMapping
public ResponseEntity<WorkflowResponse> createWorkflow(
    @Valid @RequestBody CreateWorkflowRequest request) {
    // Validation happens automatically
}

// DTO
public class CreateWorkflowRequest {
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;
    
    @NotNull(message = "Status is required")
    private WorkflowStatus status;
    
    @Valid
    @NotNull(message = "Nodes are required")
    private List<NodeRequest> nodes;
}
```

### Custom Validation
- Create custom validators for business rules
- Use `@Constraint` annotation
- Implement `ConstraintValidator` interface

**Example:**
```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CronExpressionValidator.class)
public @interface ValidCronExpression {
    String message() default "Invalid cron expression";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

### Validation Rules
- **Validate all inputs** at controller level
- **Validate business rules** at service level
- **Use validation groups** for different scenarios (create vs update)
- **Return detailed validation errors** in error response

---

## Transaction Management

### Transaction Annotations
- **Use `@Transactional` on service methods**
- Use `@Transactional(readOnly = true)` for read-only operations
- Place `@Transactional` on service interface or implementation (not both)
- Understand transaction propagation and isolation

**Example:**
```java
@Service
@Transactional
public class WorkflowServiceImpl implements WorkflowService {
    
    @Override
    public WorkflowResponse createWorkflow(CreateWorkflowRequest request) {
        // Transactional by default
    }
    
    @Override
    @Transactional(readOnly = true)
    public WorkflowResponse getWorkflowById(String id) {
        // Read-only transaction
    }
}
```

### Transaction Rules
- **Keep transactions short** - don't include long-running operations
- **Don't use transactions in controllers** - only in services
- **Handle transaction rollback** for exceptions
- **Use `@Transactional(propagation = Propagation.REQUIRES_NEW)`** when needed
- **Avoid nested transactions** unless necessary

### Transaction Best Practices
- Read-only operations should use `readOnly = true`
- Write operations should use default transaction settings
- Use `@Transactional(rollbackFor = Exception.class)` if needed
- Don't catch exceptions that should trigger rollback

---

## Logging

### SLF4J + Logback
- **Use SLF4J for logging** (via Lombok `@Slf4j`)
- Configure Logback for structured logging
- Use appropriate log levels
- Include context in log messages

**Example:**
```java
@Slf4j
@Service
public class WorkflowServiceImpl implements WorkflowService {
    
    public WorkflowResponse createWorkflow(CreateWorkflowRequest request) {
        log.info("Creating workflow: name={}", request.getName());
        
        try {
            Workflow workflow = workflowMapper.toEntity(request);
            Workflow saved = workflowRepository.save(workflow);
            log.info("Workflow created successfully: id={}, name={}", 
                    saved.getId(), saved.getName());
            return workflowMapper.toResponse(saved);
        } catch (Exception e) {
            log.error("Failed to create workflow: name={}", request.getName(), e);
            throw e;
        }
    }
}
```

### Log Levels
- **ERROR**: Errors that need immediate attention
- **WARN**: Warning conditions (e.g., deprecated API usage)
- **INFO**: Informational messages (e.g., business events)
- **DEBUG**: Detailed information for debugging
- **TRACE**: Very detailed information

### Logging Best Practices
- **Include context** in log messages (requestId, userId, workflowId, etc.)
- **Use structured logging** (key-value pairs)
- **Don't log sensitive information** (passwords, tokens, PII)
- **Log exceptions with stack traces** at ERROR level
- **Use appropriate log levels** - don't log everything at ERROR

---

## API Design

### RESTful Conventions
- **Use proper HTTP methods**: GET (read), POST (create), PUT (update), DELETE (delete)
- **Use proper HTTP status codes**: 200 (OK), 201 (Created), 204 (No Content), 400 (Bad Request), 404 (Not Found), 500 (Internal Server Error)
- **Use proper URL patterns**: `/resources`, `/resources/{id}`, `/resources/{id}/sub-resources`
- **Use proper request/response formats**: JSON

**Example:**
```java
@RestController
@RequestMapping("/workflows")
public class WorkflowController {
    
    @PostMapping
    public ResponseEntity<WorkflowResponse> createWorkflow(
        @Valid @RequestBody CreateWorkflowRequest request) {
        WorkflowResponse response = workflowService.createWorkflow(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<WorkflowResponse> getWorkflow(@PathVariable String id) {
        WorkflowResponse response = workflowService.getWorkflowById(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<WorkflowResponse> updateWorkflow(
        @PathVariable String id,
        @Valid @RequestBody UpdateWorkflowRequest request) {
        WorkflowResponse response = workflowService.updateWorkflow(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable String id) {
        workflowService.deleteWorkflow(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Response Format
- **Use consistent response format** across all endpoints
- **Use DTOs for responses** (never expose entities)
- **Include pagination** for list endpoints
- **Return appropriate status codes**

**Example:**
```java
// Single resource response
{
    "id": "workflow-123",
    "name": "Welcome Email",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00Z"
}

// List response with pagination
{
    "data": [...],
    "pagination": {
        "total": 100,
        "limit": 20,
        "offset": 0
    }
}

// Error response
{
    "error": {
        "code": "RESOURCE_NOT_FOUND",
        "message": "Workflow not found with id: workflow-123",
        "details": {},
        "timestamp": "2024-01-15T10:30:00Z"
    },
    "requestId": "req-123"
}
```

### API Versioning
- **Version APIs** when making breaking changes
- Use URL versioning: `/api/v1/workflows`, `/api/v2/workflows`
- Or use header versioning: `Accept: application/vnd.api+json;version=1`

---

## Database Access

### Spring Data JPA
- **Use Spring Data JPA repositories** for data access
- **Extend `JpaRepository<T, ID>`** for standard CRUD
- **Create custom query methods** when needed
- **Use `@Query` for complex queries**

**Example:**
```java
@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, String> {
    
    // Method name query
    List<Workflow> findByStatus(WorkflowStatus status);
    
    // Custom query
    @Query("SELECT w FROM Workflow w WHERE w.name LIKE %:search% " +
           "AND w.status = :status")
    List<Workflow> searchByNameAndStatus(
        @Param("search") String search,
        @Param("status") WorkflowStatus status);
    
    // Native query (use sparingly)
    @Query(value = "SELECT * FROM workflows WHERE created_at > :date",
           nativeQuery = true)
    List<Workflow> findRecentWorkflows(@Param("date") LocalDateTime date);
}
```

### Entity Design
- **Use JPA annotations** properly
- **Use `@Entity`, `@Table`, `@Column`** annotations
- **Use `@Id`, `@GeneratedValue`** for primary keys
- **Use `@OneToMany`, `@ManyToOne`, `@ManyToMany`** for relationships
- **Use `@CreatedDate`, `@LastModifiedDate`** for audit fields

**Example:**
```java
@Entity
@Table(name = "workflows")
public class Workflow {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowStatus status;
    
    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL)
    private List<Node> nodes;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

### Database Best Practices
- **Use transactions** for write operations
- **Use `@Transactional(readOnly = true)`** for read operations
- **Avoid N+1 queries** - use `@EntityGraph` or `JOIN FETCH`
- **Use pagination** for large result sets
- **Use indexes** on frequently queried columns
- **Don't use `SELECT *`** - select only needed columns

---

## Configuration Management

### Externalized Configuration
- **Use `application.yml` or `application.properties`**
- **Use Spring Profiles** for different environments
- **Use `@ConfigurationProperties`** for type-safe configuration
- **Never hardcode configuration values**

**Example:**
```java
@ConfigurationProperties(prefix = "app.workflow")
@Data
public class WorkflowProperties {
    private int maxNodes = 100;
    private int maxExecutionTime = 3600;
    private boolean enableValidation = true;
}

// application.yml
app:
  workflow:
    max-nodes: 100
    max-execution-time: 3600
    enable-validation: true
```

### Environment Variables
- **Use environment variables** for sensitive data
- **Use `@Value` annotation** for simple values
- **Use `@ConfigurationProperties`** for complex configuration
- **Never commit secrets** to version control

**Example:**
```yaml
# application.yml
database:
  url: ${DB_URL:jdbc:postgresql://localhost:5432/notificationplatform}
  username: ${DB_USERNAME:postgres}
  password: ${DB_PASSWORD:password}
```

### Configuration Best Practices
- **Externalize all configuration** - don't hardcode
- **Use profiles** for environment-specific config
- **Validate configuration** at startup
- **Document configuration properties**

---

## Security

### Spring Security
- **Use Spring Security** for authentication and authorization
- **Use JWT tokens** for stateless authentication
- **Validate all inputs** to prevent injection attacks
- **Use HTTPS** in production
- **Implement proper CORS** configuration

### Input Validation
- **Validate all user inputs** using Bean Validation
- **Sanitize inputs** to prevent XSS attacks
- **Use parameterized queries** (JPA does this automatically)
- **Validate file uploads** (size, type, content)

### Security Best Practices
- **Never trust user input** - always validate
- **Use prepared statements** (JPA does this)
- **Encrypt sensitive data** at rest
- **Use secure password hashing** (BCrypt)
- **Implement rate limiting** for APIs
- **Log security events** (failed logins, unauthorized access)

---

## Testing

### Unit Testing
- **Write unit tests** for all business logic
- **Use JUnit 5** and Mockito
- **Test edge cases** and error scenarios
- **Maintain test coverage** above 70%

**Example:**
```java
@ExtendWith(MockitoExtension.class)
class WorkflowServiceImplTest {
    
    @Mock
    private WorkflowRepository workflowRepository;
    
    @Mock
    private WorkflowMapper workflowMapper;
    
    @InjectMocks
    private WorkflowServiceImpl workflowService;
    
    @Test
    void createWorkflow_ShouldReturnWorkflowResponse_WhenRequestIsValid() {
        // Given
        CreateWorkflowRequest request = new CreateWorkflowRequest();
        Workflow workflow = new Workflow();
        WorkflowResponse response = new WorkflowResponse();
        
        when(workflowMapper.toEntity(request)).thenReturn(workflow);
        when(workflowRepository.save(workflow)).thenReturn(workflow);
        when(workflowMapper.toResponse(workflow)).thenReturn(response);
        
        // When
        WorkflowResponse result = workflowService.createWorkflow(request);
        
        // Then
        assertThat(result).isNotNull();
        verify(workflowRepository).save(workflow);
    }
}
```

### Integration Testing
- **Write integration tests** for API endpoints
- **Use `@SpringBootTest`** for full context
- **Use `@WebMvcTest`** for controller testing
- **Use TestContainers** for database testing (optional)

**Example:**
```java
@SpringBootTest
@AutoConfigureMockMvc
class WorkflowControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void createWorkflow_ShouldReturn201_WhenRequestIsValid() throws Exception {
        CreateWorkflowRequest request = new CreateWorkflowRequest();
        request.setName("Test Workflow");
        
        mockMvc.perform(post("/workflows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Workflow"));
    }
}
```

### Testing Best Practices
- **Test happy paths** and error paths
- **Use descriptive test names** (should_expectedBehavior_whenStateUnderTest)
- **Keep tests independent** - don't rely on test execution order
- **Mock external dependencies** in unit tests
- **Use real dependencies** in integration tests (when possible)

---

## Performance Optimization

### Caching
- **Use Spring Cache** for frequently accessed data
- **Cache at service level** using `@Cacheable`
- **Invalidate cache** when data changes using `@CacheEvict`

**Example:**
```java
@Service
public class WorkflowServiceImpl implements WorkflowService {
    
    @Cacheable(value = "workflows", key = "#id")
    public WorkflowResponse getWorkflowById(String id) {
        // Expensive operation
    }
    
    @CacheEvict(value = "workflows", key = "#id")
    public WorkflowResponse updateWorkflow(String id, UpdateWorkflowRequest request) {
        // Update operation
    }
}
```

### Database Optimization
- **Use pagination** for large result sets
- **Use `@EntityGraph`** or `JOIN FETCH` to avoid N+1 queries
- **Use indexes** on frequently queried columns
- **Optimize queries** - avoid `SELECT *`

### Performance Best Practices
- **Profile your application** to find bottlenecks
- **Use connection pooling** (Spring Boot does this automatically)
- **Use async processing** for long-running operations
- **Monitor application performance** using Actuator

---

## Code Style & Naming

### Naming Conventions
- **Classes**: PascalCase (e.g., `WorkflowController`)
- **Methods**: camelCase (e.g., `createWorkflow`)
- **Variables**: camelCase (e.g., `workflowService`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)
- **Packages**: lowercase (e.g., `com.notificationplatform.service`)

### Code Style Rules
- **Use meaningful names** - code should be self-documenting
- **Keep methods short** - max 20-30 lines
- **Keep classes focused** - single responsibility
- **Use comments** only when code is not self-explanatory
- **Remove dead code** - don't leave commented code

### Lombok Usage
- **Use `@Slf4j`** for logging
- **Use `@Data`** for simple DTOs (be careful with entities)
- **Use `@Builder`** for complex object construction
- **Use `@AllArgsConstructor` / `@NoArgsConstructor`** when needed
- **Don't use Lombok** for entities with JPA relationships (can cause issues)

**Example:**
```java
@Slf4j
@Service
public class WorkflowServiceImpl implements WorkflowService {
    // Logger available as 'log'
}

@Data
public class CreateWorkflowRequest {
    private String name;
    private WorkflowStatus status;
    // Getters, setters, equals, hashCode, toString generated
}
```

---

## Summary Checklist

### Before Writing Code
- [ ] Understand requirements from documentation
- [ ] Check API contract for request/response formats
- [ ] Check database schema for entity structure
- [ ] Plan the implementation (controller → service → repository)

### While Writing Code
- [ ] Use constructor injection (not field injection)
- [ ] Use interfaces for services
- [ ] Use DTOs for API communication
- [ ] Add `@Valid` for validation
- [ ] Add `@Transactional` for write operations
- [ ] Add logging with context
- [ ] Handle exceptions properly
- [ ] Follow naming conventions

### After Writing Code
- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Check test coverage
- [ ] Review code for best practices
- [ ] Update documentation if needed

---

## References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Jakarta Bean Validation](https://beanvalidation.org/)
- [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)
- [Clean Code by Robert C. Martin](https://www.amazon.com/Clean-Code-Handbook-Software-Craftsmanship/dp/0132350882)

