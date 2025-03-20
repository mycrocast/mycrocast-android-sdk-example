# SDK Errors

The sdk provides a number of errors that are all presented in the [MycrocastError](documentation/de/mycrocast/android/sdk/error/MycrocastError.html) class. 

The class consist of an enum value of type [ErrorType](documentation/de/mycrocast/android/sdk/error/ErrorType.html) and a description providing more information.

The error types are described below as taken directly from the implementation.

``` java
public enum ErrorType {
    // error during the initialisation with the customer token
    // check if you entered it correctly
    AUTHENTICATION,
	
    // error with a stream interaction. This normally means that either the stream
    // with the id could not not be found, or that the stream was removed beforehand (the streamer
    // could went offline) This is more for information only as the user cannot do anything about it
    STREAM,
	
    // security problem because of not authorised to do something
    // this should only occur if you try to do something without first initialising the sdk
    // or your provided api key is not valid
    SECURITY,
	
    // the server response was unexpected
    // normally a status code between 4xx..5xx this indicates unusual behaviour and should
    // result in informing the user and maybe try later again
    // the description of the mycrocast error contains the status code and more information if available
    SERVER,
	
    // a generic exception internally occurred, for example trying to communicate while no internet
    // connection is available
    // the description of the mycrocast error contains the message of the throwable
    GENERIC,
	
    // for internal use only
    NO_CONTENT
}
```

## Receiving Errors
You have the possibility to get informed, if an error occurred in the SDK.
Therefore you need to implement the ErrorReceiving.Observer:
``` java
/**  
* Interface that will be informed, if an error occurs somewhere in the sdk.
*/
public interface ErrorReceiving extends Observable<ErrorReceiving.Observer> {  
  
	/**  
	* The interface to be implemented to register as observer. 
	* Here you will receive any potential error message 
	*/ 
	interface Observer {  

		/**  
		* A new error was received from the sdk 
		* @param error - the error that was send from the sdk  
		*/
		void onError(MycrocastError error);  
	}
}
```

And add this observer to the ErrorReceiver of our SDK via:
```java
// add observer for receiving any error occurring in the sdk  
Mycrocast.getErrorReceiving().addObserver(this);
```