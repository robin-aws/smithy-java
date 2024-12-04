package software.amazon.smithy.java.server.core;

public final class InMemoryResponse extends ResponseImpl {
    // TODO: Introduce type parameters, and/or dynamic type saftey checks ala client protocols
    private Object serializedValue;

    public InMemoryResponse() {
    }

    public Object getSerializedValue() {
        return serializedValue;
    }

    public void setSerializedValue(Object serializedValue) {
        this.serializedValue = serializedValue;
    }
}
