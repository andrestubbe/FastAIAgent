> [!WARNING]
> **🚧 WIP — Active AI Pipeline Construction & Architecture Optimization in Progress.**

# FastAIAgent 0.1.7 [ALPHA-2026-08-26]: Cognitive Mind and Autonomous Coding Engine for Java

[![Status](https://img.shields.io/badge/status-0.1.7-brightgreen.svg)](https://github.com/andrestubbe/FastAIAgent/releases/tag/0.1.7)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-0.1.7-green.svg)](https://jitpack.io/#andrestubbe/FastAIAgent)

---

**⚡ Autonomous ReAct coding loops and cognitive planning for Java — decoupled execution mind orchestrating file authoring, terminal commands, and self-healing.**

**FastAIAgent** is a high-performance, framework-agnostic cognitive agent engine for the JVM. It implements the formal **5-step ReAct coding loop** (`Observe → Plan → Act → Reflect → Memory`) to enable autonomous coding agents that inspect codebases, write and edit project files, run CLI tools, and correct build errors with zero framework bloat.

![Autonomous Coding Agent State Machine](docs/coding_loop_diagram.jpg)

---

## Quick Start

```java
import fastaiagent.FastAgentKernel;
import fastairuntime.FastAIRuntime;
import fastairuntime.tools.CommandRunnerTool;
import fastairuntime.tools.FileEditTool;
import fastairuntime.tools.FileReadTool;
import fastairuntime.tools.FileSaveTool;

public class Demo {
    public static void main(String[] args) {
        // 1. Setup deterministic OS toolchain harness
        FastAIRuntime runtime = new FastAIRuntime();
        runtime.register(new FileReadTool());
        runtime.register(new FileSaveTool());
        runtime.register(new FileEditTool());
        runtime.register(new CommandRunnerTool());

        // 2. Initialize Autonomous Coding Kernel
        FastAgentKernel kernel = new FastAgentKernel(runtime,
            () -> runtime.execute(new fastairuntime.FastCommand("file.read", java.util.Map.of("path", "src/Main.java"))),
            (goal, obs, plan) -> /* AI / LLM reasoning planner */,
            (plan, result) -> result.success() ? "OK" : "Error: " + result.message()
        );

        // 3. Execute goal-driven ReAct loop
        kernel.loop("Create, compile, and fix Calculator.java", 10);
    }
}
```

---

## Table of Contents

- [Why FastAIAgent?](#why-fastaiagent)
- [Quick Start](#quick-start)
- [Key Features](#key-features)
- [Real-World Use Cases](#real-world-use-cases)
- [Architecture Overview](#architecture-overview)
- [API Quick Reference](#api-quick-reference)
- [Technical Demos & Benchmarks](#technical-demos--benchmarks)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [Related Projects](#related-projects)
- [License](#license)

---

## Why FastAIAgent?

Traditional agent frameworks in Python (`LangChain`, `CrewAI`, `AutoGen`) and Java (`LangChain4j`) are bloated, slow, and impose heavy framework locks:

| Feature | LangChain / LangChain4j | FastAIAgent |
|:---|:---|:---|
| **Architecture** | Monolithic chain DSL | Strict Mind/Body separation (Agent + Runtime) |
| **Execution Layer** | Abstracted away, hard to control | Deterministic `FastAIRuntime` with explicit tool registry |
| **Cognitive Loop** | Chain-based, no formal state machine | Formal 5-step ReAct: `Observe → Plan → Act → Reflect → Memory` |
| **Framework Lock** | Tight coupling to LangChain abstractions | Pure Java 21+, zero framework dependencies |
| **Observability** | Limited hooks | `FastAIEventBus` real-time event subscription on every step |
| **Self-Healing** | Manual error handling | Native compile-diagnose-patch loop via `FastAIReasoner` |

---

## Key Features

- 💻 **Autonomous Code Authoring & Patching**: Create, inspect, patch (`file.edit`), and compile Java source files in a self-directed loop.
- 🧠 **5-Stage Cognitive Loop**: Native state machine for `Observe → Plan → Act → Reflect → Memory` with full plan rewriting between turns.
- 📡 **FastAIEventBus Observability**: Real-time event subscription for step logs, token traces, and tool telemetry at every loop iteration.
- ⚡ **Deterministic OS Execution**: Direct file, keyboard, mouse, process, and CLI management via `FastAIRuntime` with security gates.
- 💾 **Stateful Conversation Memory**: Native integration with `FastAIMemory` and `FastAIBot` for multi-turn context persistence.

---

## Real-World Use Cases

- 🛠️ **Self-Healing CI/CD Pipelines**: Automatically parses compiler error logs and test failure stack traces, locates the offending source files, and applies targeted patches (`file.edit`) to restore green builds.
- 🔄 **Autonomous Codebase Refactoring**: Scans repository structure via `FastAIRuntime`, upgrades deprecated API calls, and normalizes formatting across hundreds of Java classes without developer intervention.
- 🧪 **Test Suite Generation & Verification**: Observes existing production classes, generates corresponding JUnit test suites (`file.save`), and executes `mvn test` in a loop until full coverage is verified.
- 🖥️ **Desktop & OS-Level Automation**: Orchestrates multi-step developer setup flows by combining shell commands with native UI interactions (UIA, Notepad, system tools).

---

## Architecture Overview

**[FastAIAgent](https://github.com/andrestubbe/FastAIAgent) (The Mind)**
Orchestrates the cognitive ReAct loop, task planning, and reflection over results.

**[FastAIRuntime](https://github.com/andrestubbe/FastAIRuntime) (The Body & Harness)**
Provides deterministic OS-level tool execution (`file.read`, `file.save`, `file.edit`, `cmd.run`, UIA, keyboard, mouse) and security gates.

**[FastAIMemory](https://github.com/andrestubbe/FastAIMemory) (The Memory)**
Maintains structured conversation history, context windows, and episodic state.

**[FastAI](https://github.com/andrestubbe/FastAI) (The LLM Client)**
Powers streaming model inference with local and cloud models.

---

## API Quick Reference

| Class / Method | Return Type | Description | Docs |
|:---|:---|:---|:---|
| `FastAgentKernel(runtime, obs, planner, reflector)` | `FastAgentKernel` | Constructs the 5-step ReAct coding agent kernel. | [Reference](docs/REFERENCE.md) |
| `kernel.loop(goal, maxCycles)` | `void` | Executes the Observe-Plan-Act-Reflect cycle until the goal is met or max cycles reached. | [Reference](docs/REFERENCE.md) |
| `FastAIEventBus.getInstance()` | `FastAIEventBus` | Accesses the global agent event dispatcher for real-time observability. | [Reference](docs/REFERENCE.md) |
| `FastAIPromptBuilder.buildSystemPrompt(runtime)` | `String` | Generates a tool-definition system prompt from all registered runtime tools. | [Reference](docs/REFERENCE.md) |
| `FastAIAgent(bot, runtime, logger)` | `FastAIAgent` | Standard conversational agent with tool-call parsing and execution. | [Reference](docs/REFERENCE.md) |

---

## Technical Demos & Benchmarks

FastAIAgent provides 36+ runnable standalone demos in `examples/Demo/`:

| Script | Class | Demonstrates |
|:---|:---|:---|
| `run-36-self-healing-reflection-demo.bat` | `SelfHealingReflectionDemo` | **Chain-of-Thought Self-Healing**: Compiler diagnosis & automated patch repair |
| `run-35-reasoner-guided-coding-demo.bat` | `ReasonerGuidedCodingDemo` | **Tree-of-Thoughts Guided Coding**: Multi-branch architectural exploration via `FastAIReasoner` |
| `run-34-coding-agent-loop-demo.bat` | `CodingAgentLoopDemo` | **Autonomous Coding Agent**: File creation, inspection, refactoring (`file.edit`) |
| `run-34a-observe-sub-demo.bat` | `CodingObserveSubDemo` | Phase 1: Environment & Workspace Observation |
| `run-34b-plan-act-sub-demo.bat` | `CodingPlanActSubDemo` | Phase 2 & 3: Plan Formulation & Deterministic Execution |
| `run-34c-reflect-sub-demo.bat` | `CodingReflectSubDemo` | Phase 4: Self-Reflection & Error Recovery |
| `run-16-multi-agent-orchestrator-demo.bat` | `MultiAgentOrchestratorDemo` | Multi-agent coordination and handoff |
| `run-05-file-manipulation-agent-demo.bat` | `FileManipulationAgentDemo` | File system read, write, and edit operations |
| `run-01-planning-agent-demo.bat` | `PlanningAgentDemo` | Multi-step planning and Notepad execution |

> [!NOTE]
> All 36 demo scripts are located in the `examples/Demo/` directory and launch their respective Java class via Maven.

---

## Installation

### Option 1: Maven (Recommended)

Add the JitPack repository and the dependencies to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- FastAIAgent - Cognitive Coding Agent Engine -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastAIAgent</artifactId>
        <version>0.1.7</version>
    </dependency>

    <!-- FastAIRuntime - Deterministic OS Execution Harness -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastAIRuntime</artifactId>
        <version>0.1.0</version>
    </dependency>

    <!-- FastAIMemory - Conversation Memory & Context Windows -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastAIMemory</artifactId>
        <version>0.1.3</version>
    </dependency>

    <!-- FastAI - Unified AI Client -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastAI</artifactId>
        <version>0.1.14</version>
    </dependency>

    <!-- FastCore - Required Native JNI Loader -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastCore</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastAIAgent:0.1.7'
    implementation 'com.github.andrestubbe:FastAIRuntime:0.1.0'
    implementation 'com.github.andrestubbe:FastAIMemory:0.1.3'
    implementation 'com.github.andrestubbe:FastAI:0.1.14'
    implementation 'com.github.andrestubbe:FastCore:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the release JARs directly from GitHub Releases:

1. 🧠 **[FastAIAgent-0.1.7.jar](https://github.com/andrestubbe/FastAIAgent/releases/tag/0.1.7)** (Cognitive Engine)
2. ⚙️ **[FastAIRuntime-0.1.0.jar](https://github.com/andrestubbe/FastAIRuntime/releases/tag/0.1.0)** (Execution Harness)
3. 💾 **[FastAIMemory-0.1.3.jar](https://github.com/andrestubbe/FastAIMemory/releases/tag/0.1.3)** (Conversation Memory)
4. 🤖 **[FastAI-0.1.14.jar](https://github.com/andrestubbe/FastAI/releases/tag/0.1.14)** (Unified AI Client)
5. ⚙️ **[FastCore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/tag/0.1.0)** (Mandatory Native JNI Loader)

> [!IMPORTANT]
> All JARs must be included in your classpath for the agent runtime and memory layers to function correctly.

---

## Documentation

- **[REFERENCE.md](docs/REFERENCE.md)**: Core API reference manual for `FastAgentKernel`, `FastAIAgent`, and `FastAIEventBus`.
- **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: ReAct coding loop and decoupled Mind/Body architecture design rationale.
- **[COMPILE.md](docs/COMPILE.md)**: Maven build instructions and dependency setup.
- **[CHANGELOG.md](docs/CHANGELOG.md)**: Complete project version history.
- **[ROADMAP.md](docs/ROADMAP.md)**: Planned milestones and ecosystem integrations.

---

## Platform Support

| Platform | Architecture | Status | Notes |
|:---|:---:|:---:|:---|
| **Windows 10 / 11** | x64 | ✅ Fully Supported | Full ReAct loop, OS tool execution, native UIA |
| **Linux** | x64 / AArch64 | 🚧 Planned | Cloud LLM providers work today; OS tools pending |
| **macOS** | Apple Silicon / x64 | 🚧 Planned | Cloud LLM providers work today; OS tools pending |

---

## Related Projects

- **[`FastAI`](https://github.com/andrestubbe/FastAI)**: Unified AI Client for Java (20+ providers)
- **[`FastAIRuntime`](https://github.com/andrestubbe/FastAIRuntime)**: Sandboxed Process Runner and Tool-Calling Execution Pipeline
- **[`FastAIMemory`](https://github.com/andrestubbe/FastAIMemory)**: Conversation History, Sliding Windows, and Rolling Summaries
- **[`FastAIBot`](https://github.com/andrestubbe/FastAIBot)**: Zero-Bloat Bot Harnesses and Persona Runtime
- **[`FastAIReasoner`](https://github.com/andrestubbe/FastAIReasoner)**: Deterministic Planning, Chain-of-Thought, and Self-Correction
- **[`FastAIMemory`](https://github.com/andrestubbe/FastAIMemory)**: Conversation History, Sliding Windows, and Rolling Summaries
- **[`FastAIGraph`](https://github.com/andrestubbe/FastAIGraph)**: In-Memory Knowledge Graph and Multi-Hop Relationship Engine
- **[`FastAIMCP`](https://github.com/andrestubbe/FastAIMCP)**: Model Context Protocol (MCP) Server & Tool Integration
- **[`FastAIState`](https://github.com/andrestubbe/FastAIState)**: Lock-Free Shared Agent State & Blackboard Memory
- **[`FastAIVision`](https://github.com/andrestubbe/FastAIVision)**: High-Speed Local Multimodal Vision and Screen-VLM Engine
- **[`FastCore`](https://github.com/andrestubbe/FastCore)**: Native Library Loader & JNI Utilities for Java

---

## License

MIT License. See [LICENSE](LICENSE) file for details.

---

**Part of the FastJava Ecosystem** — *Making the JVM faster.* 🚀