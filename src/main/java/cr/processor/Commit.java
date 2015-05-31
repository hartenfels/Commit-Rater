package cr.processor;

import java.util.Arrays;
import java.util.Map;

public class Commit {
    private String sha;
    private String[] parents;
    private String message;
    private Map<String, String> diff;

    public Commit(String sha, String[] parents, String message, Map<String, String> diff) {
        this.sha = sha;
        this.parents = parents;
        this.message = message;
        this.diff = diff;
    }

    public Commit() {}

    public Map<String, String> getDiff() {

        return diff;
    }

    public void setDiff(Map<String, String> diff) {
        this.diff = diff;
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
                ", diff=" + diff +
                '}';
    }
}
