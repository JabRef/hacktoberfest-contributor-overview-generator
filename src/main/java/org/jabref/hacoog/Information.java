package org.jabref.hacoog;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.MutableSet;

public class Information {

    public final String login;

    public String name;
    public String email;
    public int linesAdded = 0;
    public int linesRemoved = 0;
    public int pullRequestCount = 0;

    public MutableSet<String> filesTouched = Sets.mutable.empty();

    public Information(String login) {
        this.login = login;
    }

    public void updateNameAndEmail(String name, String email) {
        if (this.email == null) {
            this.name = name;
            this.email = email;
        } else {
            if (!email.contains("noreply")) {
                this.name = name;
                this.email = email;
            }
        }
    }

}
