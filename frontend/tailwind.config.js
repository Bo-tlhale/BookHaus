/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        cream: { 50: '#fffdf8', 100: '#faf6ed', 200: '#eee5d6' },
        lavender: { 50: '#f7f4fb', 100: '#eee8f7', 200: '#dcd2ec', 300: '#c5b4df', 400: '#aa95c7', 500: '#8d79ad', 600: '#74618f', 700: '#5c4b75' },
        skybook: { 50: '#f2f8fb', 100: '#e2f0f7', 200: '#c5e0ec', 400: '#82b7ca', 600: '#4e8298' },
        wood: { 100: '#eadbcb', 300: '#c9a98b', 500: '#9b7457', 700: '#684b38', 900: '#3f3029' },
        leaf: { 100: '#e4eee3', 300: '#afc7ad', 500: '#789575', 700: '#50684e' },
        ink: '#39343b',
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif'],
        display: ['Georgia', 'Cambria', 'Times New Roman', 'serif'],
      },
      boxShadow: {
        soft: '0 16px 40px -24px rgba(80, 65, 93, 0.35)',
        card: '0 10px 28px -20px rgba(70, 53, 77, 0.38)',
      },
    },
  },
  plugins: [],
}
