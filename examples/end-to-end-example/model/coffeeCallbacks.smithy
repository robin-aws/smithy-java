$version: "2"

namespace com.example

use aws.protocols#restJson1

@restJson1
service CoffeeShopCallbacks {
    resources: [
        CompletedCallback
    ]
}

resource CompletedCallback {
    identifiers: {
        callbackId: String
    }
    operations: [
        NotifyCompleted
    ]
}

/// Callback operation triggered when an order is completed,
/// if requested in CreateOrder
@http(method: "POST", uri: "/completed")
operation NotifyCompleted {
    input := for CompletedCallback {
        @required
        $callbackId

        @required
        orderId: Uuid
    }

    output := {
        @range(min: 1, max: 5)
        @notProperty
        starRating: Integer
    }
}

// This should be in some shared prelude model with a more generic name.
// Calling it something other than "Endpoint" to avoid conflicting with the
// Endpoint class in Smithy core.
structure CallbackEndpoint {
    @required
    url: String

    channelUrl: String
}
