package lt.vu.tube.model;

public class LambdaResponse<T> {
    private Integer statusCode;
    private T body;

    public LambdaResponse() {}

    public LambdaResponse(Integer statusCode, T body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
