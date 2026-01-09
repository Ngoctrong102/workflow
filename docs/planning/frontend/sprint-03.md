# Sprint 03: UI Components & Design System

## Goal
Implement base UI components and design system, ensuring compliance with `docs/technical/frontend/design-system.json` and `docs/technical/frontend/components.md`.

## Phase
Foundation

## Complexity
Medium

## Dependencies
Sprint 01

## Compliance Check

### Before Starting
1. ✅ Read `@import(technical/frontend/design-system.json)` - Understand design system
2. ✅ Read `@import(technical/frontend/components.md)` - Understand component specifications
3. ✅ Check existing components for violations
4. ✅ Fix any violations immediately before proceeding

### Existing Code Verification
- [ ] Verify components match design system
- [ ] Verify components match specifications
- [ ] Verify Shadcn/ui components are used correctly
- [ ] Fix any violations found

## Tasks

### Base UI Components (Shadcn/ui)
- [ ] Verify/Install Shadcn/ui base components:
  - [ ] Button component
  - [ ] Input component
  - [ ] Select component
  - [ ] Textarea component
  - [ ] Checkbox component
  - [ ] Radio Group component
  - [ ] Switch component
  - [ ] Label component
  - [ ] Card component
  - [ ] Dialog component
  - [ ] Sheet component (sidebar)
  - [ ] Table component
  - [ ] Badge component
  - [ ] Avatar component
  - [ ] Dropdown Menu component
  - [ ] Toast component (Sonner or similar)
  - [ ] Loading/Spinner component
  - [ ] Skeleton component
- [ ] Customize components with design system colors
- [ ] Create component variants matching design system

### Layout Components
- [ ] Create `Layout` component:
  - [ ] Header section
  - [ ] Sidebar section
  - [ ] Main content area
  - [ ] Footer section (if needed)
- [ ] Create `Header` component:
  - [ ] Logo/branding
  - [ ] Navigation menu
  - [ ] User menu (if authentication)
  - [ ] Notifications (if needed)
- [ ] Create `Sidebar` component:
  - [ ] Navigation links
  - [ ] Collapsible sections
  - [ ] Active state highlighting
- [ ] Create `Breadcrumbs` component
- [ ] Create `PageHeader` component (title, actions, breadcrumbs)

### Form Components
- [ ] Create `Form` wrapper component (using React Hook Form)
- [ ] Create `FormField` component
- [ ] Create `FormLabel` component
- [ ] Create `FormError` component
- [ ] Create `FormSelect` component
- [ ] Create `FormTextarea` component
- [ ] Create `FormCheckbox` component
- [ ] Create `FormRadio` component

### Data Display Components
- [ ] Create `DataTable` component (enhanced table)
- [ ] Create `Pagination` component
- [ ] Create `EmptyState` component
- [ ] Create `LoadingState` component
- [ ] Create `ErrorState` component

### Feedback Components
- [ ] Create `Toast` provider and hook
- [ ] Create `Alert` component (success, error, warning, info)
- [ ] Create `ConfirmDialog` component
- [ ] Create `LoadingOverlay` component

### Design System Application
- [ ] Apply design system colors to Tailwind config
- [ ] Apply typography system
- [ ] Apply spacing scale
- [ ] Apply component variants
- [ ] Create design tokens file (if needed)

## Deliverables

- ✅ All base UI components implemented
- ✅ Design system applied consistently
- ✅ Layout components ready
- ✅ Form components ready
- ✅ Components match design system specifications

## Technical Details

### Component Structure

```
src/components/
├── ui/                    # Shadcn/ui base components
│   ├── button.tsx
│   ├── input.tsx
│   ├── card.tsx
│   └── ...
├── layout/                # Layout components
│   ├── Layout.tsx
│   ├── Header.tsx
│   ├── Sidebar.tsx
│   └── Breadcrumbs.tsx
├── forms/                 # Form components
│   ├── Form.tsx
│   ├── FormField.tsx
│   └── ...
├── data-display/          # Data display components
│   ├── DataTable.tsx
│   ├── Pagination.tsx
│   └── ...
└── feedback/              # Feedback components
    ├── Toast.tsx
    ├── Alert.tsx
    └── ...
```

### Design System Colors (from design-system.json)

Apply colors to Tailwind config:
```javascript
// tailwind.config.js
theme: {
  extend: {
    colors: {
      primary: {
        DEFAULT: '#...',
        light: '#...',
        dark: '#...',
      },
      secondary: {
        // ...
      },
      // ... other colors from design-system.json
    },
  },
}
```

### Component Variants

Use class-variance-authority (CVA) for component variants:
```typescript
// components/ui/button.tsx
import { cva, type VariantProps } from 'class-variance-authority';

const buttonVariants = cva(
  'inline-flex items-center justify-center rounded-md text-sm font-medium',
  {
    variants: {
      variant: {
        default: 'bg-primary text-primary-foreground',
        destructive: 'bg-destructive text-destructive-foreground',
        outline: 'border border-input bg-background',
        // ... other variants
      },
      size: {
        default: 'h-10 px-4 py-2',
        sm: 'h-9 rounded-md px-3',
        lg: 'h-11 rounded-md px-8',
        // ... other sizes
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'default',
    },
  }
);
```

### Form Component Example

```typescript
// components/forms/FormField.tsx
import { useFormContext } from 'react-hook-form';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { FormError } from './FormError';

interface FormFieldProps {
  name: string;
  label: string;
  type?: string;
  required?: boolean;
}

export const FormField = ({ name, label, type = 'text', required }: FormFieldProps) => {
  const { register, formState: { errors } } = useFormContext();
  
  return (
    <div className="space-y-2">
      <Label htmlFor={name}>
        {label}
        {required && <span className="text-destructive">*</span>}
      </Label>
      <Input
        id={name}
        type={type}
        {...register(name, { required })}
      />
      {errors[name] && <FormError message={errors[name]?.message} />}
    </div>
  );
};
```

## Compliance Verification

### After Implementation
- [ ] Verify all components match design system (`@import(technical/frontend/design-system.json)`)
- [ ] Verify components match specifications (`@import(technical/frontend/components.md)`)
- [ ] Verify colors are applied correctly
- [ ] Verify typography is applied correctly
- [ ] Verify spacing is applied correctly
- [ ] Test all component variants
- [ ] Test responsive behavior
- [ ] Verify accessibility (ARIA labels, keyboard navigation)
- [ ] Verify no violations of design system

## Related Documentation

- `@import(technical/frontend/design-system.json)` ⚠️ **MUST MATCH**
- `@import(technical/frontend/components.md)` ⚠️ **MUST MATCH**
- `@import(technical/frontend/overview.md)`
