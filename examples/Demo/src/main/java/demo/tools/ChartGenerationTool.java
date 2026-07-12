package demo.tools;

import fastairuntime.FastObservation;
import fastairuntime.FastTool;
import java.util.Arrays;
import java.util.Map;

public final class ChartGenerationTool implements FastTool {

    @Override
    public String name() {
        return "chart.generate_ascii";
    }

    @Override
    public FastObservation execute(Map<String, Object> args) {
        String dataStr = (String) args.get("data");
        if (dataStr == null || dataStr.isEmpty()) {
            return new SimpleObservation(false, "data argument is missing.");
        }
        try {
            double[] data = Arrays.stream(dataStr.split(","))
                    .map(String::trim)
                    .mapToDouble(Double::parseDouble)
                    .toArray();

            if (data.length == 0) {
                return new SimpleObservation(false, "Data array is empty.");
            }

            double max = data[0];
            for (double v : data) {
                if (v > max) max = v;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("ASCII Bar Chart:\n");
            for (int i = 0; i < data.length; i++) {
                int barLength = max > 0 ? (int) ((data[i] / max) * 20) : 0;
                sb.append(String.format("Item %02d | ", i + 1));
                for (int j = 0; j < barLength; j++) {
                    sb.append("█");
                }
                sb.append(String.format(" %.2f\n", data[i]));
            }

            return new SimpleObservation(true, sb.toString());
        } catch (Exception e) {
            return new SimpleObservation(false, "Failed to generate chart: " + e.getMessage());
        }
    }

    private record SimpleObservation(boolean success, String message) implements FastObservation {}
}
