package cr.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AuthorData {
    public String name;
    public String email;

    public int commits;
    public int merges;

    public float averageCommitMessageLength;
    public float averageCommitMessageWords;
    public Map<Integer, Integer> commitMessageWords;
    public float percentageOfMerges;
    public TimeAnalysisResult timeAnalysis;
    public float commitsWithMessage;
    public float commitsWithTrailingPeriod;
    public float averageFileActions;

    public AuthorData(Author author) {
        name = author.getName();
        email = author.getEmail();

        List<Commit> commits = author.getCommits();

        this.commits = commits.size();
        merges = commits.size() - removeMerges(commits).size();
        averageCommitMessageLength = averageCommitMessageLength(commits);
        averageCommitMessageWords = averageCommitMessageWords(commits);
        commitMessageWords = commitMessageWords(commits);
        percentageOfMerges = percentMerges(commits);
        timeAnalysis = timeAnalysis(commits);
        commitsWithMessage = percentWithCommitMessage(commits);
        commitsWithTrailingPeriod = percentWithTrailingPeriod(commits);
        averageFileActions = averageFileActions(commits);
    }

    @Override
    public String toString() {
        return "AuthorData{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", commits=" + commits +
                ", merges=" + merges +
                ", averageCommitMessageLength=" + averageCommitMessageLength +
                ", averageCommitMessageWords=" + averageCommitMessageWords +
                ", commitMessageWords=" + commitMessageWords +
                ", percentageOfMerges=" + percentageOfMerges +
                ", timeAnalysis=" + timeAnalysis +
                ", commitsWithMessage=" + commitsWithMessage +
                ", commitsWithTrailingPeriod=" + commitsWithTrailingPeriod +
                ", averageFileActions=" + averageFileActions +
                '}';
    }
    public static float averageCommitMessageLength(List<Commit> commits) {
        float sum = 0;
        for (Commit commit : commits) {
            sum += commit.getMessage().length();
        }
        return sum / commits.size();
    }


    public static float averageCommitMessageWords(List<Commit> commits) {
        float sum = 0;
        for (Commit commit : commits) {
            sum += commit.getMessage().split("\\s+").length;
        }
        return sum / commits.size();
    }

    public static TimeAnalysisResult timeAnalysis(List<Commit> commits) {
        TimeAnalysisResult result = new TimeAnalysisResult();
        for (Commit commit : commits) {
            String firstWord = commit.getMessage().split("\\s+")[0];
            if (firstWord.endsWith("s")) {
                result.present++;
            } else if (firstWord.endsWith("d")) {
                result.past++;
            } else {
                result.indetermined++;
            }
        }
        return result;
    }

    public static float percentWithCommitMessage(List<Commit> commits) {
        float sum = 0;
        for (Commit commit : commits) {
            if (commit.getMessage() == null || commit.getMessage().matches("(\\s|-)*")) {
                sum++;
            }
        }
        return 1 - sum / commits.size();
    }

    public static float percentWithTrailingPeriod(List<Commit> commits) {
        float sum = 0;
        for (Commit commit : commits) {
            if (commit.getMessage().endsWith(".")) {
                sum++;
            }
        }
        return sum / commits.size();
    }

    public static float percentMerges(List<Commit> commits) {
        return ((float) removeMerges(commits).size()) / commits.size();
    }

    public static List<Commit> removeMerges(List<Commit> commits) {
        ArrayList<Commit> result = new ArrayList<>();
        for (Commit commit : commits) {
            if (commit.getMessage().toLowerCase().startsWith("merge")) {
                result.add(commit);
            }
        }
        return result;
    }

    public static float averageFileActions(List<Commit> commits) {
        float sum = 0;
        for (Commit commit : commits) {
            sum += commit.getDiff().size();
        }
        return sum / commits.size();
    }

    public static Map<Integer, Integer> commitMessageWords(List<Commit> commits) {
        Map<Integer, Integer> occurrances = new TreeMap<>(); // Number of words -> Messages with that number of words
        for (Commit commit : commits) {
            int words = commit.getMessage().split("\\s+").length;
            if (occurrances.get(words) != null) {
                occurrances.put(words, occurrances.get(words) + 1);
            } else {
                occurrances.put(words, 1);
            }
        }
        return occurrances;
    }
}
