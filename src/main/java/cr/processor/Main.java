package cr.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder input = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            input.append(line);
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Author> authors = mapper.readValue(input.toString(), new TypeReference<List<Author>>() {});
        for (Author author : authors) {
            if (author.getCommits().size() < 8) {
                System.out.printf("Skipping author %s with only %s commits%n", author.getName(), author.getCommits().size());
                continue;
            }
            System.out.printf("%s (%s, %s commits including %s merges):%n", author.getName(), author.getEmail(), author.getCommits().size(), removeMerges(author.getCommits()).size());
            System.out.println("    Average commit message length: " + averageCommitMessageLength(author.getCommits()));
            System.out.println("    Average commit message words: " + averageCommitMessageWords(author.getCommits()));
            System.out.println("    Time analysis: " + timeAnalysis(author.getCommits()));
            System.out.println("    Percent with commit message: " + percentWithCommitMessage(author.getCommits()));
            System.out.println("    Percent with trailing period: " + percentWithTrailingPeriod(author.getCommits()));
            System.out.println("    Percent merges: " + percentMerges(author.getCommits()));
            System.out.println("    Average file actions: " + averageFileActions(author.getCommits()));
        }
        wordRanksPlot(authors);
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

    public static void wordRanksPlot(List<Author> authors) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<Author, Map<Integer, Integer>> occurrancesPerAuthor = new HashMap<>();

        for (Author author : authors) {
            Map<Integer, Integer> occurrances = new TreeMap<>(); // Number of words -> Messages with that number of words
            for (Commit commit : author.getCommits()) {
                int words = commit.getMessage().split("\\s+").length;
                if (occurrances.get(words) != null) {
                    occurrances.put(words, occurrances.get(words) + 1);
                } else {
                    occurrances.put(words, 1);
                }
            }

            occurrancesPerAuthor.put(author, occurrances);

            for (Integer words : occurrances.keySet()) {
                dataset.addValue(occurrances.get(words), author.getName(), words);
            }

            //System.out.println("occurrances = " + occurrances);
        }

        occurrancesPerAuthorToCSV(occurrancesPerAuthor);

        JFreeChart lineChart = ChartFactory.createLineChart3D("Commit message words per rank.", "ranks", "words", dataset);
        BufferedImage bufferedImage = lineChart.createBufferedImage(1028, 726);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        InputStream in = new ByteArrayInputStream(byteArray);
        BufferedImage image = ImageIO.read(in);
        File outputfile = new File("image.png");
        ImageIO.write(image, "png", outputfile);
    }

    public static void occurrancesPerAuthorToCSV(Map<Author, Map<Integer, Integer>> occurrancesPerAuthor) {
        for (Author author : occurrancesPerAuthor.keySet()) {
            System.out.println("occurrance[" + author.getName() + "] = " + occurrancesPerAuthor.get(author));
        }

        int maxWordCount = 0;
        for (Author author : occurrancesPerAuthor.keySet()) {
            for (Integer wordCount : occurrancesPerAuthor.get(author).keySet()) {
                if (wordCount > maxWordCount) {
                    maxWordCount = wordCount;
                }
            }
        }
        int numberOfAuthors = occurrancesPerAuthor.keySet().size();

        System.out.println("maxWordCount = " + maxWordCount);

        // Initialize the table
        String[][] table = new String[maxWordCount + 1 + 1][];
        for (int i = 0; i < maxWordCount + 1 + 1; i++) {
            table[i] = new String[numberOfAuthors + 1];

            for(int j = 0; j < numberOfAuthors + 1; j++) {
                if (j == 0) {
                    table[i][j] = i - 1 + "";
                } else {
                    table[i][j] = "0";
                }
            }
        }

        table[0][0] = "Rank";
        int a = 1;
        for (Author author : occurrancesPerAuthor.keySet()) {
            table[0][a] = author.getName();
            for (Integer wordCount : occurrancesPerAuthor.get(author).keySet()) {
                table[wordCount + 1][a] = occurrancesPerAuthor.get(author).get(wordCount).toString();
            }
            a++;
        }

        try {
            toCSV(table, "wordCounts.csv");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void toCSV(String[][] table, String filename) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(filename, "UTF-8");
        for (String[] row : table) {
            writer.println(StringUtils.join(row, ","));
        }
        writer.close();
    }
}
