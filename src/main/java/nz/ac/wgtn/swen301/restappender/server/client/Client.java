package nz.ac.wgtn.swen301.restappender.client;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Client {

    private static final OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: Client <type (csv or excel)> <fileName>");
            return;
        }

        String type = args[0];
        String fileName = args[1];
        String url;

        if ("csv".equals(type)) {
            url = "http://localhost:8080/restappender/stats/csv";
        } else if ("excel".equals(type)) {
            url = "http://localhost:8080/restappender/stats/excel";
        } else {
            System.err.println("Invalid type. Choose 'csv' or 'excel'.");
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Error fetching data. Server responded with status: " + response.code());
                return;
            }

            byte[] bytes = response.body().bytes();
            Files.write(Paths.get(fileName), bytes);
            System.out.println("Data saved to: " + fileName);

        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }
}
