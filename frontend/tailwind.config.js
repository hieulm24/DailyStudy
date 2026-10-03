/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      borderRadius: {
        'none': '0',
        'sm': '4px',
        DEFAULT: '6px',
        'md': '6px',
        'lg': '7px',
        'full': '9999px',
      },
      colors: {
        brand: {
          50: '#eff6ff',
          100: '#dbeafe',
          200: '#bfdbfe',
          300: '#93c5fd',
          400: '#60a5fa',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
          800: '#1e40af',
          900: '#1e3a8a',
        },
        surface: {
          50: '#f8fafc',
          100: '#f1f5f9',
          200: '#e2e8f0',
          300: '#cbd5e1',
          400: '#94a3b8',
          500: '#64748b',
          600: '#475569',
          700: '#334155',
          800: '#1e293b',
          900: '#0f172a',
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'sans-serif'],
      },
      fontSize: {
        '2xs': ['0.75rem', { lineHeight: '1.05rem' }], // 12px
        'xs': ['0.84rem', { lineHeight: '1.25rem' }],  // ~13.5px (tăng từ 12px)
        'sm': ['0.925rem', { lineHeight: '1.4rem' }],  // ~14.8px (tăng từ 14px)
        'base': ['1.025rem', { lineHeight: '1.55rem' }], // ~16.4px
        'lg': ['1.15rem', { lineHeight: '1.75rem' }],
        'xl': ['1.3rem', { lineHeight: '1.85rem' }],
        '2xl': ['1.6rem', { lineHeight: '2.1rem' }],
        '3xl': ['1.95rem', { lineHeight: '2.35rem' }],
      }
    },
  },
  plugins: [],
}
