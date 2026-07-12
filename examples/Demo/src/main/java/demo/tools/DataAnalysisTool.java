package demo.tools;

import fastairuntime.FastObservation;
import fastairuntime.FastTool;
import fastmath.FastMathStats;
import java.util.Arrays;
import java.util.Map;

public final class DataAnalysisTool implements FastTool {

    @Override
    public String name() {
        return "data.analyze";
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

            double mean = FastMathStats.mean(data);
            double stddev = FastMathStats.stddev(data);
            
            // compute min and max manually since we don't know if FastMathStats has min/max off the top of our head
            double min = data[0];
            double max = data[0];
            for (double v : data) {
                if (v < min) min = v;
                if (v > max) max = v;
            }

            String result = String.format("Data Analysis Result:\n- Count: %d\n- Mean: %.4f\n- StdDev: %.4f\n- Min: %.4f\n- Max: %.4f",
                    data.length, mean, stddev, min, max);

            return new SimpleObservation(true, result);
        } catch (Exception e) {
            return new SimpleObservation(false, "Failed to analyze data: " + e.getMessage());
        }
    }

    private record SimpleObservation(boolean success, String message) implements FastObservation {}
}
