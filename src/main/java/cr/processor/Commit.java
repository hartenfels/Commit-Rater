package cr.processor;

import java.util.Arrays;

public class Commit {
    private String sha;
    private String[] parents;
    private String message;

    public Commit() {}

    public Commit(String sha, String[] parents, String message) {

        this.sha = sha;
        this.parents = parents;
        this.message = message;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String[] getParents() {
        return parents;
    }

    public void setParents(String[] parents) {
        this.parents = parents;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Commit{" +
                "sha='" + sha + '\'' +
                ", parents=" + Arrays.toString(parents) +
                ", message='" + message + '\'' +
                '}';
    }
}
