# iOS Errors

Errors through the iOS SDK are present in the [MycrocastError](docs/Classes/MycrocastError.html) class, this consist of 2 fields, an errorType (enum describing the error type) and a description.

The description should help the developer determine what was wrong as it contains more information.

The [ErrorType](docs/Enum/ErrorType.html) looks like the following which is directly taken from the code

```swift
public enum ErrorType {
    // authentication of the SDK failed, this is
    // most likely because of a wrong customer token
    case authentication
    // Error based on losing connection
    case connection
    // A security error is either thrown because you try to use
    // the SDK without initialising it or the provided api key during
    // initialisation is wrong
    case security
    // Stream exceptions are normally thrown when a could not be found based on the provided
    // streamId, this can be because the id you try to use is wrong, or the streamer just ended the stream and is
    // therefore not longer in the system
    // the stream error is more like an information as you cannot do anything about it
    case stream
    // generic exception normally a wrapper around an NSException
    // the content of the exception is present in the description
    case generic
}

```



## Receiving Errors

Some functions have in their closure an optional error field that can be checked. 

Refer to the AppState class of the example app (although errors are only printed there)

For the general error workflow you need to create a class that conforms to the ErrorReceiving protocol and register the class with the sessionControl of the mycrocast SDK

```swift
public protocol SessionControl {
 	// removed other functions for brevity sake

    /**
     Add a delegate as observer to receive errors propagated from the system
     - Parameter errorDelegate:
     */
    func addObserver(errorDelegate: ErrorReceiving)
    /**
     Remove a delegate as observer from receiving any more MycrocastErrors
     - Parameter errorDelegate:
     */
    func removeObserver(errorDelegate: ErrorReceiving)
}
```



