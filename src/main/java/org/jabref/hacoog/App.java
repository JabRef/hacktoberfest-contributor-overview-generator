package org.jabref.hacoog;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Pull;
import com.jcabi.github.Repo;
import com.jcabi.github.RtGithub;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.collections.api.bag.primitive.IntBag;
import org.eclipse.collections.api.bag.primitive.MutableIntBag;
import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.primitive.IntBags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    // required to access the API
    private static final String GitHub_Token = "REPLACEME";

    // Which organization/user should be analyzed?
    private static final String organization = "JabRef";

    // these repositories are analyzed
    private static final ImmutableList<String> repositories = Lists.immutable.of(
            "jabref",
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
            "docker-javafx",
            "cloudref"
    );

    // PRs created by these logins do not count towards the statistics
    private static final ImmutableCollection<String> excludedLogins = Sets.immutable.of("tobiasdiez", "koppor", "stefan-kolb", "matthiasgeiger", "Siedlerchr", "LinusDietz", "davidemdot");

    private static final String RESULT_CSV_FILE = "./result.csv";

    static LocalDateTime tooLate = LocalDateTime.of(2019, 11, 1, 12, 00);
    static LocalDateTime tooEarly = LocalDateTime.of(2019, 9, 30, 12, 00);

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public void analyze() throws Exception {
        Github github = new RtGithub(GitHub_Token);

        MutableMap<String, Information> authorToInformation = Maps.mutable.empty();
        Information globalInformation = new Information("*all*");

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(RESULT_CSV_FILE));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("Repository", "Pull Request Number", "State", "Title", "Created At", "User", "Name", "Email", "Invalid?", "Lines Added", "Lines Removed", "Files Touched"));) {
            for (String repoName : repositories) {
                logger.debug("Handling {} ...", repoName);

                Repo repo = github.repos().get(new Coordinates.Simple(organization + "/" + repoName));
                Iterable<Pull> iterate = repo.pulls().iterate(Map.of("state", "all"));

                Iterator<Pull> iterator = iterate.iterator();
                boolean isSeptember = false;
                while (!isSeptember && iterator.hasNext()) {
                    Pull pull = iterator.next();
                    JsonObject json = pull.json();
                    String createdAt = json.getString("created_at");
                    LocalDateTime creationDate = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME);
                    if (creationDate.isAfter(tooLate)) {
                        // next iteration
                    } else if (creationDate.isAfter(tooEarly)) {
                        String state = json.getString("state");
                        String title = json.getString("title");
                        String login = json.get("user").asJsonObject().getString("login");
                        Iterator<JsonValue> labels = json.get("labels").asJsonArray().iterator();
                        boolean isInvalid = false;
                        while (labels.hasNext() && !isInvalid) {
                            isInvalid = labels.next().asJsonObject().getString("name").equalsIgnoreCase("invalid");
                        }

                        int additions = ((JsonNumber) json.get("additions")).intValue();
                        int deletions = ((JsonNumber) json.get("deletions")).intValue();
                        int changedFiles = ((JsonNumber) json.get("changed_files")).intValue();

                        JsonObject commitJson = pull.commits().iterator().next().json();
                        JsonObject commiterInformation = commitJson.get("committer").asJsonObject();
                        String committerName = commiterInformation.getString("name");
                        String committerEmail = commiterInformation.getString("email");

                        if (!isInvalid && !excludedLogins.contains(login)) {
                            Information information = authorToInformation.computeIfAbsent(login, key -> new Information(key));
                            information.pullRequestCount++;
                            globalInformation.pullRequestCount++;
                            information.updateNameAndEmail(committerName, committerEmail);

                            information.linesAdded += additions;
                            information.linesRemoved += deletions;

                            globalInformation.linesAdded += additions;
                            globalInformation.linesRemoved += deletions;

                            pull.files().forEach(fileInformation -> {
                                String fileName = fileInformation.getString("filename");
                                // "README.md" is different for each repository. Thus,
                                information.filesTouched.add(repoName + " --> " + fileName);
                                globalInformation.filesTouched.add(repoName + " --> " + fileName);
                            });
                        }

                        csvPrinter.printRecord(repoName, pull.number(), state, title, createdAt, login, committerName, committerEmail, isInvalid, additions, deletions, changedFiles);
                    } else {
                        // 2019-09-30 12:00 or earlier
                        isSeptember = true;
                    }
                }
            }
            csvPrinter.flush();

            System.out.println();

            authorToInformation.forEach((login, information) -> {
                System.out.printf("%s (%s <%s>): pull requests: %d, lines added: %d, lines removed: %d, files touched: %d\n", login, information.name, information.email, information.pullRequestCount, information.linesAdded, information.linesRemoved, information.filesTouched.size());
            });

            System.out.println();

            System.out.printf("total: authors: %d, pull requests: %d, lines added: %d, lines removed: %d, files touched: %d\n", authorToInformation.keySet().size(), globalInformation.pullRequestCount, globalInformation.linesAdded, globalInformation.linesRemoved, globalInformation.filesTouched.size());

            final MutableIntBag pullRequestCounts = IntBags.mutable.empty();
            final MutableIntBag linesAdded = IntBags.mutable.empty();
            final MutableIntBag linesRemoved = IntBags.mutable.empty();
            final MutableIntBag filesTouched = IntBags.mutable.empty();

            authorToInformation.forEach((login, information) -> {
                pullRequestCounts.add(information.pullRequestCount);
                linesAdded.add(information.linesAdded);
                linesRemoved.add(information.linesRemoved);
                filesTouched.add(information.filesTouched.size());
            });

            outAggregatedInformation("pull requests", pullRequestCounts);
            outAggregatedInformation("files touched", filesTouched);
            outAggregatedInformation("lines added", linesRemoved);
            outAggregatedInformation("lines removed", linesAdded);
        }
    }

    private void outAggregatedInformation(String prefix, IntBag set) {
        System.out.printf("%s: total: %d, min: %d, max: %d, avg: %.0f, med: %.0f\n", prefix, set.sum(), set.min(), set.max(), set.average(), set.median());
    }

    public static void main(String[] args) throws Exception {
        new App().analyze();
    }
}
