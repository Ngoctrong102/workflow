# Code Quality Rules

## Code Quality Standards

- Follow the patterns and architecture defined in documentation
- Ensure all implementations match the API specifications
- Maintain database schema integrity
- Implement security measures as specified
- Write code that aligns with user flows
- Follow technical specifications for frontend and backend

## Frontend Code Quality

### Component Standards
- Use Shadcn/ui components as base, customize to match design system
- Follow TypeScript best practices
- Implement proper error handling
- Use React hooks appropriately
- Follow accessibility guidelines (WCAG 2.1 Level AA)

### Styling Standards
- Use Tailwind CSS utility classes
- Follow design system color palette and typography
- Ensure responsive design (mobile-first approach)
- Maintain consistent spacing and sizing

### Performance Standards
- Lazy load images
- Code splitting by route
- Memoization for expensive renders
- Optimize bundle size

## Backend Code Quality

### Architecture Standards
- Follow layered architecture (Controller → Service → Repository)
- Use dependency injection
- Implement proper exception handling
- Follow RESTful API conventions

### Code Organization
- Follow package structure defined in `docs/technical/backend/project-structure.md`
- Use proper naming conventions (camelCase for methods, PascalCase for classes)
- Implement proper logging
- Use appropriate design patterns

### Security Standards
- Validate all inputs
- Use JWT for authentication
- Implement proper authorization checks
- Follow security guidelines in `docs/security/`

## Testing Standards

- Write unit tests for business logic
- Write integration tests for API endpoints
- Test error cases and edge cases
- Maintain test coverage above 70%

## Documentation Standards

- Document complex logic with comments
- Keep README files updated
- Document API endpoints
- Update technical documentation when making changes

