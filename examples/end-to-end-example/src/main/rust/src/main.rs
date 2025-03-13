

use std::net::SocketAddr;
use tower::ServiceBuilder;

use coffee_shop_sdk::{CoffeeShop, CoffeeShopConfig};

#[tokio::main]
pub async fn main() {
    let config = CoffeeShopConfig::builder()
        .build();

    let app = CoffeeShop::builder(config)
        .create_order(handler)
        .build()
        .expect("failed to build an instance of PokemonService");

    // Using `into_make_service_with_connect_info`, rather than `into_make_service`, to adjoin the `SocketAddr`
    // connection info.
    let make_app = app.into_make_service_with_connect_info::<SocketAddr>();

    // Bind the application to a socket.
    let bind: SocketAddr = "unix:/..."
        .parse()
        .expect("unable to parse the server bind address and port");
    let server = hyper::Server::bind(&bind).serve(make_app);

    // Run forever-ish...
    if let Err(err) = server.await {
        eprintln!("server error: {}", err);
    }
}