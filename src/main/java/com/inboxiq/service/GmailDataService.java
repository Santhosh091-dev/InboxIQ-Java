package com.inboxiq.service;

import com.google.api.services.drive.model.About;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GmailDataService {

    @Autowired private GoogleClientService google;

    public record SenderInfo(String address, String display, int count,
                             String unsubscribeLink, List<String> subjects) {}

    private static class Agg {
        String display; int count = 0; String link = ""; List<String> subjects = new ArrayList<>();
        Agg(String display) { this.display = display; }
    }

    public String myEmail() throws Exception {
        return google.gmail().users().getProfile("me").execute().getEmailAddress();
    }

    public Map<String, Object> storage() throws Exception {
        About.StorageQuota q = google.drive().about().get()
                .setFields("storageQuota").execute().getStorageQuota();
        double used = q.getUsage() == null ? 0 : q.getUsage() / 1073741824.0;
        double limit = q.getLimit() == null ? 15.0 : q.getLimit() / 1073741824.0;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("usedGb", Math.round(used * 100.0) / 100.0);
        out.put("limitGb", Math.round(limit * 100.0) / 100.0);
        out.put("percent", limit == 0 ? 0.0 : Math.round(used / limit * 1000.0) / 10.0);
        return out;
    }

    public List<SenderInfo> fetchSubscriptionSenders(int maxMessages) throws Exception {
        Gmail gmail = google.gmail();
        ListMessagesResponse resp = gmail.users().messages().list("me")
                .setQ("unsubscribe").setMaxResults((long) maxMessages).execute();
        List<Message> msgs = resp.getMessages();
        if (msgs == null) return List.of();

        Map<String, Agg> map = new LinkedHashMap<>();
        for (Message m : msgs) {
            Message full = gmail.users().messages().get("me", m.getId())
                    .setFormat("metadata")
                    .setMetadataHeaders(List.of("From", "Subject", "List-Unsubscribe"))
                    .execute();
            if (full.getPayload() == null || full.getPayload().getHeaders() == null) continue;

            String from = null, subject = null, unsub = null;
            for (MessagePartHeader h : full.getPayload().getHeaders()) {
                switch (h.getName().toLowerCase()) {
                    case "from" -> from = h.getValue();
                    case "subject" -> subject = h.getValue();
                    case "list-unsubscribe" -> unsub = h.getValue();
                    default -> { }
                }
            }
            if (from == null) continue;

            String address = extractAddress(from);
            Agg a = map.get(address);
            if (a == null) { a = new Agg(from); map.put(address, a); }
            a.count++;
            if (subject != null && a.subjects.size() < 5) a.subjects.add(subject);
            if (a.link.isEmpty() && unsub != null) a.link = unsub;
        }

        List<SenderInfo> list = new ArrayList<>();
        for (Map.Entry<String, Agg> e : map.entrySet()) {
            Agg a = e.getValue();
            list.add(new SenderInfo(e.getKey(), a.display, a.count, a.link, a.subjects));
        }
        list.sort((x, y) -> Integer.compare(y.count(), x.count()));
        return list;
    }

    private String extractAddress(String from) {
        Matcher m = Pattern.compile("<([^>]+)>").matcher(from);
        String a = (m.find() ? m.group(1) : from).trim().toLowerCase();
        return a.length() > 255 ? a.substring(0, 255) : a;
    }
}