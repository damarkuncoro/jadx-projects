# DexForge Coding Standards

To ensure high-fidelity static analysis and maintainability, all DexForge components must adhere to the following rules.

## 1. No Deep Nesting (Flat Logic)
Avoid `if-in-if` whenever possible.
- **Max Depth:** 2 levels. If you hit level 3, you MUST refactor.
- **Technique:** Use **Guard Clauses** (Early Returns/Throws).
- **Technique:** Use **Strategy Pattern** (Map lookups) for opcode/type dispatching.

## 2. SOLID Principles
- **Single Responsibility:** A class should do one thing (e.g., `InstructionSet` registers opcodes, `SmaliEmulator` runs them).
- **Encapsulation:** "Tell, Don't Ask". Objects should manage their own null checks and state validity.

## 3. DRY (Don't Repeat Yourself)
- If a specific null-check or signature-parsing logic is used in more than two places, move it to a utility class or a base component in `dexforge-commons`.

## 4. Java 11 Compatibility
- Do not use features from Java 14+ (like Switch Expressions with `->` or Records) in the `dexforge-core` or `dexforge-api` modules yet, to ensure maximum environment compatibility.
