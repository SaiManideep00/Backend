package com.messagingservice.backendservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class XMLParser {
    public static Map<String, List<List<String>>> xmlToMap(String xml) throws JSONException {
        Map<String, List<List<String>>> hashMap = new TreeMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode rootNode = objectMapper.readTree(xml);

        // Convert JSON to HashMap

        hashMap = flattenXML(xml);
        System.out.println(hashMap);
        //System.out.println("Street: "+ hashMap.get("employees.skills"));
        // Print the key-value pairs
        for (Map.Entry<String, List<List<String>>> entry : hashMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            System.out.println(key + " = " + value);
        }
        return hashMap;
    }

    // Recursive function to convert XML to the desired format
    public static Map<String, List<List<String>>> flattenXML(String xmlData) {
        Map<String, List<List<String>>> resultMap = new TreeMap<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(xmlData));
            Document doc = builder.parse(inputSource);

            processNode(doc.getDocumentElement(), "", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(resultMap);
        return resultMap;
    }

    private static void processNode(Node node, String currentPath, Map<String, List<List<String>>> resultMap) {
        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String newPath = currentPath.isEmpty() ? child.getNodeName() : currentPath + "." + child.getNodeName();

            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (child.hasChildNodes() && child.getFirstChild().getNodeType() == Node.TEXT_NODE) {
                    String value = child.getFirstChild().getNodeValue();
                    List<List<String>> currentValuesList = new ArrayList<>();
                    List<String> valueList = new ArrayList<>();
                    valueList.add(value);
                    currentValuesList.add(valueList);

                    if (resultMap.containsKey(newPath)) {
                        resultMap.get(newPath).add(valueList);
                    } else {
                        resultMap.put(newPath, currentValuesList);
                    }
                } else {
                    processNode(child, newPath, resultMap);
                }
            }
        }
    }

    public static String removeXmlDeclaration(String xmlData) {
        // Find the index of the closing angle bracket of the XML declaration
        int startIndex = xmlData.indexOf("<?xml");
        int endIndex = xmlData.indexOf("?>");

        if (startIndex != -1 && endIndex != -1) {
            // Include the length of "?>" to remove the entire XML declaration
            startIndex = xmlData.lastIndexOf("<", endIndex);
            // Extract and return the substring after the XML declaration
            return xmlData.substring(startIndex).trim();
        }
        System.out.println(xmlData);
        // If XML declaration not found, return the original XML data
        return xmlData;
    }
    public static Map<String, Object> getKeys(MultipartFile xmlFile) {
        Map<String, Object> keysMap = new TreeMap<>();
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(xmlFile.getInputStream());

            // Start the recursive traversal to get the keys
            traverseXML(document.getDocumentElement(), "", keysMap);

            // Print the keys (values are not required, as per the requirement)
            for (String key : keysMap.keySet()) {
                System.out.println(key);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keysMap;
    }

    private static void traverseXML(Element element, String prefix, Map<String, Object> keysMap) {
        NodeList nodeList = element.getChildNodes();
        int childCount = 0;

        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i) instanceof Element) {
                Element childElement = (Element) nodeList.item(i);
                String childName = childElement.getTagName();
                String childPrefix = prefix + (prefix.isEmpty() ? "" : ".") + childName;
                traverseXML(childElement, childPrefix, keysMap);
                childCount++;
            }
        }
        if (childCount == 0) {
            // Leaf node (no children)
            String value = element.getTextContent().trim();
            if (!value.isEmpty()) {
                keysMap.put(prefix, value);
            }
        }
    }
}
