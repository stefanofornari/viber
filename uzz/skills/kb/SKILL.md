# Knowledge Base Instructions
version: 0.0

## Structure
The knowledge base is a loosely structured collection of articles in markdown files. The root of the knowledge contains the following main folders:
- **library**: Useful general information on various topics, such as:
  - Development practices
  - Programming languages
  - Frameworks
  - Standard definitions
  - ...
- **project**: Project-specific information, such as:
  - Project name and version
  - Project summary (short description)
  - Roles and people
  - Timeline
  - Online repository
  - ...
- **stories**: User stories implemented, in progress, or to be done. Each article contains one user story only with the following structure:
  - **Title**
  - **User Story**
    - Body (the user story itself)
    - Status
    - Dependencies
    - Notes
  - **Acceptance Criteria**
  - **References**
  - **Keywords** (list of relevant keywords)

## User Stories

### The Title
- Should be **clear and concise** (e.g., `US101 - Enable Dark Mode for Improved User Experience`).
- Include the **US identifier** (e.g., `US101`).

### The Body
- Written in the **standard format**:
  ```
  AS A [role]
  I WANT [goal]
  SO THAT [benefit]
  ```
- Focus on **user intent** and **value**.

### The Status
- Possible values: `DONE`, `TO BE DONE`, `IN PROGRESS`, `BLOCKED`.
- Indicates the **current state** of the user story.

### Dependencies
- List any **dependent user stories** (e.g., `US100` for `US101`).
- Helps track **implementation order**.

### Notes
- Provide **technical details**, **implementation guidance**, or **edge cases**.
- Should **not** include acceptance criteria (keep it separate).

### The Acceptance Criteria
- Structured as **scenarios** with:
  - **Given** (preconditions, if any).
  - **When** (user action).
  - **Expect** (observed result).
- Use **bold** for keywords like `WHEN`, `EXPECT`, and `GIVEN`.
- Ensure **testability**: Each criterion should be verifiable.

### The References
- Link to **external sources** (e.g., GitHub issues, design documents).
- Helps trace **requirements back to discussions**.

### Keywords
- Add a **"Keywords"** section **at the end** of each article.
- Include **relevant keywords** to improve searchability and categorization.

### Example 1: Enable Dark Mode
```markdown
## US101 - Enable Dark Mode for Improved User Experience

### User Story
```
AS A user
I WANT to enable a dark mode in the application
SO THAT I can reduce eye strain during prolonged use
```

#### Status
TO BE DONE

#### Dependencies
- US99

#### Notes
1. Implement a toggle button in the **Settings** menu.
2. Ensure dark mode is consistent across all UI components.
3. Allow users to customize text and background colors.

### Acceptance Criteria

1. **Primary action: toggle dark mode**
   ```
   **WHEN** the user toggles dark mode from the Settings menu
   **EXPECT**
   - The UI switches to dark mode immediately.
   - All components adjust to the dark theme.
   ```

2. **Primary action: customize theme colors**
   ```
   **GIVEN** dark mode is enabled
   **WHEN** the user clicks on "Customize Colors"
   **EXPECT**
   - A dialog appears to adjust text and background colors.
   - Changes are applied in real-time.
   ```

### References
- [Design Document #101](https://example.com/design-docs/101)

### Keywords
...

```

### Example 2: Add Multi-Language Support
```markdown
## US102 - Add Multi-Language Support for Global Accessibility

### User Story
```
AS A user
I WANT to use the application in my preferred language
SO THAT I can interact with it comfortably
```

#### Status
IN PROGRESS

#### Dependencies
- US98

#### Notes
1. Integrate a language selector dropdown in the **Profile** section.
2. Support at least five languages initially.
3. Ensure text translations are accurate and contextually appropriate.

### Acceptance Criteria

1. **Primary action: select language**
   ```
   **WHEN** the user selects a language from the dropdown
   **EXPECT**
   - The application UI updates to the selected language.
   - All text elements reflect the translation.
   ```

2. **Primary action: default language**
   ```
   **GIVEN** no language preference is set
   **WHEN** the application is launched
   **EXPECT**
   - The UI appears in the system default language.
   ```

### References
- [Localization Guide #102](https://example.com/localization/102)

### Keywords
...

```

## Rules
- Do not create anything directly under the root.
- Under the main folders, articles can be organized in subfolders, although this is not mandatory.
- When creating a new article, ensure it includes a **"Keywords"** section listing the most relevant keywords.
- Every article can be fully referenced by its parent folder(s) and filename.

## Keywords
- Knowledge Base
- Markdown files
- Library
- Project
- User Stories
- Instructions
- Acceptance creteria