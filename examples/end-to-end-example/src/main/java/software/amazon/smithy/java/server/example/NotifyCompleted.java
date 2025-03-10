package software.amazon.smithy.java.server.example;

import software.amazon.smithy.java.example.model.NotifyCompletedInput;
import software.amazon.smithy.java.example.model.NotifyCompletedOutput;
import software.amazon.smithy.java.example.service.NotifyCompletedOperation;
import software.amazon.smithy.java.server.RequestContext;

public class NotifyCompleted implements NotifyCompletedOperation {

    @Override
    public NotifyCompletedOutput notifyCompleted(NotifyCompletedInput input, RequestContext context) {
        throw new UnsupportedOperationException();
    }
}
