package demo.tools;

import fastairag.RagStore;
import fastairuntime.FastObservation;
import fastairuntime.FastTool;
import java.util.Map;

public final class RagSearchTool implements FastTool {

    private final RagStore ragStore;

    public RagSearchTool(RagStore ragStore) {
        this.ragStore = ragStore;
    }

    @Override
    public String name() {
        return "knowledge.search";
    }

    @Override
    public FastObservation execute(Map<String, Object> args) {
        String query = (String) args.get("query");
        if (query == null || query.isEmpty()) {
            return new SimpleObservation(false, "query argument is missing.");
        }
        try {
            // Retrieve top k documents (e.g. k=3)
            String result = ragStore.buildContext(query, 3);
            if (result == null || result.trim().isEmpty() || result.equals("Context information:\n---\n")) {
                return new SimpleObservation(true, "No relevant documents found for query: " + query);
            }
            return new SimpleObservation(true, "Found relevant context:\n" + result);
        } catch (Exception e) {
            return new SimpleObservation(false, "Failed to search knowledge base: " + e.getMessage());
        }
    }

    private record SimpleObservation(boolean success, String message) implements FastObservation {}
}
