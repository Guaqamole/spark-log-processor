package org.guaqamole.alert;

import com.slack.api.Slack;
import com.slack.api.webhook.Payload;

import java.io.IOException;

public class SlackAlertManager {
    private String webhookUrl;

    public SlackAlertManager(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
    public void sendSlackAlert(String message) {
        Slack slack = Slack.getInstance();
        Payload payload = Payload.builder()
                .text(message)
                .build();
        try {
            slack.send(webhookUrl, payload);
        } catch (IOException e) {
            System.err.println("Error sending Slack alert: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
