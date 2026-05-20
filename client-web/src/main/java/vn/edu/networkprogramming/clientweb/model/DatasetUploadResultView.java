package vn.edu.networkprogramming.clientweb.model;

public record DatasetUploadResultView<T>(
        T dataset,
        boolean reused
) {
    public T getDataset() { return dataset; }
    public boolean isReused() { return reused; }
}
