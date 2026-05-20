package vn.edu.networkprogramming.assignserver.model;

public record DatasetUploadResult<T>(
        T dataset,
        boolean reused
) {
}
