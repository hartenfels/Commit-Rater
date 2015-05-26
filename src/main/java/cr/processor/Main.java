package cr.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

class Main {
    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Author> authors = mapper.readValue(new File("out/cr.json"), new TypeReference<List<Author>>() {});
        for (Author author : authors) {
            System.out.printf("%s (%s, %s commits):%n", author.getName(), author.getEmail(), author.getCommits().size());
            System.out.println("    Average commit length: " + averageCommitMessageLength(author.getCommits()));
        }
    }

    public static float averageCommitMessageLength(List<Commit> commits) {
        int sum = 0;
        for (Commit commit : commits) {
            sum += commit.getMessage().length();
        }
        return sum / commits.size();
    }
}
