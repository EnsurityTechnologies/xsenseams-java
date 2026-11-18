import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
    // ui1.localhost and ui2.localhost resolve to 127.0.0.1. Listen there so those
    // page origins can load the dev app. The browser Origin is the page origin.
    host: '127.0.0.1',
    allowedHosts: ['ui1.localhost', 'ui2.localhost'],
  },
});
