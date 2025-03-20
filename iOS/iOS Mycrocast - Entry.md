# iOS Mycrocast - Entry 

The Mycrocast singleton is your entry point into anything related to the iOS mycrocast SDK.

It provides:

- The initialisation
- Access to the Chat control ([Chat](docs/Protocols/Chat.html))
- Access to the LiveStreams control ([Streams](docs/Protocols/Streams.html))
- Access to the Advertisements ([Advertisements](docs/Protocols/Advertisements.html))
- Access to the Rating control ([Rating](docs/Protocols/Rating.html))
- Access to the AudioStreams control ([SessionControl](docs/Protocols/SessionControl.html))

````swift
public final class Mycrocast {
    public static let shared = Mycrocast()

    /**
    Check if the session has already been established
     - returns: true if the session was successfully established, otherwise false
     */
    public var sessionEstablished: Bool {
        get {
            SDKAppState.shared.sdkSession.loggedIn
        }
    }

    /**
     Get the chat control.
     Use this to join a chat, get chat messages and participate in the chat of a stream
     */
    public var chat: Chat {
        get {
            SDKAppState.shared.chat
        }
    }

    /**
     Get the streams control
     Use this to get the current streams, register for changes to the current streams
     */
    public var streams: Streams {
        get {
            SDKAppState.shared.streamManager
        }
    }

    /**
     Get the session control
     Use this to control the session state, start playing a stream, stop a stream, configure delay
     See SessionControl for more details
     */
    public var sessionControl: SessionControl {
        get {
            SDKAppState.shared.session
        }
    }

    /**
     Get the rating control
      Use this to rate a stream
     */
    public var rating: Rating {
        get {
            SDKAppState.shared.rating
        }
    }

    /**
     Get the advertisements control
     Use this for registering to advertisements,
     get ads and provide statistics
     */
    public var advertisements: Advertisements {
        get {
            SDKAppState.shared.advertisements
        }
    }

    /**
     Start the SDK and connect to the server with the provided key and token.
     This function should be the first call before doing anything else otherwise all subsequent calls will fail
     Check the error field of the callback for any possible errors
     
     - Parameters:
        - apiKey: The api key provided for your account
        - customerToken: The token provided for your account
        - callback: callback that is executed after the setup is done, contains a list of any currently online streamers for your club or an error if something went wrong
        - streams: a list of all currently available live streams or none if no one is currently online
        - error: contains any ``MycrocastError`` error that occurred during setup of the SDK
     */
    public func start(_ apiKey: String, customerToken: String, callback: @escaping (_ streams: [LiveStream], _ error: MycrocastError?) -> ()) 

   /**
     Terminate the SDK cleaning up any internal resources
     */
    public func terminate() 
````



