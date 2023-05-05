package xxxx.find.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Config {
    private Map<String, List<String>> sinks;
    private Map<String, String> neo4j;
}
