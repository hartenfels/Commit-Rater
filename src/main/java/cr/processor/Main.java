package cr.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

class Main {
    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Author> authors = mapper.readValue(new File("out/local.json"), new TypeReference<List<Author>>() {});
        for (Author author : authors) {
            System.out.printf("%s (%s, %s commits):%n", author.getName(), author.getEmail(), author.getCommits().size());
            System.out.println("    Average commit message length: " + averageCommitMessageLength(author.getCommits()));
            System.out.println("    Average commit message words: " + averageCommitMessageWords(author.getCommits()));
            System.out.println("    Time analysis: " + timeAnalysis(author.getCommits()));
            System.out.println("    Percent with commit message: " + percentWithCommitMessage(author.getCommits()));
        }
    }

    public static float averageCommitMessageLength(List<Commit> commits) {
        int sum = 0;
        for (Commit commit : commits) {
            sum += commit.getMessage().length();
        }
        return sum / commits.size();
    }


    public static float averageCommitMessageWords(List<Commit> commits) {
        int sum = 0;
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
            } else if (firstWord.endsWith("d")){
                result.past++;
            } else {
                result.indetermined++;
            }
        }
        return result;
    }

    private static class TimeAnalysisResult {
        public int past;
        public int present;
        public int indetermined;

        @Override
        public String toString() {
            return "TimeAnalysisResult{" +
                    "past=" + past +
                    ", present=" + present +
                    ", indetermined=" + indetermined +
                    '}';
        }
    }

    public static float percentWithCommitMessage(List<Commit> commits) {
        int sum = 0;
        for (Commit commit : commits) {
            if (commit.getMessage() == null || commit.getMessage().matches("(\\s|-)*")){
                sum++;
            }
        }
        return 1 - sum / commits.size();
    }
}
