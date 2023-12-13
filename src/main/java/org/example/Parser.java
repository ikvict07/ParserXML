package org.example;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


public class Parser {
    private static final String OUTPUT_PATH = "src/main/java/org/example/";
    private final String path_to_xml;

    public Parser(String pathToXml) {
        path_to_xml = pathToXml;
    }

    public void parse() {
        StringBuilder stringBuilder = new StringBuilder();
        String className;
        List<String> tags = new ArrayList<>();
        try {
            Document document = getDocument();
            document.getDocumentElement().normalize();

            Element root = document.getDocumentElement();
            if (!root.getTagName().equals("extension")) {
                throw new IllegalArgumentException();
            }
            className = root.getAttribute("type");
            if (className.isEmpty()) {
                throw new IllegalArgumentException();
            }

            getTags(root, tags);

            String javaFile = generateJavaFile(stringBuilder, className, tags);
            writeToFile(javaFile, className);

        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    private Document getDocument() throws ParserConfigurationException, SAXException, IOException {
        File xmlFile = new File(path_to_xml);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlFile);
    }

    private void getTags(Node node, List<String> tags) {
        if (node.getNodeType() == Node.ELEMENT_NODE && !node.getNodeName().equals("extension")) {
            Element element = (Element) node;
            tags.add(element.getTagName());
        }

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            getTags(nodeList.item(i), tags);
        }
    }

    private void generateHeader(StringBuilder builder, String className) {
        builder.append(
                STR."class \{className} {\n\n"
        );
    }

    private void addField(StringBuilder builder, String fieldName) {
        builder.append(
                STR."\tprivate String \{fieldName};\n"
        );
    }

    private void generateGetter(StringBuilder stringBuilder, String fieldName) {
        stringBuilder
                .append(STR."\tpublic String get\{capitalize(fieldName)}() {\n")
                .append(STR."\t\treturn \{fieldName};\n")
                .append("\t}\n");
    }

    private void generateSetter(StringBuilder stringBuilder, String fieldName) {
        stringBuilder
                .append(STR."\tpublic void set\{capitalize(fieldName)}() {\n")
                .append(STR."\t\tthis.\{fieldName} = \{fieldName};\n")
                .append("\t}\n");
    }

    private void writeToFile(String content, String name) {
        File generatedFile = new File(OUTPUT_PATH + STR."\{name}.java");
        try (PrintWriter writer = new PrintWriter(generatedFile)) {
            writer.print(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String generateJavaFile(StringBuilder stringBuilder, String className, List<String> tags) {
        generateHeader(stringBuilder, className);
        for (String field : tags) {
            addField(stringBuilder, field);
        }
        stringBuilder.append("\n\n");
        for (String field : tags) {
            generateGetter(stringBuilder, field);
            stringBuilder.append("\n");
            generateSetter(stringBuilder, field);
            stringBuilder.append("\n");
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
