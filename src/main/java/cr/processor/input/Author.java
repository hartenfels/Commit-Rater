package cr.processor.input;

import java.util.List;

public class Author {
    private String name;
    private String email;
    private List<Commit> commits;

    public Author() {}

    public Author(String name, String email, List<Commit> commits) {
        this.name = name;
        this.email = email;
        this.commits = commits;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", commits=" + commits +
                '}';
    }
}
