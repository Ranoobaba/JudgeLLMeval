# ARCHITECTURE.md

## Overview

This project implements an **AI Judge layer** for an internal annotation platform:

- Users upload annotation **submissions** (JSON).

- They define **judges** (rubrics + target models).

- They assign judges to specific **questions** per **queue**.

- The system calls an LLM provider to evaluate each answer and records:

  - `verdict: pass | fail | inconclusive`

  - short `reasoning` text

- A **Results** view lets users filter and analyze evaluations.

The backend runs on **Akka SDK** (agents + workflows + entities).  

The frontend is a **Vite + React 18 + TypeScript** SPA.

---

## High-Level Architecture

```text

+---------------------------+         +--------------------------+

|         Frontend          |  HTTP   |          Backend         |

|  (Vite / React / TS)      +-------->+ (Akka SDK Service)       |

+---------------------------+         +--------------------------+

          ^       |                               |

          |       |                               v

          |   JSON APIs                   +-------------------+

          |                                |  Database (SQL)  |

          +--------------------------------+ (e.g. Postgres)  |

                                           +-------------------+

