# Questions

Here are 2 questions related to the codebase. There's no right or wrong answer - we want to understand your reasoning.

## Question 1: API Specification Approaches

When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded everything directly. 

What are your thoughts on the pros and cons of each approach? Which would you choose and why?

**Answer:**
```txt
OpenAPI-first (Warehouse):
- Pros:
  - Single contract source of truth for backend + consumers.
  - Generated models/interfaces reduce boilerplate and drift.
  - Easier governance (versioning, linting, documentation, client SDK generation).
  - Strong fit for cross-team/public APIs where backward compatibility matters.
- Cons:
  - Spec maintenance overhead (you must keep YAML high quality).
  - Generation workflow can feel slower during rapid prototyping.
  - Custom behavior can become awkward if generator abstractions are too rigid.

Code-first (Product/Store):
- Pros:
  - Faster iteration for small/internal endpoints.
  - Less tooling friction initially.
  - Business logic and endpoint behavior evolve together in one place.
- Cons:
  - Higher risk of undocumented behavior and contract drift.
  - Harder to align consumers without a strict contract.
  - API consistency can degrade over time.

What I would choose:
- Hybrid by API criticality.
- OpenAPI-first for externally consumed or business-critical domains (like Warehouse where rules and integrations are sensitive).
- Code-first for small internal endpoints in early stages, but with a path to converge on an OpenAPI contract once endpoints stabilize.
- In this project, I would keep Warehouse OpenAPI-first and gradually move Store/Product to a shared contract once requirements settle.
```

---

## Question 2: Testing Strategy

Given the need to balance thorough testing with time and resource constraints, how would you prioritize tests for this project? 

Which types of tests (unit, integration, parameterized, etc.) would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I’d prioritize tests by business risk and change frequency:

1) Fast domain/unit tests (highest ROI)
- Validate business rules in use cases (capacity constraints, archive/replace invariants, uniqueness assumptions).
- Keep these deterministic and very fast so they run on every commit.

2) Integration tests for transactional boundaries
- Focus on repository + transaction behavior that unit tests cannot prove:
  - persistence and constraints,
  - rollback behavior,
  - legacy-sync only after commit.
- Run on each PR in CI.

3) Concurrency/optimistic-locking tests for critical write paths
- Fewer in count, but targeted at high-impact race conditions.
- Run in CI (possibly in a dedicated stage) and in nightly suites if runtime is heavy.

4) API endpoint tests (contract + error mapping)
- Validate status codes, payloads, and key failure scenarios.
- Prefer a small but representative set over exhaustive duplication of use-case tests.

How to keep coverage effective over time:
- Define a test pyramid budget (many unit, fewer integration, selective concurrency).
- Add tests for every production bug (regression-first policy).
- Track critical-path coverage rather than only line coverage.
- Keep test data builders/fixtures reusable to reduce maintenance cost.
- Periodically prune flaky/low-value tests and strengthen deterministic ones.

In short: maximize fast domain confidence first, then protect transactional/concurrency correctness where production risk is highest.
```
