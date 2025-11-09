/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx,mdx}',
    './src/components/**/*.{js,ts,jsx,tsx,mdx}',
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f0f7f4',
          100: '#dceee7',
          200: '#b9ddd0',
          300: '#96ccb8',
          400: '#73bba0',
          500: '#5A8F7B',
          600: '#4A7C6A',
          700: '#3d6757',
          800: '#2f5244',
          900: '#213d31',
        },
        sage: {
          50: '#F4F7F5',
          100: '#E8EEEB',
          200: '#D1DDD7',
        },
      },
      borderRadius: {
        'card': '12px',
      },
      boxShadow: {
        'card': '0 1px 3px rgba(0, 0, 0, 0.05)',
        'card-hover': '0 4px 12px rgba(0, 0, 0, 0.08)',
      },
    },
  },
  plugins: [],
}
