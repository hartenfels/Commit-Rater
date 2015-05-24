package cr.processor;

import java.util.Arrays;
import java.util.List;

public class Author {
    private String[] names;
    private String[] emails;
    private List<Commit> commits;

    public Author() {
    }

    public Author(String[] names, String[] emails, List<Commit> commits) {
        this.names = names;
        this.emails = emails;
        this.commits = commits;
    }

    public String[] getNames() {
        return names;
    }

    public void setName(String[] name) {
        this.names = name;
    }

    public String[] getEmails() {
        return emails;
    }

    public void setEmail(String[] email) {
        this.emails = email;
    }

    public List<Commit> getCommits() {
        return commits;
    }

    public void setCommits(List<Commit> commits) {
        this.commits = commits;
    }

    @Override
    public String toString() {
        return "Author{" +
                "names=" + Arrays.toString(names) +
                ", emails=" + Arrays.toString(emails) +
                ", commits=" + commits +
                '}';
    }
}
