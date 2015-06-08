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
        // Read input from fetcher
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder input = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            input.append(line);
        }
        ObjectMapper mapper = new ObjectMapper();
        FetchInput fetchInput = mapper.readValue(input.toString(), FetchInput.class);
        List<AuthorData> authorDatas = new ArrayList<>();

        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(new String[]{"carton", "exec", "./give-files", "-r", "--cap", "1000000", fetchInput.getLocal()});
        BufferedReader diffToolReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedWriter diffToolWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        // Analyze authors
        for (Author author : fetchInput.getAuthors()) {
            if (author.getCommits().size() < 8) {
                continue;
            }
            authorDatas.add(new AuthorData(author));

            // Diff analysis
            for (int c = 1; c < author.getCommits().size(); c++) {
                Commit commit = author.getCommits().get(c);
                if (commit.getParents().length > 1) {
                    System.out.println(commit.getSha() + " has more than one parent, skipping");
                    continue; // Skip merges
                }

                for (String fileName : commit.getDiff().keySet()) {
                    if (!commit.getDiff().get(fileName).equals("M")) {
                        continue; // Skip files that are created or deleted
                    }

                    String out = commit.getParents()[0]
                            + "\0"
                            + commit.getSha()
                            + "\0"
                            + fileName
                            + "\n";
                    System.out.printf("%n----%s%n", out);
                    diffToolWriter.write(out);
                    diffToolWriter.flush();

                    StringBuilder diffToolInput = new StringBuilder();
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
                    System.out.println("lineDiffs = " + lineDiffs);
                }
            }
        }

        //System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(authorDatas));

        diffToolReader.close();
        diffToolWriter.close();
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
//        for (Author author : occurrancesPerAuthor.keySet()) {
//            System.out.println("occurrance[" + author.getName() + "] = " + occurrancesPerAuthor.get(author));
//        }

        int maxWordCount = 0;
        for (Author author : occurrancesPerAuthor.keySet()) {
            for (Integer wordCount : occurrancesPerAuthor.get(author).keySet()) {
                if (wordCount > maxWordCount) {
                    maxWordCount = wordCount;
                }
            }
        }
        int numberOfAuthors = occurrancesPerAuthor.keySet().size();

        //System.out.println("maxWordCount = " + maxWordCount);

        // Initialize the table
        String[][] table = new String[maxWordCount + 1 + 1][];
        for (int i = 0; i < maxWordCount + 1 + 1; i++) {
            table[i] = new String[numberOfAuthors + 1];

            for (int j = 0; j < numberOfAuthors + 1; j++) {
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
