$version: "2"

namespace com.example

// This should be in some shared prelude model with a more generic name.
// Calling it "ResolvedEndpoint" because it should be the result
// of evaluating any templates in a smithy.rules#Endpoint.
// (https://smithy.io/2.0/additional-specs/rules-engine/specification.html#endpoint-object)
// It also helps avoid conflicts with the existing smithy.api#endpoint trait
// and the existing Endpoint class in smithy-java.
structure ResolvedEndpoint {
    @required
    url: String

    channelUrl: String

    properties: EndpointPropertiesMap

    // TODO: Should include authSchemes and headers too as in
    // https://smithy.io/2.0/additional-specs/rules-engine/specification.html#endpoint-object
}

map EndpointPropertiesMap {
    key: String
    value: Document
}
