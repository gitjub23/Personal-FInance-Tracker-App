import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        port: 8080,        // Set your desired port here
        strictPort: true,  // Optional: Crashes if 8080 is busy instead of auto-switching
    }
})