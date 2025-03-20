# iOS - Advertisement

This document describes everything relevant to the advertisement workflow in the iOS SDK.

To see the advertisement in action consider the following classes in the example app:

- AdvertisementView (displayed during the play of an advertisement)
- AdvertisementPlayer (playing the audio of the advertisement)
- AdPlayManager (control for displaying banner, starting audio, muting live stream )
- AudioSession (Registered for advertisement updates to pass to AdPlayManager)

## Relevant classes

- [Advertisements](docs/Protocols/Advertisements.html) - Protocol to interact with advertisements, get advertisements, receive updates and more
- [AdvertisementDelegate](docs/Protocols/AdvertisementDelegate.html) - Protocol to implement to receive information that a new amount of advertisements are available
- [MycrocastAdvertisement](docs/Classes/MycrocastAdvertisement.html) - represents a single advertisement with the URL of the audio to play as well as an title and optional advertisement banner related information

## Concepts

Everything related to advertisements is done via the Advertisements protocol. The SDK only provides the information that an advertisement is available as well as the information in that advertisement. Display of the provided images as well as playing the audio spot is the responsibility of the implementing side of the SDK.

### Receiving Advertisement Updates

The first step is to create a class that conforms to AdvertisementDelegate.

````swift
public protocol AdvertisementDelegate {
    /**
     Callback when a new number of advertisements is available
     Get the advertisement by calling getNextAdvertisement until no further ads are returned
      Otherwise call ignoreReceivedAdvertisements to remove them from the internal queue
     */
    func onAdvertisementAvailable()
}
````

The second step is to register the above created class with the Advertisements class to receive updates when an advertisement is available.

````swift
Mycrocast.shared.advertisements.addObserver(advertisementDelegate: self)
````

You are now able to receive updates to advertisements. 

This step should be done before you start a live stream, as the initial response (preStreamAdvertisement) could already contain an advertisement.

The advertisement push will not trigger the play of an the received advertisements, instead they are queued internally and wait for you to either retrieve them and play them or dequeue and ignore them.

The relevant functions from the Advertisements Protocol:

````swift
public protocol Advertisements {
  // other functions removed for brevity sake
    /**
      Get the next advertisement available, this should be called after receiving a push that a new amount of advertisements is present
      Call this again after playing the current ad to check if more ads are available to play.
      A single push of ads could contain multiple ads

      - returns: The next ad to play if any is present
    */
    func getAdvertisement() -> MycrocastAdvertisement?

    /**
     Ignore the advertisement push that was just receive, removing the ads from the internal playback queue.
      They cannot be retrieved anymore by the getAdvertisements call.
     */
    func ignoreReceivedAdvertisements()
}
````



##### Playing the advertisements

To receive and play advertisements call the getAdvertisement Function. After a push this function should be called in a loop with the following steps:

1. Check if advertisement is nil, if so you are done or proceed to step 2.
2. Play Advertisement
3. When advertisement is done repeat step 1.

##### Ignoring an advertisement

If you decide to not play the advertisement it is still in the internal queue and therefore will be returned the next time you call getAdvertisement, if this is not the behavior you want, then call ignoreReceivedAdvertisements after receiving an advertisement event.