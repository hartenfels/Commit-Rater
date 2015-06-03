package cr.processor;

class TimeAnalysisResult {
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