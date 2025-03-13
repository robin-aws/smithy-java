$version: "2"

namespace com.example

use aws.protocols#restJson1

/// Allows CoffeeShop users to provide a callback notification
/// when their coffee is ready
// TODO: rpcv2Cbor would be much better and avoid needing the @http traits,
// but at the time I started this POC
// smithy-java didn't have that implemented client-side yet.
// A big advantage of this approach is that new protocols can be
// swapped in later on without changing client or server interface though!
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
    delete: DeleteCallback
    operations: [
        NotifyCompleted
    ]
}

/// Callback operation triggered when an order is completed,
/// if requested in CreateOrder
@http(method: "POST", uri: "/completed/{callbackId}")
operation NotifyCompleted {
    input :=
        @references([
            {
                resource: CompletedCallback
            }
        ])
        for CompletedCallback {
            @required
            @httpLabel
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

/// Deletes a callback.
/// CoffeeShop will automatically delete a callback
/// after successfully invoking it.
@idempotent
@http(method: "DELETE", uri: "/{callbackId}")
operation DeleteCallback {
    input :=
        @references([
            {
                resource: CompletedCallback
            }
        ])
        for CompletedCallback {
            @required
            @httpLabel
            $callbackId
        }
}
