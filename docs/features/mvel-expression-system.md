# MVEL Expression System - Unified Dynamic Logic và Placeholders

> **Tài liệu này định nghĩa hệ thống MVEL expression được sử dụng cho TẤT CẢ dynamic logic và placeholders trong workflow system.**

## Tổng quan

**MVEL (MVFLEX Expression Language)** được sử dụng làm **unified system** cho tất cả dynamic logic và placeholders trong workflow system. Thay vì sử dụng nhiều syntax khác nhau (`${variable}`, template strings, custom expressions), hệ thống sẽ sử dụng **MVEL với cú pháp `@{expression}`** cho tất cả các trường hợp cần dynamic evaluation.

## Nguyên tắc cốt lõi

### 1. MVEL là Standard cho Dynamic Logic

**MVEL được sử dụng cho TẤT CẢ các trường hợp sau:**

1. **Action Config Values** - Config fields trong action nodes
   - URL: `@{_vars.baseUrl}/users/@{fetchUser.userId}`
   - Headers: `Bearer @{getToken.token}`
   - Body: `{"userId": "@{fetchUser.userId}", "name": "@{fetchUser.userName}"}`

2. **Email/Notification Templates** - Template content với placeholders
   - Subject: `Welcome @{user.firstName} @{user.lastName}!`
   - Body: `Hello @{user.firstName}, your order @{order.id} has been confirmed.`

3. **Function Node Expressions** - Expression evaluation
   - Expression: `@{user.firstName} + ' ' + @{user.lastName}`
   - Calculation: `@{order.total} * (1 + @{order.taxRate})`

4. **Condition Expressions** - Conditional logic
   - Condition: `@{user.age} >= 18 && @{user.status} == 'active'`
   - Filter: `@{order.amount} > 1000`

5. **Field Mappings** - Data transformation
   - Mapping: `@{source.firstName}` → `@{target.fullName}`
   - Transformation: `@{user.email.toLowerCase()}`

6. **Output Mapping** - Map raw response vào output schema
   - Mapping: `@{_response.statusCode}`, `@{_response.body.userId}`

7. **Any Dynamic Value** - Bất kỳ nơi nào cần dynamic evaluation
   - Kafka topic: `events-@{eventType}`
   - File path: `/data/@{userId}/@{timestamp}.json`
   - Query params: `?userId=@{userId}&token=@{token}`

### 2. Cú pháp MVEL: `@{expression}`

**Format**: `@{expression}` (KHÔNG phải `${expression}`)

**Lý do**:
- Tránh conflict với template strings trong JavaScript/TypeScript
- Dễ nhận biết trong code
- Consistent across toàn bộ hệ thống

### 3. Static Values hoặc MVEL Expressions

- User có thể cung cấp static value: `/users/1234`
- Hoặc sử dụng MVEL expression: `/users/@{userID}`
- MVEL sẽ được evaluate ở runtime với context (previous nodes, trigger data, variables)

## MVEL Expression Format và Syntax

MVEL sử dụng cú pháp `@{expression}` để đánh dấu dynamic expressions. Hệ thống sẽ scan và evaluate tất cả expressions trong strings, objects, arrays.

### 1. Simple Variable Reference
```mvel
@{variableName}
```
- Ví dụ: `@{userID}`, `@{apiUrl}`
- Resolve từ execution context

### 2. Nested Object Reference
```mvel
@{object.field}
@{object.nested.field}
```
- Ví dụ: `@{user.email}`, `@{order.items[0].price}`
- Resolve từ nested object trong context
- Support array indexing: `@{items[0]}`, `@{items[itemIndex]}`

### 3. Node Output Reference
```mvel
@{nodeId.field}
@{nodeId.nested.field}
```
- Ví dụ: `@{fetchUser.userId}`, `@{getToken.token}`
- Resolve từ previous node output
- Node ID là ID của node trong workflow

### 4. Trigger Data Reference
```mvel
@{_trigger.field}
@{_trigger.nested.field}
```
- Ví dụ: `@{_trigger.userId}`, `@{_trigger.eventType}`
- Resolve từ trigger data
- Prefix `_trigger` để phân biệt với node outputs

### 5. Workflow Variables Reference
```mvel
@{_vars.varName}
@{_vars.nested.varName}
```
- Ví dụ: `@{_vars.apiKey}`, `@{_vars.baseUrl}`
- Resolve từ workflow variables
- Prefix `_vars` để phân biệt

### 6. Response Data Reference (trong Output Mapping)
```mvel
@{_response.field}
@{_response.nested.field}
@{_response.body.userId}
```
- Ví dụ: `@{_response.statusCode}`, `@{_response.body.data}`
- Resolve từ raw action response
- Prefix `_response` chứa raw result từ action execution
- Available trong output mapping context

### 7. Built-in Functions
```mvel
@{_now()}              // Current timestamp (milliseconds)
@{_uuid()}             // Generate UUID
@{_date()}              // Current date string
@{_timestamp()}         // Current timestamp (seconds)
@{_formatDate(date, format)}  // Format date
@{_jsonStringify(obj)}  // Convert object to JSON string
@{_jsonParse(str)}      // Parse JSON string to object
```
- Built-in functions available trong execution context
- Có thể extend với custom functions

### 8. Complex Expressions
```mvel
@{user.firstName} + ' ' + @{user.lastName}           // String concatenation
@{order.total} * (1 + @{order.taxRate})              // Arithmetic
@{user.age} >= 18 && @{user.status} == 'active'      // Boolean logic
@{items.size()} > 0 ? @{items[0].name} : 'N/A'      // Ternary operator
@{user.email.toLowerCase()}                          // Method calls
```
- Full MVEL expression support
- String concatenation, arithmetic, boolean logic, method calls
- Support ternary operators, loops, conditionals

### 9. String Interpolation
```mvel
"Hello @{user.firstName}, welcome!"
"/users/@{userId}/orders/@{orderId}"
"Bearer @{token}"
```
- MVEL expressions có thể embedded trong strings
- System sẽ extract và evaluate expressions, replace với values

### 10. Object và Array Construction
```mvel
{"userId": "@{userId}", "name": "@{userName}"}       // Object với MVEL
["@{item1}", "@{item2}", "@{item3}"]                  // Array với MVEL
```
- MVEL expressions trong JSON objects và arrays
- Recursively evaluate nested structures

## MVEL Evaluation Flow

### 1. Build Execution Context (cho Config Evaluation)

Context được build từ nhiều sources:

```java
{
  // Previous node outputs (keyed by nodeId)
  "fetchUser": { "userId": "123", "userName": "John Doe" },
  "getToken": { "token": "abc123" },
  
  // Trigger data
  "_trigger": { "eventType": "user.created", "userId": "123" },
  
  // Workflow variables
  "_vars": { "apiKey": "secret-key", "baseUrl": "https://api.example.com" },
  
  // Built-in functions
  "_now": () -> System.currentTimeMillis(),
  "_uuid": () -> UUID.randomUUID().toString(),
  // ... other built-in functions
}
```

### 2. Build Output Context (cho Output Mapping)

Sau khi action execute, build output context với raw response:

```java
{
  // Previous context (cho backward compatibility)
  "fetchUser": { "userId": "123", "userName": "John Doe" },
  "_trigger": { "eventType": "user.created" },
  "_vars": { "apiKey": "secret-key" },
  
  // Raw action response
  "_response": {
    "statusCode": 200,
    "headers": { "Content-Type": "application/json" },
    "body": {
      "userId": "123",
      "name": "John Doe",
      "email": "john@example.com"
    }
  },
  
  // Built-in functions (vẫn available)
  "_now": () -> System.currentTimeMillis(),
  // ...
}
```

### 3. Scan và Extract MVEL Expressions

System scan tất cả strings, objects, arrays để tìm MVEL expressions:
- Pattern: `@\{([^}]+)\}`
- Extract expression content
- Build evaluation map

### 4. Evaluate MVEL Expressions

- Evaluate mỗi expression với execution context
- Handle errors gracefully với detailed messages
- Replace expression với evaluated value
- Recursively evaluate nested structures

## Example Evaluation

### Config Evaluation Example

**Execution Context**:
```json
{
  "fetchUser": {
    "userId": "123",
    "userName": "John Doe"
  },
  "getToken": {
    "token": "abc123"
  },
  "_trigger": {
    "eventType": "user.created"
  },
  "_vars": {
    "apiKey": "secret-key",
    "baseUrl": "https://api.example.com"
  }
}
```

**Config Values** (với MVEL expressions):
```json
{
  "url": "@{_vars.baseUrl}/users/@{fetchUser.userId}",
  "method": "POST",
  "headers": {
    "Authorization": "Bearer @{getToken.token}"
  },
  "body": {
    "userId": "@{fetchUser.userId}",
    "name": "@{fetchUser.userName}"
  }
}
```

**Resolved Config** (sau khi evaluate MVEL):
```json
{
  "url": "https://api.example.com/users/123",
  "method": "POST",
  "headers": {
    "Authorization": "Bearer abc123"
  },
  "body": {
    "userId": "123",
    "name": "John Doe"
  }
}
```

### Output Mapping Example

**Raw Response** (từ API call):
```json
{
  "statusCode": 200,
  "headers": {
    "Content-Type": "application/json"
  },
  "body": {
    "id": "123",
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

**Output Mapping** (MVEL expressions):
```json
{
  "statusCode": "@{_response.statusCode}",
  "status": "@{_response.statusCode} >= 200 && @{_response.statusCode} < 300 ? 'success' : 'error'",
  "body": "@{_response.body}"
}
```

**Mapped Output**:
```json
{
  "statusCode": 200,
  "status": "success",
  "body": {
    "id": "123",
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

## Use Cases cho MVEL

### 1. Action Config Values
```json
{
  "url": "@{_vars.baseUrl}/users/@{fetchUser.userId}",
  "headers": {
    "Authorization": "Bearer @{getToken.token}"
  },
  "body": {
    "userId": "@{fetchUser.userId}",
    "timestamp": "@{_now()}"
  }
}
```

### 2. Email/Notification Templates
```json
{
  "subject": "Welcome @{user.firstName}!",
  "body": "Hello @{user.firstName} @{user.lastName}, your order @{order.id} has been confirmed. Total: $@{order.total}",
  "to": "@{user.email}"
}
```

### 3. Function Node Expressions
```json
{
  "expression": "@{user.firstName} + ' ' + @{user.lastName}",
  "outputField": "fullName"
}
```

### 4. Condition Expressions
```json
{
  "condition": "@{user.age} >= 18 && @{user.status} == 'active'"
}
```

### 5. Field Mappings
```json
{
  "source": "@{fetchUser.userId}",
  "target": "@{target.userId}",
  "transform": "@{source.email.toLowerCase()}"
}
```

### 6. Dynamic Paths và URLs
```json
{
  "filePath": "/data/@{userId}/@{_now()}.json",
  "kafkaTopic": "events-@{eventType}",
  "apiEndpoint": "@{_vars.baseUrl}/api/v1/@{resourceType}/@{resourceId}"
}
```

### 7. Output Mapping
```json
{
  "outputMapping": {
    "userId": "@{_response.body.user.id}",
    "fullName": "@{_response.body.user.firstName} + ' ' + @{_response.body.user.lastName}",
    "email": "@{_response.body.user.email}",
    "status": "@{_response.statusCode} >= 200 && @{_response.statusCode} < 300 ? 'success' : 'error'"
  }
}
```

## Migration từ Old Syntax

### Old Syntax (DEPRECATED)
- `${variable}` - Template string syntax
- `{{variable}}` - Mustache-like syntax
- Custom expression syntax

### New Syntax (MVEL)
- `@{variable}` - MVEL expression syntax
- **Tất cả old syntax sẽ được migrate sang MVEL**

## Decisions Made

1. **MVEL Expression Format**: `@{expression}` (not `${expression}`) để tránh conflict với template strings
2. **Unified System**: MVEL được sử dụng cho TẤT CẢ dynamic logic và placeholders
3. **MVEL Editor**: Text editor với autocomplete cho context variables (syntax highlighting optional)
4. **Backward Compatibility**: Old syntax sẽ được migrate/convert sang MVEL

## Related Documentation

- [Action Registry](./action-registry.md) - Action schema structure và configuration
- [Action Node Configuration](../technical/frontend/action-node-configuration.md) - Frontend implementation
- [Action Execution](../technical/backend/action-execution.md) - Backend implementation
- [Planning: Sprint 39](../planning/frontend/sprint-39.md) - ActionEditor Config Template Schema
- [Planning: Sprint 40](../planning/frontend/sprint-40.md) - PropertiesPanel MVEL Support
- [Planning: Sprint 27](../planning/backend/sprint-27.md) - Backend MVEL Evaluation
