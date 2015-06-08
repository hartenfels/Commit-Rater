package cr.processor.input;

import java.util.List;

public class FetchInput {
    private String remote;
    private String local;
    private List<Author> authors;

    public String getLocal() {
        return local;
    }
    public void setLocal(String local) {
        this.local = local;
    }
    public String getRemote() {
        return remote;
    }
    public void setRemote(String remote) {
        this.remote = remote;
    }
    public List<Author> getAuthors() {
        return authors;
    }
    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }
}
