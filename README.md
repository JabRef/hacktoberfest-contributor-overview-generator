# Hacktoberfest Contributor Overview Generator

> Generates a CSV file containing the overview on Hacktoberfest contributions.

For [JabRef's Hacktoberfest](https://www.jabref.org/hacktoberfest/2019.html), we needed an overview on contributors.
There was no tool available, so we wrote our own.

This tool works for us.
The code needs to be adapted for your repositories.

- Get a [GitHub personal access token](https://github.com/settings/tokens) and insert it at the beginning of `org.jabref.hacoog.App.analyze`.
- In `org.jabref.hacoog.App.analyze`, adapt the `repoName` for loop to contain your repositories.
- If run later than November 2019, adapt the `if` condition at line 68 in `org.jabref.hacoog.App.analyze` accordingly.

Run it with `./gradlew run`

License: MIT. See [LICENSE](./LICENSE) for details.
