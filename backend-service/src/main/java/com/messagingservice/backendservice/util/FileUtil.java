package com.messagingservice.backendservice.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtil {
    public static String readFile(MultipartFile file){
        try {
            // Check if the file is XML or JSON based on its content type or file extension
            if (isXMLFile(file)) {
                // Read XML file
                String xmlContent = readXMLFile(file);
                return xmlContent;
            } else if (isJSONFile(file)) {
                // Read JSON file
                String jsonContent = readJSONFile(file);
                return jsonContent;
            } else {
                return "Unsupported file format";
            }
        } catch (IOException e) {
            return "Error reading the file";
        }
    }

    public static boolean isXMLFile(MultipartFile file) {
        // Check if the file content type or file extension indicates it's an XML file
        // Example implementation:
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        return contentType != null && contentType.equals("application/xml") ||
                fileName != null && fileName.toLowerCase().endsWith(".xml");
    }

    public static boolean isJSONFile(MultipartFile file) {
        // Check if the file content type or file extension indicates it's a JSON file
        // Example implementation:
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        return contentType != null && contentType.equals("application/json") ||
                fileName != null && fileName.toLowerCase().endsWith(".json");
    }

    public static String readXMLFile(MultipartFile file) throws IOException {
        // Read XML file content and return as a string
        // Example implementation:
        InputStream inputStream = file.getInputStream();
        StringBuilder xmlContent = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            xmlContent.append(line);
        }
        return xmlContent.toString();
    }

    public static String readJSONFile(MultipartFile file) throws IOException {
        // Read JSON file content and return as a string
        // Example implementation:
        InputStream inputStream = file.getInputStream();
        byte[] bytes = inputStream.readAllBytes();
        return new String(bytes);
    }

}
