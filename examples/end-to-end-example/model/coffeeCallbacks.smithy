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

        @required
        coffeeType: CoffeeType
    }

    output := {
        @range(min: 1, max: 5)
        @notProperty
        starRating: Integer
    }
}
