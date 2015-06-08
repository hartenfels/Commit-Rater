package cr.processor;

public class WhitespaceAnalysisResult {
    public static final float MIXED_THRESHOLD = 0.4f;
    public static final float WHITESPACE_THRESHOLD = 0.9f;

    public int codeChanges;
    public int mixedChanges;
    public int whitespaceChanges;

    public WhitespaceAnalysisResult() {
        codeChanges = 0;
        mixedChanges = 0;
        whitespaceChanges = 0;
    }

    @Override
    public String toString() {
        return "WhitespaceAnalysisResult{" +
                "codeChanges=" + codeChanges +
                ", mixedChanges=" + mixedChanges +
                ", whitespaceChanges=" + whitespaceChanges +
                '}';
    }
}
