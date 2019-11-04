package org.jabref.hacoog;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Pull;
import com.jcabi.github.Repo;
import com.jcabi.github.RtGithub;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class App {

    private static final String RESULT_CSV_FILE = "./result.csv";

    private static final MediaType MEDIA_TYPE_TEXT = MediaType.get("text/plain; charset=utf-8");

    public void analyze() throws Exception {
        Github github = new RtGithub("INSERT-YOUR-TOKEN-HERE");

        OkHttpClient client = new OkHttpClient();

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(RESULT_CSV_FILE));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("Repository", "Pull Request Number", "State", "Title", "Created At", "User", "Name", "Email", "Invalid?", "Lines Added", "Lines Removed", "Files Touched"));) {
            for (String repoName : List.of("jabref",
                    "help.jabref.org",
                    "abbrv.jabref.org",
                    "JabFox",
                    "code.jabref.org",
                    "issues.jabref.org",
                    "donations.jabref.org",
                    "contribute.jabref.org",
                    "faq.jabref.org",
                    "downloads.jabref.org",
                    "www.jabref.org",
                    "blog.jabref.org",
                    "cloudref",
                    "docker-javafx"
            )) {

                System.out.println("Handling " + repoName + "...");

                Repo repo = github.repos().get(new Coordinates.Simple("JabRef/" + repoName));
                Iterable<Pull> iterate = repo.pulls().iterate(Map.of("state", "all"));

                Iterator<Pull> iterator = iterate.iterator();
                boolean isSeptember = false;
                while (!isSeptember && iterator.hasNext()) {
                    Pull pull = iterator.next();
                    System.out.println(pull.number());
                    JsonObject json = pull.json();
                    String createdAt = ((JsonString) json.get("created_at")).getString();
                    System.out.println(createdAt);
                    if (createdAt.startsWith("2019-11")) {
                        // next iteration
                    } else if (createdAt.startsWith("2019-10")) {
                        String state = ((JsonString) json.get("state")).getString();
                        String title = ((JsonString) json.get("title")).getString();
                        System.out.println(title);
                        System.out.println(createdAt);
                        String login = ((JsonString) json.get("user").asJsonObject().get("login")).getString();
                        System.out.println(login);
                        Iterator<JsonValue> labels = json.get("labels").asJsonArray().iterator();
                        boolean isInvalid = false;
                        while (labels.hasNext() && !isInvalid) {
                            isInvalid = ((JsonString) labels.next().asJsonObject().get("name")).getString().equalsIgnoreCase("invalid");
                        }
                        System.out.println(isInvalid);
                        String diffUrl = ((JsonString) json.get("diff_url")).getString();
                        System.out.println(diffUrl);

                        int additions = ((JsonNumber) json.get("additions")).intValue();
                        int deletions = ((JsonNumber) json.get("deletions")).intValue();
                        int changedFiles = ((JsonNumber) json.get("changed_files")).intValue();

                        JsonObject commitJson = pull.commits().iterator().next().json();
                        JsonObject commiterInformation = commitJson.get("committer").asJsonObject();
                        String committerName = ((JsonString) commiterInformation.get("name")).getString();
                        System.out.println(committerName);
                        String committerEmail = ((JsonString) commiterInformation.get("email")).getString();
                        System.out.println(committerEmail);

                        System.out.println(additions);
                        System.out.println(deletions);
                        System.out.println(changedFiles);

/*
                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .url(diffUrl)
                            .build();

                    try (Response response = client.newCall(request).execute()) {
                        System.out.println("body?");
                        String diffAsString = response.body().string();
                        System.out.println(diffAsString);
                        Patch<String> stringPatch = UnifiedDiffUtils.parseUnifiedDiff(Arrays.asList(diffAsString.split("\\n")));
                        stringPatch.getDeltas().stream().map(delta -> delta.getSource().)
                    }
*/

                        csvPrinter.printRecord(repoName, pull.number(), state, title, createdAt, login, committerName, committerEmail, isInvalid, additions, deletions, changedFiles);
                    } else {
                        // 2019-09 or earlier
                        isSeptember = true;
                    }
                }
            }
            csvPrinter.flush();
        }
    }

    public static void main(String[] args) throws Exception {
        new App().analyze();
    }
}
