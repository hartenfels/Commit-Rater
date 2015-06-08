package cr.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cr.processor.input.Author;
import cr.processor.input.Commit;

import java.io.*;
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
    public WhitespaceAnalysisResult whitespaceAnalysisResult;

    public AuthorData(Author author, String local) {
        name = author.getName();
        email = author.getEmail();

        List<Commit> commits = author.getCommits();

        this.commits = commits.size();
        merges = filterMerges(commits).size();
        averageCommitMessageLength = averageCommitMessageLength(commits);
        averageCommitMessageWords = averageCommitMessageWords(commits);
        commitMessageWords = commitMessageWords(commits);
        percentageOfMerges = percentMerges(commits);
        timeAnalysis = timeAnalysis(commits);
        commitsWithMessage = percentWithCommitMessage(commits);
        commitsWithTrailingPeriod = percentWithTrailingPeriod(commits);
        averageFileActions = averageFileActions(commits);
        try {
            whitespaceAnalysisResult = whitespaceAnalysisResult(commits, local);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        return ((float) filterMerges(commits).size()) / commits.size();
    }

    /**
     * Removes all commits that are *not* merges
     * @param commits A list of commits with commit messages
     * @return A list of merges
     */
    public static List<Commit> filterMerges(List<Commit> commits) {
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

    public static WhitespaceAnalysisResult whitespaceAnalysisResult(List<Commit> commits, String local) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(new String[]{"carton", "exec", "./give-files", "-c", "1000000", "-r", local});
        BufferedReader diffToolReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedWriter diffToolWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        ObjectMapper mapper = new ObjectMapper();

        WhitespaceAnalysisResult whitespaceAnalysisResult = new WhitespaceAnalysisResult();
        for (Commit commit : commits) {
            float numberOfWSChanges = 0;
            int linesChanged = 0;

            if (commit.getParents().length > 1) {
                //System.out.println(commit.getSha() + " has more than one parent, skipping");
                continue; // Skip merges
            }

            // Iterate over all changed files in commit
            for (String fileName : commit.getDiff().keySet()) {
                if (!commit.getDiff().get(fileName).equals("M")) {
                    continue; // Skip files that are created or deleted
                }

                // Prepare input for external diff tool
                String out = commit.getParents()[0]
                        + "\0"
                        + commit.getSha()
                        + "\0"
                        + fileName
                        + "\n";
                diffToolWriter.write(out);
                diffToolWriter.flush();

                // Get diff from external diff tool
                StringBuilder diffToolInput = new StringBuilder();
                String line = "";
                try {
                    while (!(line = diffToolReader.readLine()).contains("\0")) {
                        diffToolInput.append(line);
                    }
                } catch (NullPointerException e) {
                    BufferedReader errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String eline;
                    while ((eline = errorStream.readLine()) != null) {
                        System.err.println(eline);
                    }
                    throw e;
                }
                List<LineDiff> lineDiffs = mapper.readValue(diffToolInput.toString(), new TypeReference<List<LineDiff>>() {});

                // For every line test if the change made was a whitespace change
                for (LineDiff lineDiff : lineDiffs) {
                    String newTrimmed = lineDiff.getNewLine().replaceAll("\\s", "");
                    String oldTrimmed = lineDiff.getOldLine().replaceAll("\\s", "");
                    switch (lineDiff.getOp()) {
                        case "c": // Changed
                            // lineDiff.getNewLine() != lineDiff.getOldLine() is given
                            if (newTrimmed.equals(oldTrimmed)) {
                                numberOfWSChanges++;
                            }
                            break;
                        case "+": // Added. oldLine == ""
                            if (newTrimmed.equals("")) {
                                // If the new line is WS only, the change was a WS change
                                numberOfWSChanges++;
                            }
                            break;
                        case "-": // Deleted. newLine == ""
                            if (oldTrimmed.equals("")) {
                                // If the old line was WS only, the change was a WS change
                                numberOfWSChanges++;
                            }
                            break;
                    }
                    linesChanged++;
                }
            }

            // Categorize commit according to thresholds
            float percentWhitespaceChanges = numberOfWSChanges / linesChanged;
            if (percentWhitespaceChanges > WhitespaceAnalysisResult.WHITESPACE_THRESHOLD) {
                whitespaceAnalysisResult.whitespaceChanges++;
            } else if (percentWhitespaceChanges > WhitespaceAnalysisResult.MIXED_THRESHOLD) {
                whitespaceAnalysisResult.mixedChanges++;
            } else {
                whitespaceAnalysisResult.codeChanges++;
            }
        }
        diffToolReader.close();
        diffToolWriter.close();
        return whitespaceAnalysisResult;
    }
}
