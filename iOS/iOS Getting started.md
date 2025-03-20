# iOS Getting started

To get started with the SDK the following steps are required.

## Requirements


#### Adding the framework yourself

If you cannot use the below described approach download the xcframework directly or get in touch with us!

#### Using Swift Package Manager

The framework is distributed via SwiftPackageManager.

Just add the following repository:
https://github.com/mycrocast/mycrocastSwiftSDK

The [example app](https://github.com/mycrocast/mycrocast-ios-sdk-example) uses this approach

## Configure the SDK

The last step is to import the SDK and call the start function with your credentials.

````swift
import MycrocastSDK

override func viewDidLoad() {
        super.viewDidLoad();

        // start the SDK with your credentials
        Mycrocast.shared.start(ViewController.YOUR_API_KEY, customerToken: ViewController.YOUR_CUSTOMER_ID) { streams, error in
            if let error = error {
                print(error)
                return
            }
            // we received a successful response from the server without any errors
            // we now have all currently streaming streamers of the club in the streams list
            // we can also access this from the SDKs streamManager
            for stream in streams {
                self.onStreamAdded(stream)
            }
        }

		... other setup code
    }
````

The result contains the current [LiveStreams](docs/Classes/LiveStream.html) or any error if something happened.

The call to start needs to be the first thing you do before anything else.

An example of this can be seen in the [ViewController](app/ViewController.html) class of the provided app implementation.

The next steps are: 

- Make yourself familiar with the [Mycrocast class](iOS Mycrocast - Entry.html)
- [Getting the livestreams, receiving updates](iOS LiveStream.html)
- [Playing from a live stream](iOS Connecting to a live stream.html)
- [Chatting](iOS Chat.html)
- [Advertisement Management](iOS Advertisement.html)

For more details about the different classes see the [api documentation](docs/index.html). 

The different errors are described [here](iOS Errors.html).





