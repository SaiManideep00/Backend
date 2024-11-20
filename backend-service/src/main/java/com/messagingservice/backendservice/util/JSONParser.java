package com.messagingservice.backendservice.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class JSONParser {
    public static Map<String, List<List<String>>> jsonToMap(String json) throws JSONException {
        Map<String, List<List<String>>> hashMap = new TreeMap<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(json);

            // Convert JSON to HashMap

            flattenJSON(rootNode, "", hashMap);
            System.out.println(hashMap);
            System.out.println("Street: "+ hashMap.get("employees.skills"));
            // Print the key-value pairs
            for (Map.Entry<String, List<List<String>>> entry : hashMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                System.out.println(key + " = " + value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hashMap;
    }
    private static void flattenJSON(JsonNode node, String prefix, Map<String, List<List<String>>> hashMap) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();
                JsonNode valueNode = entry.getValue();
                flattenJSON(valueNode, prefix + key + ".", hashMap);
            }
        } else if (node.isArray()) {
            List<String> arrayValues = new ArrayList<>();
            for (JsonNode arrayElement : node) {
                if (arrayElement.isValueNode()) {
                    arrayValues.add(arrayElement.asText());
                } else {
                    Map<String, List<List<String>>> nestedMap = new TreeMap<>();
                    flattenJSON(arrayElement, prefix, nestedMap);
                    for (Map.Entry<String, List<List<String>>> entry : nestedMap.entrySet()) {
                        String nestedKey = entry.getKey();
                        List<List<String>> nestedValues = entry.getValue();
                        String fullKey = prefix + nestedKey;
                        if (hashMap.containsKey(fullKey)) {
                            hashMap.get(fullKey).addAll(nestedValues);
                        } else {
                            hashMap.put(fullKey, nestedValues);
                        }
                    }
                }
            }
            if (!arrayValues.isEmpty()) {
                String fullKey = prefix.substring(0, prefix.length() - 1);
                if (hashMap.containsKey(fullKey)) {
                    List<List<String>> existingValues = hashMap.get(fullKey);
                    for (List<String> existingValue : existingValues) {
                        existingValue.addAll(arrayValues);
                    }
                } else {
                    List<List<String>> values = new ArrayList<>();
                    values.add(arrayValues);
                    hashMap.put(fullKey, values);
                }
            }
        } else if (node.isValueNode()) {
            String value = node.asText();
            String fullKey = prefix.substring(0, prefix.length() - 1);
            if (hashMap.containsKey(fullKey)) {
                List<List<String>> existingValues = hashMap.get(fullKey);
                for (List<String> existingValue : existingValues) {
                    existingValue.add(value);
                }
            } else {
                List<List<String>> values = new ArrayList<>();
                List<String> singleValue = new ArrayList<>();
                singleValue.add(value);
                values.add(singleValue);
                hashMap.put(fullKey, values);
            }
        }
    }
}
