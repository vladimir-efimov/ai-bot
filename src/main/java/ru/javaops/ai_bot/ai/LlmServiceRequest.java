package ru.javaops.ai_bot.ai;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class LlmServiceRequest {

    private final String request;

    private static final String REQUEST_BODY_TEMPLATE = """
            {
                "message": {
                    "text": "%s",
                    "chat": {
                       "id": "%d"
                    }
                }
            }
            """;

    public LlmServiceRequest(long tgId, String course) throws IOException{
        String llmRequestTemplate = readResource("llm_request.txt");
        request = String.format(REQUEST_BODY_TEMPLATE, String.format(llmRequestTemplate, course), tgId);
    }

    @Override
    public String toString() {
        return request;
    }

    // reads data from text resource file and merges data into single line
    public static String readResource(String fileName) throws IOException {
        try (InputStream inputStream = LlmServiceRequest.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource not found: " + fileName);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!content.isEmpty()) content.append(" ");
                    content.append(line);
                }
                return content.toString();
            }
        }
    }
}
