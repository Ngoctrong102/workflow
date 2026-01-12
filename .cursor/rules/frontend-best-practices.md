# Frontend Best Practices & Principles

## 🎯 Core Principles

### 1. User-Centric Design
- **Always prioritize user experience** - Every decision should improve user experience
- **Progressive enhancement** - Build for the lowest common denominator, enhance for capable devices
- **Accessibility first** - WCAG 2.1 Level AA compliance is mandatory, not optional
- **Performance matters** - Users expect fast, responsive interfaces

### 2. Code Quality Principles
- **Type safety** - Use TypeScript strictly, avoid `any` unless absolutely necessary
- **Single Responsibility** - Each component/function should do one thing well
- **DRY (Don't Repeat Yourself)** - Reuse code through components, hooks, and utilities
- **KISS (Keep It Simple, Stupid)** - Prefer simple solutions over complex ones
- **YAGNI (You Aren't Gonna Need It)** - Don't add functionality until it's needed

### 3. React Best Practices

#### Component Design
- **Functional components only** - Use function components with hooks, never class components
- **Component composition** - Prefer composition over inheritance
- **Small, focused components** - Break down large components into smaller, reusable pieces
- **Props interface** - Always define explicit TypeScript interfaces for props
- **Default props** - Use default parameters or defaultProps for optional props

#### State Management
- **Local state first** - Use `useState` for component-specific state
- **Lift state up** - Move shared state to the nearest common ancestor
- **Context for global state** - Use React Context for truly global state (theme, auth, etc.)
- **External state management** - Use Zustand/Redux only when local state becomes unwieldy
- **Derived state** - Compute derived values in render, don't store them in state

#### Hooks Best Practices
- **Rules of Hooks** - Always call hooks at the top level, never in loops/conditions
- **Custom hooks** - Extract reusable logic into custom hooks
- **Dependency arrays** - Always include all dependencies in useEffect/useMemo/useCallback
- **Cleanup functions** - Always return cleanup functions from useEffect when needed
- **Memoization** - Use `useMemo` and `useCallback` judiciously (only when needed for performance)

#### Performance Optimization
- **React.memo** - Use for expensive components that re-render frequently
- **Code splitting** - Use React.lazy() and Suspense for route-based code splitting
- **Virtual scrolling** - Use for long lists (react-window, react-virtualized)
- **Debounce/throttle** - Use for expensive operations (search, resize, scroll)
- **Image optimization** - Lazy load images, use appropriate formats (WebP, AVIF)

### 4. TypeScript Best Practices

#### Type Definitions
- **Explicit types** - Prefer explicit types over inference for public APIs
- **Interface over type** - Use `interface` for object shapes, `type` for unions/intersections
- **Generic types** - Use generics for reusable, type-safe utilities
- **Utility types** - Leverage TypeScript utility types (Partial, Pick, Omit, etc.)
- **Discriminated unions** - Use for type-safe state machines and variants

#### Type Safety
- **Strict mode** - Always use TypeScript strict mode
- **No implicit any** - Never use `any` without explicit justification
- **Type guards** - Use type guards for runtime type checking
- **Assertions sparingly** - Use type assertions only when absolutely necessary
- **Null safety** - Always handle null/undefined explicitly

### 5. Form Handling Best Practices

#### React Hook Form
- **Controlled components** - Use `register()` for simple inputs
- **Controller for complex** - Use `Controller` for custom components (Select, DatePicker, etc.)
- **Validation** - Use `rules` prop for validation, not custom onChange handlers
- **Error handling** - Display errors from `formState.errors`
- **Nested paths** - Use dot notation for nested fields: `register("user.name")`
- **Watch values** - Use `watch()` for dependent fields, not for controlled inputs
- **setValue** - Use `setValue()` for programmatic updates, not direct state manipulation

#### Form State Management
- **Default values** - Always provide `defaultValues` in `useForm()`
- **Reset form** - Use `reset()` to reset form to default values
- **Dirty tracking** - Use `formState.isDirty` to track unsaved changes
- **Submit handler** - Use `handleSubmit()` wrapper, not direct form submission
- **Async validation** - Use `validate` function for async validation

#### Common Pitfalls to Avoid
- ❌ **Don't use `watch()` for controlled inputs** - Use `register()` or `Controller`
- ❌ **Don't access nested paths with optional chaining in `watch()`** - Access object directly
- ❌ **Don't mix controlled and uncontrolled** - Be consistent
- ❌ **Don't forget `shouldDirty: true`** - When using `setValue()` programmatically
- ❌ **Don't validate in onChange** - Use `rules` prop instead

### 6. API Integration Best Practices

#### React Query (TanStack Query)
- **Query keys** - Use consistent, hierarchical query keys: `['users', userId]`
- **Stale time** - Set appropriate `staleTime` based on data freshness needs
- **Cache time** - Use `gcTime` (formerly `cacheTime`) to control cache retention
- **Refetch strategies** - Use `refetchOnWindowFocus`, `refetchOnMount` appropriately
- **Error handling** - Handle errors in `onError` callback or component error boundaries
- **Loading states** - Use `isLoading` for initial load, `isFetching` for background refetch

#### Data Fetching Patterns
- **Optimistic updates** - Use `onMutate` for optimistic UI updates
- **Invalidation** - Invalidate related queries after mutations
- **Parallel queries** - Use `useQueries` for parallel data fetching
- **Dependent queries** - Use `enabled` option for dependent queries
- **Pagination** - Use `useInfiniteQuery` for infinite scroll/pagination

#### Error Handling
- **Error boundaries** - Use React Error Boundaries for component-level errors
- **API errors** - Handle API errors consistently (network, 4xx, 5xx)
- **User feedback** - Always show user-friendly error messages
- **Retry logic** - Implement retry logic for transient failures
- **Error logging** - Log errors to monitoring service (Sentry, etc.)

### 7. Styling Best Practices

#### Tailwind CSS
- **Utility-first** - Use Tailwind utilities, avoid custom CSS when possible
- **Responsive design** - Use responsive prefixes (sm:, md:, lg:, xl:)
- **Dark mode** - Use `dark:` prefix for dark mode styles
- **Custom values** - Use arbitrary values `[value]` sparingly
- **Component extraction** - Extract repeated patterns into components, not CSS classes

#### Component Styling
- **Shadcn/ui base** - Start with Shadcn/ui components, customize as needed
- **Consistent spacing** - Use Tailwind spacing scale consistently
- **Color system** - Use design system colors, not arbitrary colors
- **Typography** - Use design system typography scale
- **Accessibility** - Ensure sufficient color contrast (WCAG AA)

### 8. File Organization Principles

#### Directory Structure
- **Feature-based** - Organize by feature, not by file type
- **Co-location** - Keep related files together (component + styles + tests)
- **Barrel exports** - Use `index.ts` for clean imports
- **Clear naming** - Use descriptive, consistent file names

#### Component Organization
```
feature/
├── components/        # Feature-specific components
├── hooks/            # Feature-specific hooks
├── services/         # Feature-specific services
├── types/            # Feature-specific types
└── utils/            # Feature-specific utilities
```

#### Import Organization
- **External first** - React, third-party libraries
- **Internal absolute** - `@/components`, `@/hooks`, etc.
- **Relative imports** - Only for closely related files
- **Group imports** - Separate groups with blank lines

### 9. Testing Principles

#### Unit Testing
- **Test behavior, not implementation** - Test what users see/do
- **Isolation** - Each test should be independent
- **Arrange-Act-Assert** - Follow AAA pattern
- **Edge cases** - Test edge cases and error scenarios
- **Mocking** - Mock external dependencies (API, services)

#### Component Testing
- **User interactions** - Test user interactions, not internal state
- **Accessibility** - Test with screen readers, keyboard navigation
- **Visual regression** - Use visual regression testing for UI components
- **Snapshot testing** - Use sparingly, only for stable components

### 10. Performance Best Practices

#### Bundle Size
- **Code splitting** - Split code by route and feature
- **Tree shaking** - Ensure unused code is eliminated
- **Dynamic imports** - Use dynamic imports for heavy dependencies
- **Bundle analysis** - Regularly analyze bundle size

#### Runtime Performance
- **Memoization** - Memoize expensive computations
- **Virtual scrolling** - Use for long lists
- **Image optimization** - Lazy load, use appropriate formats
- **Debounce/throttle** - Use for expensive operations
- **Web Workers** - Use for CPU-intensive tasks

#### Network Performance
- **Request batching** - Batch multiple requests when possible
- **Caching** - Use appropriate caching strategies
- **Prefetching** - Prefetch data for likely next actions
- **Compression** - Ensure assets are compressed (gzip, brotli)

### 11. Accessibility (A11y) Best Practices

#### Semantic HTML
- **Use semantic elements** - `<button>`, `<nav>`, `<main>`, etc.
- **Headings hierarchy** - Use h1-h6 in proper order
- **Landmarks** - Use ARIA landmarks for page structure
- **Labels** - Always label form inputs

#### Keyboard Navigation
- **Focus management** - Ensure all interactive elements are keyboard accessible
- **Focus indicators** - Visible focus indicators for keyboard users
- **Tab order** - Logical tab order
- **Skip links** - Provide skip links for main content

#### Screen Readers
- **ARIA attributes** - Use ARIA when semantic HTML isn't enough
- **Live regions** - Use `aria-live` for dynamic content updates
- **Descriptions** - Provide `aria-describedby` for complex inputs
- **States** - Announce state changes (loading, errors, success)

#### Color & Contrast
- **Color contrast** - Minimum 4.5:1 for text, 3:1 for UI components
- **Color independence** - Don't rely solely on color to convey information
- **Focus indicators** - High contrast focus indicators

### 12. Security Best Practices

#### Input Validation
- **Client-side validation** - Validate all user inputs
- **Sanitization** - Sanitize user inputs before rendering
- **XSS prevention** - Never use `dangerouslySetInnerHTML` without sanitization
- **CSRF protection** - Include CSRF tokens in state-changing requests

#### Authentication
- **Token storage** - Store tokens securely (httpOnly cookies preferred)
- **Token refresh** - Implement token refresh mechanism
- **Session management** - Handle session expiration gracefully
- **Logout** - Clear all auth data on logout

### 13. Error Handling Patterns

#### Error Boundaries
- **Component boundaries** - Use Error Boundaries for component-level errors
- **Fallback UI** - Provide meaningful fallback UI
- **Error logging** - Log errors to monitoring service
- **User communication** - Show user-friendly error messages

#### API Error Handling
- **Consistent format** - Handle errors in consistent format
- **User feedback** - Always provide user feedback for errors
- **Retry logic** - Implement retry for transient failures
- **Error types** - Distinguish between network, client, and server errors

### 14. Code Review Checklist

#### Before Submitting
- [ ] Code follows TypeScript strict mode
- [ ] All components have proper TypeScript types
- [ ] No `any` types without justification
- [ ] All forms use React Hook Form correctly
- [ ] All API calls use React Query
- [ ] Error handling is implemented
- [ ] Loading states are shown
- [ ] Accessibility requirements met
- [ ] Responsive design works on mobile/tablet/desktop
- [ ] No console.logs or debug code
- [ ] No unused imports or variables
- [ ] Code is properly formatted (Prettier)
- [ ] Linter passes without errors

### 15. Common Anti-Patterns to Avoid

#### React Anti-Patterns
- ❌ **Mutating state directly** - Always use setState/useState setter
- ❌ **Using index as key** - Use stable, unique keys
- ❌ **Props drilling** - Use Context or state management for deep props
- ❌ **Side effects in render** - Use useEffect for side effects
- ❌ **Creating objects in render** - Memoize objects passed as props

#### Form Anti-Patterns
- ❌ **Using watch() for controlled inputs** - Use register() or Controller
- ❌ **Nested path with optional chaining in watch()** - Access object directly
- ❌ **Validating in onChange** - Use rules prop
- ❌ **Mixing controlled/uncontrolled** - Be consistent
- ❌ **Forgetting shouldDirty** - Include when using setValue()

#### Performance Anti-Patterns
- ❌ **Unnecessary re-renders** - Memoize expensive components
- ❌ **Large bundle size** - Code split and lazy load
- ❌ **Blocking renders** - Use Suspense and lazy loading
- ❌ **Memory leaks** - Clean up subscriptions and timers

### 16. Documentation Standards

#### Code Comments
- **Why, not what** - Explain why code exists, not what it does
- **Complex logic** - Comment complex algorithms and business logic
- **TODOs** - Use TODO comments with context and owner
- **JSDoc** - Use JSDoc for public APIs

#### Component Documentation
- **Props interface** - Document all props with JSDoc
- **Usage examples** - Provide usage examples in component files
- **Storybook** - Use Storybook for component documentation
- **README** - Maintain README for complex features

### 17. Git & Version Control

#### Commit Messages
- **Conventional commits** - Follow conventional commit format
- **Clear messages** - Write clear, descriptive commit messages
- **Atomic commits** - One logical change per commit
- **Ticket references** - Include ticket ID in commit message

#### Branch Strategy
- **Feature branches** - Create feature branches from main
- **Branch naming** - Use descriptive branch names
- **Keep branches small** - Don't let branches diverge too far
- **Regular merges** - Merge main into feature branch regularly

### 18. Continuous Improvement

#### Code Quality
- **Regular refactoring** - Refactor code regularly
- **Remove dead code** - Remove unused code and dependencies
- **Update dependencies** - Keep dependencies up to date
- **Performance monitoring** - Monitor and optimize performance

#### Learning
- **Stay updated** - Keep up with React and ecosystem updates
- **Code reviews** - Learn from code reviews
- **Best practices** - Follow React and TypeScript best practices
- **Community** - Engage with React community

## 📋 Quick Reference Checklist

### Before Writing Code
- [ ] Read feature documentation
- [ ] Understand API contract
- [ ] Check design system
- [ ] Review wireframes
- [ ] Plan component structure

### While Writing Code
- [ ] Use TypeScript strictly
- [ ] Follow React best practices
- [ ] Use React Hook Form correctly
- [ ] Handle errors and loading states
- [ ] Ensure accessibility
- [ ] Make it responsive

### After Writing Code
- [ ] Test on multiple devices
- [ ] Check accessibility
- [ ] Verify error handling
- [ ] Review performance
- [ ] Clean up code
- [ ] Update documentation

## 🎯 Remember

> **"Code is read more often than it's written. Write code for humans, not just for machines."**

- Write clear, self-documenting code
- Prioritize maintainability over cleverness
- Think about the next developer who will read your code
- Always consider the user experience
- Performance and accessibility are not optional

