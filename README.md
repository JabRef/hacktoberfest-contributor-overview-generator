# Hacktoberfest Contributor Overview Generator

> Generates a CSV file containing the overview on Hacktoberfest contributions.

For [JabRef's Hacktoberfest](https://www.jabref.org/hacktoberfest/2019.html), we needed an overview on contributors.
There was no tool available, so we wrote our own.

This tool works for us.
The code needs to be adapted for your repositories.

- Get a [GitHub personal access token](https://github.com/settings/tokens) and insert it at the beginning of `App.java`.
- Adapt the `organization` and `repoName` fields to contain your repositories in `App.java`..
- Adapt the `excludedLogins` to contain login names core developers in `App.java`.

Run it with `./gradlew run`

Example output:

```text
Example-User1 (GitHub <noreply@github.com>): pull requests: 1, lines added: 12, lines removed: 12, files touched: 5
Example-User2 (GitHub <noreply@github.com>): pull requests: 2, lines added: 52, lines removed: 9, files touched: 5
Example-User3 (GitHub <noreply@github.com>): pull requests: 3, lines added: 28, lines removed: 27, files touched: 16
Example-User4 (GitHub <noreply@github.com>): pull requests: 5, lines added: 99, lines removed: 17, files touched: 13
Example-User5 (GitHub <noreply@github.com>): pull requests: 1, lines added: 290, lines removed: 14, files touched: 4

total: authors: 5, pull requests: 12, lines added: 481, lines removed: 79, files touched: 41
pull requests: total: 12, min: 1, max: 5, avg: 2, med: 2
files touched: total: 43, min: 4, max: 16, avg: 9, med: 5
lines added: total: 79, min: 9, max: 27, avg: 16, med: 14
lines removed: total: 481, min: 12, max: 290, avg: 96, med: 52
```

License: MIT. See [LICENSE](./LICENSE) for details.
