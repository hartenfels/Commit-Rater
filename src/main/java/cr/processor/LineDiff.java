package cr.processor;

public class LineDiff {
    private String op;
    private String oldLine;
    private String newLine;

    @Override
    public String toString() {
        return "LineDiff{" +
                "type='" + op + '\'' +
                ", oldLine='" + oldLine + '\'' +
                ", newLine='" + newLine + '\'' +
                '}';
    }
    public String getOp() {
        return op;
    }
    public void setOp(String op) {
        this.op = op;
    }
    public String getOldLine() {
        return oldLine;
    }
    public void setOldLine(String oldLine) {
        this.oldLine = oldLine;
    }
    public String getNewLine() {
        return newLine;
    }
    public void setNewLine(String newLine) {
        this.newLine = newLine;
    }
}
