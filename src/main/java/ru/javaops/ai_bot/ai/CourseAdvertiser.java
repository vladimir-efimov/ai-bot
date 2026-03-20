package ru.javaops.ai_bot.ai;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.URI;
import java.net.http.HttpResponse;

import org.eclipse.jetty.http.HttpStatus;

public class CourseAdvertiser {

    private final String httpAddress;

    public CourseAdvertiser(String httpAddress) {
        this.httpAddress = httpAddress;
    }

    public void advertise(long tgId, String course) throws IOException {
        var llmServiceRequest = new LlmServiceRequest(tgId, course);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(httpAddress))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(llmServiceRequest.toString()))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if ( response.statusCode() != HttpStatus.OK_200) {
                throw new IOException("Get response with status " + response.statusCode() + "\n" +
                        "Response body is: " + response.body());
            }
        } catch (InterruptedException ex) {
            throw new IOException("Connection was interrupted");
        }
    }
}
