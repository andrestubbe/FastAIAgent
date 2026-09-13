# TODO: FastAIAgent — High-Performance Local Multi-Agent Runtime

Baut direkt auf den existierenden Klassen `FastAgentKernel`, `FastAIAgent`, `FastAIEventBus` und `FastAIRuntime` auf.

## 1. Orchestrierung & Multi-Agent Dispatch (First Mate)
- [ ] **`FastAIService` / `FastAgentOrchestrator`**:
  - Zentraler Dispatcher für parallele Spezialisten-Agenten (`FastAgentKernel`).
  - Automatische Delegierung: Ziel/Prompt analysieren -> passenden Agenten spawnen.
  - Quota-freie, rein lokale Ausführung über `FastAIModel` (Zero-Copy Layer Streaming).
- [ ] **Agenten-Rollen als Spezialisten**:
  - `ArchitectureAgent`: Projektplanung, Modul-Hierarchien.
  - `BugfixAgent`: Testausführung (`FastTest`), Fehler-Lokalisierung, Auto-Patching.
  - `ResearchAgent`: Web- & Codebase-Recherche via `FastWebSpider` & `FastWebScrape`.
  - `TuiAgent`: Terminal-UIs via `FastTUIContext` (bereits im Demo-Paket vorhanden).
  - `ValidationAgent`: Verifikation & Live-Testing ("No-Mistakes"-Pipeline).

## 2. Integration mit `FastAIMemory`
- [ ] **Context- & History-Management**:
  - Kopplung des `FastAgentKernel.Observer` an `FastAIMemory.MemoryContextBuilder`.
  - Token-Begrenzung & dynamisches Sliding-Window via `MemoryWindow`.
  - Multi-Turn-Gedächtnis mit `SemanticMemory` und `SummaryMemory`.
  - Nutzung der existierenden Formatierer (`ChatMLFormatter`, `PlainTextFormatter`).

## 3. Tool-Bridge & Native FastJava-Integration
- [ ] Direkte Tool-Aufrufe ohne Python/Web-Overhead:
  - `FastFileIndex` / `FastFileSearch` für Code-Navigation.
  - `FastGPU` für beschleunigte Tensor- und Matrixberechnungen.
  - `FastConPTY` / `FastTerminal` für Prozess- und Befehlsausführung.

## 4. CREAM Integration (Deterministisches Replay & Time-Travel)
- [ ] Alle `FastAIEventBus`-Events (`agent.plan`, `agent.act.result`, `agent.reflect`) in CREAM-Zeitachsen loggen.
- [ ] Vollständige Wiederholbarkeit fehlerhafter Task-Schritte zur Ursachenanalyse.

## 5. Visual Evidence (FastOverlay AgentBoard)
- [ ] GPU-gerendertes Live-Dashboard für alle aktiven Agenten via `FastOverlay` / `FastWindow`.
- [ ] Visueller Task-Graph, Token-Throughput und Ressourcen-Monitor.
