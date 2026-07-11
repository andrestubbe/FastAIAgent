# FastAIAgent 0.1.0 — Cognitive Mind and Planning Layer for Java

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe)

---

**💡 Cognitive reasoning, planning loops, and multi-agent coordination — Decoupled execution mind for the FastJava AI ecosystem.**

FastAIAgent is the **cognitive engine** of the FastJava AI platform. It processes natural language goals, generates logical plan graphs, orchestrates tool-use chains, and verifies results through the underlying execution runtime (`FastAIRuntime`) to complete complex agentic tasks.

---

## Technical Features

- **🧠 Plan-Act-Observe Loop** — Closed-loop execution cycle leveraging FastAI LLMs to generate and adapt schedules.
- **💾 Short/Long-Term Memory** — Dynamic session history formatting powered by `FastAIMemory`.
- **🔌 Runtime Decoupling** — Separation of thought (FastAIAgent) and execution (FastAIRuntime) ensures reliable replayability.
- **⚡ Thread-Safe Orchestration** — Ready for concurrent multi-agent environments.

---

## Quick Start

```java
import fastai.AI;
import fastai.FastAI;
import fastaiagent.FastAIAgent;
import fastaimemory.ConversationHistory;
import fastairuntime.FastAIRuntime;

public class Demo {
    public static void main(String[] args) {
        AI brain = FastAI.connect("ollama:llama3.2:3b");
        FastAIRuntime runtime = new FastAIRuntime();
        ConversationHistory memory = new ConversationHistory();

        FastAIAgent agent = new FastAIAgent(brain, runtime, memory);
        
        // Plan -> Act -> Observe
        agent.run("Start application notepad.exe");
    }
}
```

---

## Installation

### Maven (JitPack)
Add JitPack repository and dependency to your `pom.xml`:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastAIAgent</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

---

## Related Projects
- [FastAIRuntime](https://github.com/andrestubbe/FastAIRuntime) - Deterministic execution engine
- [FastAI](https://github.com/andrestubbe/FastAI) - Unified AI client interface for Java
- [FastAIMemory](https://github.com/andrestubbe/FastAIMemory) - Unified conversation memory
- [FastAIVectorDB](https://github.com/andrestubbe/FastAIVectorDB) - JNI native vector database
- [FastAIRag](https://github.com/andrestubbe/FastAIRag) - Retrieval-augmented generation pipeline
- [FastAIBot](https://github.com/andrestubbe/FastAIBot) - High-performance bot orchestrator
- [FastCore](https://github.com/andrestubbe/FastCore) - Unified JNI loader and platform abstraction

---

## License
This project is licensed under the MIT License.
