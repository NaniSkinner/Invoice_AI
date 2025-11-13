# AI Chat Assistant - Architecture Diagram

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         FRONTEND (Next.js)                          │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │                      RootLayout                            │    │
│  │  ┌──────────────────────────────────────────────────┐     │    │
│  │  │              AuthProvider                        │     │    │
│  │  │  ┌────────────────────────────────────────┐     │     │    │
│  │  │  │          ChatAssistant                │     │     │    │
│  │  │  │  (Only shown when authenticated)      │     │     │    │
│  │  │  │                                        │     │     │    │
│  │  │  │  ┌──────────────┐  ┌──────────────┐  │     │     │    │
│  │  │  │  │ ChatBubble   │  │ ChatWindow   │  │     │     │    │
│  │  │  │  │              │  │              │  │     │     │    │
│  │  │  │  │ - Fixed pos  │  │ - Messages   │  │     │     │    │
│  │  │  │  │ - Toggle btn │  │ - Input      │  │     │     │    │
│  │  │  │  │ - Badge      │  │ - Suggestions│  │     │     │    │
│  │  │  │  └──────┬───────┘  └──────┬───────┘  │     │     │    │
│  │  │  └─────────┼──────────────────┼──────────┘     │     │    │
│  │  └────────────┼──────────────────┼────────────────┘     │    │
│  └───────────────┼──────────────────┼──────────────────────┘    │
│                  │                  │                            │
│                  └────────┬─────────┘                            │
│                           │                                      │
│                  ┌────────▼─────────┐                            │
│                  │   chatStore.ts   │  (Zustand State)          │
│                  ├──────────────────┤                            │
│                  │ - messages       │                            │
│                  │ - isOpen         │                            │
│                  │ - isLoading      │                            │
│                  │ - conversationId │                            │
│                  │ - actions        │                            │
│                  └────────┬─────────┘                            │
│                           │                                      │
│                  ┌────────▼─────────┐                            │
│                  │   chat API       │                            │
│                  │ sendChatMessage()│                            │
│                  └────────┬─────────┘                            │
│                           │                                      │
└───────────────────────────┼──────────────────────────────────────┘
                            │
                            │ HTTP POST /api/chat/message
                            │ Authorization: Basic
                            │
┌───────────────────────────▼──────────────────────────────────────┐
│                   BACKEND (Spring Boot)                          │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌────────────────────────────────────────────────────────┐     │
│  │              ChatController                            │     │
│  │  POST /api/chat/message                                │     │
│  │  ┌──────────────────────────────────────────────┐     │     │
│  │  │ @RequestBody ChatMessageRequest              │     │     │
│  │  │ @ResponseBody ChatMessageResponse            │     │     │
│  │  └─────────────────┬────────────────────────────┘     │     │
│  └────────────────────┼───────────────────────────────────┘     │
│                       │                                          │
│  ┌────────────────────▼───────────────────────────────────┐     │
│  │              ChatService                               │     │
│  │  Template-based NLP Pattern Matching                   │     │
│  │  ┌──────────────────────────────────────────────┐     │     │
│  │  │ Pattern Handlers:                            │     │     │
│  │  │ - handleOverdueInvoicesQuery()               │     │     │
│  │  │ - handleRevenueQuery()                       │     │     │
│  │  │ - handleDraftInvoicesQuery()                 │     │     │
│  │  │ - handleSentInvoicesQuery()                  │     │     │
│  │  │ - handleCustomerQuery()                      │     │     │
│  │  │ - handleInvoiceStatusQuery()                 │     │     │
│  │  │ - handleCreateInvoiceHelp()                  │     │     │
│  │  │ - handleReminderHelp()                       │     │     │
│  │  │ - handleHelpQuery()                          │     │     │
│  │  │ - handleUnknownQuery()                       │     │     │
│  │  └────────┬─────────────────────┬─────────────┘     │     │
│  └───────────┼─────────────────────┼────────────────────┘     │
│              │                     │                            │
│  ┌───────────▼──────┐  ┌───────────▼──────────┐                │
│  │ InvoiceRepository│  │ CustomerRepository  │                │
│  ├──────────────────┤  ├─────────────────────┤                │
│  │ - findAll()      │  │ - findAll()         │                │
│  │ - findByStatus() │  │ - findByActiveTrue()│                │
│  │ - findByStatus   │  │ - findByEmail()     │                │
│  │   AndDueDateBefore│ └─────────────────────┘                │
│  └──────────┬───────┘                                          │
│             │                                                   │
│  ┌──────────▼────────────────────────────────────────────┐     │
│  │                  Database (H2)                        │     │
│  │  ┌─────────────┐  ┌──────────────┐                   │     │
│  │  │  invoices   │  │  customers   │                   │     │
│  │  │  table      │  │  table       │                   │     │
│  │  └─────────────┘  └──────────────┘                   │     │
│  └───────────────────────────────────────────────────────┘     │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow

### 1. User Sends Message

```
User Input → ChatWindow → chatStore → API Client → Backend
```

**Details:**
1. User types message in ChatWindow input field
2. ChatWindow calls `addMessage()` to add user message to store
3. ChatWindow calls `sendChatMessage()` API function
4. API client sends POST request with Authorization header
5. Backend ChatController receives request

### 2. Backend Processing

```
ChatController → ChatService → Repositories → Database
```

**Details:**
1. ChatController receives ChatMessageRequest
2. ChatService processes message using pattern matching
3. Service queries InvoiceRepository and/or CustomerRepository
4. Repositories fetch data from database
5. Service constructs response with suggestions
6. Controller returns ChatMessageResponse

### 3. Display Response

```
Backend → API Client → chatStore → ChatWindow → User
```

**Details:**
1. API client receives ChatMessageResponse
2. ChatWindow adds AI message to store via `addMessage()`
3. ChatWindow updates suggestions state
4. ChatWindow stores conversationId
5. Messages auto-scroll to bottom
6. User sees response and suggestion chips

## Component Interaction

### ChatBubble Component
- **Purpose:** Toggle button for chat
- **State:** Reads `isOpen` from chatStore
- **Actions:** Calls `toggleChat()` on click
- **Position:** Fixed bottom-right
- **Visibility:** Only when authenticated

### ChatWindow Component
- **Purpose:** Main chat interface
- **State:** Reads `messages`, `isOpen`, `isLoading`, `conversationId`
- **Actions:**
  - `addMessage()` - Add user/AI messages
  - `setIsLoading()` - Show loading indicator
  - `setConversationId()` - Track conversation
  - `toggleChat()` - Close window
- **Features:**
  - Auto-scroll to latest message
  - Enter key sends message
  - Clickable suggestion chips
  - Welcome message on first open
  - Loading animation (bouncing dots)

### ChatAssistant Component
- **Purpose:** Wrapper with auth check
- **Checks:** `isAuthenticated` from AuthContext
- **Renders:** ChatWindow + ChatBubble if authenticated

## State Management (Zustand)

```typescript
interface ChatState {
  messages: ChatMessage[]      // All messages in conversation
  isOpen: boolean             // Chat window open/closed
  isLoading: boolean          // Waiting for response
  conversationId: string | null  // Current conversation ID

  // Actions
  addMessage()
  setMessages()
  setIsOpen()
  setIsLoading()
  setConversationId()
  clearMessages()
  toggleChat()
}
```

## Authentication Flow

```
1. User logs in → AuthProvider sets isAuthenticated = true
2. ChatAssistant component renders (was null before)
3. ChatBubble appears in bottom-right corner
4. All API calls include Basic Auth header from localStorage
5. Backend validates credentials on each request
6. User logs out → isAuthenticated = false
7. ChatAssistant returns null (chat disappears)
```

## Pattern Matching Logic

```java
// In ChatService.processMessage()

if (matchesPattern(message, "overdue|past due|late")) {
    return handleOverdueInvoicesQuery(response);
}
else if (matchesPattern(message, "revenue|income|earnings|total.*paid")) {
    return handleRevenueQuery(message, response);
}
else if (matchesPattern(message, "draft")) {
    return handleDraftInvoicesQuery(response);
}
// ... more patterns ...
else {
    return handleUnknownQuery(message, response);
}
```

**Pattern Format:** Java regex with case-insensitive matching

## Error Handling

### Frontend
```typescript
try {
  const response = await sendChatMessage({...});
  addMessage(aiMessage);
} catch (error) {
  addMessage({
    content: 'Sorry, I encountered an error...',
    sender: 'ai'
  });
  console.error('Chat error:', error);
}
```

### Backend
- Spring Boot exception handling (existing)
- 401 if not authenticated
- 500 if service error
- Frontend shows error message in chat

## Security

1. **Authentication Required:** Chat only visible when logged in
2. **Authorization Headers:** All API calls include Basic Auth
3. **Server Validation:** Backend validates auth on every request
4. **No Data Modification:** Chat only queries, never modifies data
5. **Input Sanitization:** Pattern matching prevents injection
6. **CORS:** Handled by existing Spring Boot configuration

## Performance Considerations

1. **Client-Side State:** Messages stored in browser (Zustand)
2. **Stateless Backend:** No server-side session state
3. **Efficient Queries:** Direct repository queries, no N+1
4. **Auto-Scroll Optimization:** Uses refs and `scrollIntoView()`
5. **Lazy Loading:** Chat components only load when needed
6. **Small Payload:** Minimal JSON response size

## Technology Stack

### Frontend
- **Framework:** Next.js 14
- **Language:** TypeScript
- **State:** Zustand
- **Styling:** Tailwind CSS
- **HTTP Client:** Axios
- **Rendering:** Client-side components ('use client')

### Backend
- **Framework:** Spring Boot 3
- **Language:** Java 21
- **Architecture:** CQRS-inspired
- **Database:** H2 (in-memory)
- **ORM:** JPA/Hibernate
- **Security:** Spring Security (Basic Auth)

## File Structure

```
backend/
└── src/main/java/com/invoiceme/
    ├── application/chat/
    │   ├── ChatMessageRequest.java
    │   ├── ChatMessageResponse.java
    │   └── ChatService.java
    └── interfaces/rest/
        └── ChatController.java

frontend/
└── src/
    ├── components/chat/
    │   ├── ChatAssistant.tsx
    │   ├── ChatBubble.tsx
    │   └── ChatWindow.tsx
    ├── lib/api/
    │   ├── chat.ts
    │   └── index.ts
    ├── store/
    │   └── chatStore.ts
    ├── types/
    │   └── chat.ts
    └── app/
        └── layout.tsx (modified)
```

## Extension Points

The architecture supports easy extension:

1. **New Patterns:** Add handler methods in ChatService
2. **Real AI:** Replace ChatService with OpenAI/Claude integration
3. **Persistence:** Add ConversationRepository to save history
4. **Actions:** Add action buttons to execute commands
5. **Notifications:** Implement unread message tracking
6. **Analytics:** Log queries for insights

---

This architecture provides a clean separation of concerns, easy testability, and straightforward extension paths for future enhancements.
