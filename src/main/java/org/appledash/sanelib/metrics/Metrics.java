package org.appledash.sanelib.metrics;

import org.appledash.sanelib.SanePlugin;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Metrics {
    private static final String METRICS_ENDPOINT = "https://public.appledash.org/etc/sanelib-statistics.php";
    private final SanePlugin plugin;

    public Metrics(SanePlugin plugin) {
        this.plugin = plugin;
    }

    public void submitMetrics() throws IOException {
        this.httpPost(METRICS_ENDPOINT, this.getMetricsParameters());
    }

    private String getMetricsParameters() {
        try {
            return String.format(
                    "plugin=%s&version=%s&port=%d&maxPlayers=%d&operatingSystem=%s&otherPlugins=%s&time=%d",
                    this.plugin.getName(),
                    this.plugin.getServer().getVersion(),
                    this.plugin.getServer().getPort(),
                    this.plugin.getServer().getMaxPlayers(),
                    System.getProperty("os.name"),
                    URLEncoder.encode(Arrays.stream(this.plugin.getServer().getPluginManager().getPlugins()).map(Plugin::getName).collect(Collectors.joining(",")), "UTF-8"),
                    System.currentTimeMillis() / 1000
            );
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private void httpPost(String url, String params) {
        byte[] postData = params.getBytes(StandardCharsets.UTF_8);

        HttpURLConnection con = null;

        try {
            URL myurl = new URL(url);
            con = (HttpURLConnection) myurl.openConnection();

            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "SaneLib");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.write(postData);
            }

            StringBuilder content;

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {

                String line;
                content = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }
}
