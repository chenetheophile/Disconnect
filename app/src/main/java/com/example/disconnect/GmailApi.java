//package com.example.disconnect;
//
//import com.google.api.client.googleapis.json.GoogleJsonError;
//import com.google.api.client.googleapis.json.GoogleJsonResponseException;
//import com.google.api.client.http.HttpRequestInitializer;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.gson.GsonFactory;
//import com.google.api.services.gmail.Gmail;
//import com.google.api.services.gmail.GmailScopes;
//import com.google.api.services.gmail.model.VacationSettings;
//import com.google.auth.http.HttpCredentialsAdapter;
//import com.google.auth.oauth2.GoogleCredentials;
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.time.ZoneOffset;
//import java.time.ZonedDateTime;
//
///* Class to demonstrate the use of Gmail Enable Auto Reply API*/
//public class GmailApi {
//    /**
//     * Enables the auto reply
//     *
//     * @return the reply message and response metadata.
//     * @throws IOException - if service account credentials file not found.
//     */
//    public static VacationSettings autoReply() throws IOException {
//        /* Load pre-authorized user credentials from the environment.
//          TODO(developer) - See https://developers.google.com/identity for
//           guides on implementing OAuth2 for your application. */
//        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
//                .createScoped(GmailScopes.GMAIL_SETTINGS_BASIC);
//        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
//
//        // Create the gmail API client
//        Gmail service = new Gmail.Builder(new NetHttpTransport(),
//                GsonFactory.getDefaultInstance(),
//                requestInitializer)
//                .setApplicationName("Gmail samples")
//                .build();
//
//        try {
//            // Enable auto reply by restricting domain with start time and end time
//            VacationSettings vacationSettings = new VacationSettings()
//                    .setEnableAutoReply(true)
//                    .setResponseBodyHtml(
//                            "I am on vacation and will reply when I am back in the office. Thanks!")
//                    .setRestrictToDomain(true)
//                    .setStartTime(LocalDateTime.now()
//                            .toEpochSecond(ZoneOffset.from(ZonedDateTime.now())) * 1000)
//                    .setEndTime(LocalDateTime.now().plusDays(7)
//                            .toEpochSecond(ZoneOffset.from(ZonedDateTime.now())) * 1000);
//
//            VacationSettings response = service.users().settings()
//                    .updateVacation("me", vacationSettings).execute();
//            // Prints the auto-reply response body
//            System.out.println("Enabled auto reply with message : " + response.getResponseBodyHtml());
//            return response;
//        } catch (GoogleJsonResponseException e) {
//            // TODO(developer) - handle error appropriately
//            GoogleJsonError error = e.getDetails();
//            if (error.getCode() == 403) {
//                System.err.println("Unable to enable auto reply: " + e.getDetails());
//            } else {
//                throw e;
//            }
//        }
//        return null;
//    }
//}
