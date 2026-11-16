---
name: uzzo-scope
description: Scope a new feature or review an existing scope from a description or high-level requirements; interactivelly ask clarifying questions, fix in/out boundaries, and write a standard scope.md.
meta:
    version: "20261115"
---

# Skill: Scope

Turn a description, a rough idea or a list of high-level requirements into a precise scope: what is delivered now, what is explicitly not, and the decisions taken to get there. Also use it to review an existing scope or a proposed change to it (gaps, contradictions, overlap, ambiguities). The output is a `scope.md` the user can later turn into user stories.

Use it when the user asks to scope, review the scope of, or refine the boundaries of an application, feature, domain or change. It does not write user stories or code.

## Modes

- **Scope** (default): the input is a description, a policy question or high-level requirements. Produce a new scope.
- **Review**: the input is an existing scope.md, a story, or a change request against one. Produce findings and, if the user agrees, an updated scope.

If unclear which one applies, ask the user.

## Pipeline

### 1. Read before asking
Look at what already exists, in particular using skill `uzzo-kb`, if available: the main scope file, other scope files in the project, the stories index, the project reference docs (coding standard, development framework). Note what the request overlaps with, depends on, or contradicts. Never ask something the project already answers. Do not duplicate existing stories or capabilities: reference them.

### 2. Clarify (at most 3 questions at a time)
- Ask only about what changes the scope: who triggers it, where the data comes from, what is explicitly excluded, defaults and limits, failure behaviour, platform assumptions.
- Give each question concrete options with a recommended default and the cost of a wrong guess, so the user can answer with one word.
- Group related questions; never exceed 3 per round. Several rounds are fine; stop as soon as the boundaries are clear.
- Focus questions on unresolved requirements, platform assumptions, or ambiguous architectural constraints.
- Never speculate about a technology, platform or document path that is unknown: ask.
- If the user is not available to answer, take the most reasonable reading, state it at the top of the result, and carry on.

### 3. Decide the boundaries
Partition all discussed capabilities into two mutually exclusive groups:
- In-Scope Capabilities: Features, platforms, and behaviors that MUST be delivered in the current implementation cycle.
- Out-of-Scope / Future Enhancements: Features explicitly deferred, nice-to-haves, or ideas that will cause scope creep. Treat anything not explicitly listed as in-scope as out-of-scope by default.
.

### 4. Review checks (always, and the whole job in Review mode)
- **Gaps**: a flow with no failure behaviour, no default, no limit, or no owner.
- **Contradictions**: with the existing scope, existing stories or the reference docs.
- **Overlap**: capabilities already covered elsewhere; point to them instead of repeating.
- **Hidden scope**: items that would silently pull in a large piece of work (authentication, packaging, a new dependency); either list them or exclude them.
- **Testability**: flag capabilities no one could verify.
Report findings briefly, most important first, with a suggested fix for each.

### 5. Write the scope
Put the file where the project keeps scopes (typically a `scope.md` in the domain folder, referenced from the main scope file). Follow exactly this template:

```
# Project Name: <name> (and domain, if it is a domain scope)
**Version:** <e.g. 0.1.0>

## Overall Description
<High-level description organised by functional domain, including the decisions taken (defaults, limits, rules).>

## In-Scope Capabilities
* **<Domain/Component>**:
  - [ ] <capability>
  - [ ] <capability>

## Out-of-Scope / Future Enhancements
* <excluded or deferred item, with the reason when it helps>

## Technical Context & Infrastructure
* **Development Toolchain**: <...>
* **Target Systems/Platforms**: <...>
* **Project Reference Docs**:
  - Coding Standard: `<path or link>`
  - Development Framework: `<path or link>`
```

If the user has not asked for the file yet, show the scope in the reply and offer to save it. Update the main scope file with a link to a new domain scope.

### 6. Close
Say in a few lines what was decided and what stays out. Offer the next step (turning the scope into user stories) without starting it.

## Rules
- Never invent requirements; everything in scope traces to something the user said or confirmed.
- Prefer the smallest scope that delivers the goal; push extras to the out-of-scope list rather than quietly adding them.
- Keep wording concrete and short; one capability per line.
- Keep decisions the user took in earlier rounds; do not reopen them.
