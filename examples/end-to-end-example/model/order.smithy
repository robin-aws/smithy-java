$version: "2.0"

namespace com.example

/// An Order resource, which has an id and describes an order by the type of coffee
/// and the order's status
resource Order {
    identifiers: {
        id: Uuid
    }
    properties: {
        coffeeType: CoffeeType
        status: OrderStatus
    }
    read: GetOrder
    create: CreateOrder
}

/// Create an order
@idempotent
@http(method: "PUT", uri: "/order")
operation CreateOrder {
    input := for Order {
        @required
        $coffeeType

        @notProperty
        callbackEndpoint: CallbackEndpoint

        @notProperty
        callbackId: String
    }

    output := for Order {
        @required
        $id

        @required
        $coffeeType

        @required
        $status
    }
}

/// Retrieve an order
@readonly
@http(method: "GET", uri: "/order/{id}")
operation GetOrder {
    input := for Order {
        @httpLabel
        @required
        $id
    }

    output := for Order {
        @required
        $id

        @required
        $coffeeType

        @required
        $status
    }

    errors: [
        OrderNotFound
    ]
}

/// An error indicating an order could not be found
@httpError(404)
@error("client")
structure OrderNotFound {
    message: String
    orderId: Uuid
}

/// An identifier to describe a unique order
@length(min: 1, max: 128)
@pattern("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$")
string Uuid

/// An enum describing the status of an order
enum OrderStatus {
    IN_PROGRESS
    COMPLETED
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

// This should be in some shared prelude model with a more generic name.
// Calling it something other than "Endpoint" to avoid conflicting with the
// Endpoint class in Smithy core.
structure CallbackEndpoint {
    @required
    url: String

    channelUrl: String
}
