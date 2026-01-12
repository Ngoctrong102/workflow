import { lazy } from "react"
import { createBrowserRouter, RouterProvider, Navigate, Outlet } from "react-router-dom"
import { Layout } from "@/components/layout/Layout"
import { LazyPage } from "@/components/common/LazyPage"

// Lazy load pages for code splitting
const Dashboard = lazy(() => import("@/pages/Dashboard").then((module) => ({ default: module.default })))
const WorkflowList = lazy(() => import("@/pages/WorkflowList").then((module) => ({ default: module.default })))
const WorkflowBuilder = lazy(() => import("@/pages/WorkflowBuilder").then((module) => ({ default: module.default })))
const Analytics = lazy(() => import("@/pages/Analytics").then((module) => ({ default: module.default })))
const ExecutionList = lazy(() => import("@/pages/ExecutionList").then((module) => ({ default: module.default })))
const ExecutionDetails = lazy(() => import("@/pages/ExecutionDetails").then((module) => ({ default: module.default })))
const ABTestList = lazy(() => import("@/pages/ABTestList").then((module) => ({ default: module.default })))
const ABTestEditor = lazy(() => import("@/pages/ABTestEditor").then((module) => ({ default: module.default })))
const WorkflowWizard = lazy(() => import("@/pages/WorkflowWizard").then((module) => ({ default: module.default })))
const WorkflowDashboard = lazy(() => import("@/pages/WorkflowDashboard").then((module) => ({ default: module.default })))
const WorkflowReportConfig = lazy(() => import("@/pages/WorkflowReportConfig").then((module) => ({ default: module.default })))
const WorkflowReportSettings = lazy(() => import("@/pages/WorkflowReportSettings").then((module) => ({ default: module.default })))
const ExecutionVisualization = lazy(() => import("@/pages/ExecutionVisualization").then((module) => ({ default: module.default })))
const TriggerList = lazy(() => import("@/pages/TriggerList").then((module) => ({ default: module.default })))
const TriggerEditor = lazy(() => import("@/pages/TriggerEditor").then((module) => ({ default: module.default })))
const TriggerRegistryList = lazy(() => import("@/pages/TriggerRegistryList").then((module) => ({ default: module.default })))
const TriggerRegistryEditor = lazy(() => import("@/pages/TriggerRegistryEditor").then((module) => ({ default: module.default })))
const ActionList = lazy(() => import("@/pages/ActionList").then((module) => ({ default: module.default })))
const ActionEditor = lazy(() => import("@/pages/ActionEditor").then((module) => ({ default: module.default })))

export const router = createBrowserRouter([
  {
    path: "/",
    element: (
      <Layout>
        <Outlet />
      </Layout>
    ),
    children: [
      {
        index: true,
        element: <Navigate to="/dashboard" replace />,
      },
      {
        path: "dashboard",
        element: <LazyPage component={Dashboard} />,
      },
      {
        path: "workflows",
        children: [
          {
            index: true,
            element: <LazyPage component={WorkflowList} />,
          },
          {
            path: "new",
            element: <LazyPage component={WorkflowBuilder} />,
          },
          {
            path: ":id",
            element: <LazyPage component={WorkflowBuilder} />,
          },
          {
            path: ":id/dashboard",
            element: <LazyPage component={WorkflowDashboard} />,
          },
          {
            path: ":id/report",
            element: <LazyPage component={WorkflowReportConfig} />,
          },
          {
            path: ":id/report/settings",
            element: <LazyPage component={WorkflowReportSettings} />,
          },
        ],
      },
            {
              path: "analytics",
              element: <LazyPage component={Analytics} />,
            },
            {
              path: "executions",
              children: [
                {
                  index: true,
                  element: <LazyPage component={ExecutionList} />,
                },
                {
                  path: ":id",
                  element: <LazyPage component={ExecutionDetails} />,
                },
                {
                  path: ":id/visualize",
                  element: <LazyPage component={ExecutionVisualization} />,
                },
              ],
            },
            {
              path: "ab-tests",
              children: [
                {
                  index: true,
                  element: <LazyPage component={ABTestList} />,
                },
                {
                  path: "new",
                  element: <LazyPage component={ABTestEditor} />,
                },
                {
                  path: ":id",
                  element: <LazyPage component={ABTestEditor} />,
                },
              ],
            },
            {
              path: "workflows/wizard",
              element: <LazyPage component={WorkflowWizard} />,
            },
            {
              path: "triggers",
              children: [
                {
                  index: true,
                  element: <LazyPage component={TriggerList} />,
                },
                {
                  path: "new",
                  element: <LazyPage component={TriggerEditor} />,
                },
                {
                  path: ":id",
                  element: <LazyPage component={TriggerEditor} />,
                },
                {
                  path: ":id/edit",
                  element: <LazyPage component={TriggerEditor} />,
                },
                {
                  path: "workflow/:workflowId/new",
                  element: <LazyPage component={TriggerEditor} />,
                },
                {
                  path: "workflow/:workflowId/new/:type",
                  element: <LazyPage component={TriggerEditor} />,
                },
              ],
            },
            {
              path: "trigger-registry",
              children: [
                {
                  index: true,
                  element: <LazyPage component={TriggerRegistryList} />,
                },
                {
                  path: "new",
                  element: <LazyPage component={TriggerRegistryEditor} />,
                },
                {
                  path: ":id",
                  element: <LazyPage component={TriggerRegistryEditor} />,
                },
              ],
            },
            {
              path: "actions",
              children: [
                {
                  index: true,
                  element: <LazyPage component={ActionList} />,
                },
                {
                  path: "new",
                  element: <LazyPage component={ActionEditor} />,
                },
                {
                  path: ":id",
                  element: <LazyPage component={ActionEditor} />,
                },
                {
                  path: ":id/edit",
                  element: <LazyPage component={ActionEditor} />,
                },
              ],
            },
          ],
        },
      ])

export function AppRouter() {
  return <RouterProvider router={router} />
}
