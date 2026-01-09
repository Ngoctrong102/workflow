import { lazy } from "react"
import { createBrowserRouter, RouterProvider, Navigate, Outlet } from "react-router-dom"
import { Layout } from "@/components/layout/Layout"
import { LazyPage } from "@/components/common/LazyPage"

// Lazy load pages for code splitting
const Dashboard = lazy(() => import("@/pages/Dashboard").then((module) => ({ default: module.default })))
const WorkflowList = lazy(() => import("@/pages/WorkflowList").then((module) => ({ default: module.default })))
const WorkflowBuilder = lazy(() => import("@/pages/WorkflowBuilder").then((module) => ({ default: module.default })))
const TemplateList = lazy(() => import("@/pages/TemplateList").then((module) => ({ default: module.default })))
const TemplateEditor = lazy(() => import("@/pages/TemplateEditor").then((module) => ({ default: module.default })))
const TemplateDetails = lazy(() => import("@/pages/TemplateDetails").then((module) => ({ default: module.default })))
const ChannelList = lazy(() => import("@/pages/ChannelList").then((module) => ({ default: module.default })))
const ChannelEditor = lazy(() => import("@/pages/ChannelEditor").then((module) => ({ default: module.default })))
const ChannelDetails = lazy(() => import("@/pages/ChannelDetails").then((module) => ({ default: module.default })))
const Analytics = lazy(() => import("@/pages/Analytics").then((module) => ({ default: module.default })))
const ExecutionList = lazy(() => import("@/pages/ExecutionList").then((module) => ({ default: module.default })))
const ExecutionDetails = lazy(() => import("@/pages/ExecutionDetails").then((module) => ({ default: module.default })))
const ABTestList = lazy(() => import("@/pages/ABTestList").then((module) => ({ default: module.default })))
const ABTestEditor = lazy(() => import("@/pages/ABTestEditor").then((module) => ({ default: module.default })))
const ABTestDetails = lazy(() => import("@/pages/ABTestDetails").then((module) => ({ default: module.default })))
const TemplateLibrary = lazy(() => import("@/pages/TemplateLibrary").then((module) => ({ default: module.default })))
const WorkflowWizard = lazy(() => import("@/pages/WorkflowWizard").then((module) => ({ default: module.default })))
const WorkflowDashboard = lazy(() => import("@/pages/WorkflowDashboard").then((module) => ({ default: module.default })))
const WorkflowReportConfig = lazy(() => import("@/pages/WorkflowReportConfig").then((module) => ({ default: module.default })))
const WorkflowReportSettings = lazy(() => import("@/pages/WorkflowReportSettings").then((module) => ({ default: module.default })))
const ObjectTypeList = lazy(() => import("@/pages/ObjectTypeList").then((module) => ({ default: module.default })))
const ObjectTypeEditor = lazy(() => import("@/pages/ObjectTypeEditor").then((module) => ({ default: module.default })))
const ExecutionVisualization = lazy(() => import("@/pages/ExecutionVisualization").then((module) => ({ default: module.default })))

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
        path: "templates",
        children: [
          {
            index: true,
            element: <LazyPage component={TemplateList} />,
          },
          {
            path: "new",
            element: <LazyPage component={TemplateEditor} />,
          },
          {
            path: ":id",
            element: <LazyPage component={TemplateEditor} />,
          },
        ],
      },
      {
        path: "channels",
        children: [
          {
            index: true,
            element: <LazyPage component={ChannelList} />,
          },
          {
            path: "new",
            element: <LazyPage component={ChannelEditor} />,
          },
          {
            path: ":id",
            element: <LazyPage component={ChannelEditor} />,
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
              path: "template-library",
              element: <LazyPage component={TemplateLibrary} />,
            },
            {
              path: "workflows/wizard",
              element: <LazyPage component={WorkflowWizard} />,
            },
            {
              path: "object-types",
              children: [
                {
                  index: true,
                  element: <LazyPage component={ObjectTypeList} />,
                },
                {
                  path: "new",
                  element: <LazyPage component={ObjectTypeEditor} />,
                },
                {
                  path: ":id",
                  element: <LazyPage component={ObjectTypeEditor} />,
                },
              ],
            },
          ],
        },
      ])

export function AppRouter() {
  return <RouterProvider router={router} />
}
