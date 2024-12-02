$version: "2"

namespace com.example

use smithy.protocols#inmemoryv1Cbor

/// Allows users to retrieve a menu, create a coffee order, and
/// and to view the status of their orders
@title("Coffee Shop Service")
@inmemoryv1Cbor
service CoffeeShop {
    version: "2024-08-23"
    operations: [
        GetMenu
    ]
    resources: [
        Order
    ]
}

/// Retrieve the menu
operation GetMenu {
    output := {
        items: CoffeeItems
    }
}
