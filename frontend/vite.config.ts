import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  build: {
    // Optimize chunk splitting
    rollupOptions: {
      output: {
        manualChunks: (id) => {
          // Split vendor chunks for better caching
          if (id.includes('node_modules')) {
            // React core
            if (id.includes('react') || id.includes('react-dom') || id.includes('react-router')) {
              return 'react-vendor'
            }
            // UI libraries
            if (id.includes('@radix-ui')) {
              return 'ui-vendor'
            }
            // Charts
            if (id.includes('recharts')) {
              return 'chart-vendor'
            }
            // Flow
            if (id.includes('reactflow')) {
              return 'flow-vendor'
            }
            // Forms
            if (id.includes('react-hook-form')) {
              return 'form-vendor'
            }
            // Query
            if (id.includes('@tanstack/react-query')) {
              return 'query-vendor'
            }
            // Utils
            if (id.includes('axios') || id.includes('date-fns') || id.includes('zustand')) {
              return 'utils-vendor'
            }
            // Other node_modules
            return 'vendor'
          }
        },
        // Optimize chunk file names
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js',
        assetFileNames: 'assets/[ext]/[name]-[hash].[ext]',
      },
      onwarn(warning, warn) {
        // Suppress certain warnings
        if (warning.code === 'UNUSED_EXTERNAL_IMPORT') return
        warn(warning)
      },
    },
    // Optimize bundle size
    chunkSizeWarningLimit: 1000,
    // Enable source maps for production debugging (optional)
    sourcemap: false,
    // Minify with esbuild for fast builds
    minify: 'esbuild',
    // Target modern browsers for smaller bundle
    target: 'esnext',
    // Enable CSS code splitting
    cssCodeSplit: true,
    // Report compressed size
    reportCompressedSize: true,
  },
  // Optimize dependencies
  optimizeDeps: {
    include: [
      'react',
      'react-dom',
      'react-router-dom',
      '@tanstack/react-query',
      'axios',
    ],
    exclude: ['html-encoding-sniffer'],
  },
})
