# InvoiceMe Frontend

Next.js 14 frontend with TypeScript and Tailwind CSS.

## Development

```bash
# Install dependencies
bun install

# Run dev server
bun run dev

# Build for production
bun run build

# Run production build
bun run start

# Lint
bun run lint
```

## Access

Frontend: http://localhost:3000

## Structure

```
src/
├── app/              # Next.js App Router pages
├── components/       # React components
├── lib/              # Utilities (API client, etc.)
└── types/            # TypeScript type definitions
```

## Environment Variables

Copy `.env.example` to `.env.local` and configure:

```
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```
