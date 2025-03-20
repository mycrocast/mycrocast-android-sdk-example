# Android getting started

To get started with the sdk the following steps are required.

## 1. Importing the library to your application

#### 1.1 Adding the maven repository (as gradle-import)

This is the url of the maven repository for the mycrocast android SDK:
``` 
https://mycrocast-maven-repo.s3.eu-central-1.amazonaws.com 
```

You need to add the following to your build.gradle file of your project:
``` java
//...

allprojects {  
	repositories {  
		//...

		maven {  
			url "https://mycrocast-maven-repo.s3.eu-central-1.amazonaws.com"  
		}  
	}
}
 
//...
```

And you need to add the following to your build.gradle file of your module:
``` java
dependencies {  

	//...

	implementation 'de.mycrocast.library:library:1.3.0'
}
```

The example application for importing the library with gradle can be found [here](https://github.com/mycrocast/mycrocast-android-sdk-example).

#### 1.2 Manually adding as aar library
If you cannot use the maven approach above and need the aar file. Get in touch with us!

## 2. Permissions

This SDK needs to communicate with the mycrocast backend-server via rest-communication, therefore you need to make sure you have the internet-permission in your AndroidManifest.

```java
<?xml version="1.0" encoding="utf-8"?>  
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="de.mycrocast.android.sdk.example">  
	// ...
	
	<uses-permission android:name="android.permission.INTERNET" />
	
	// ...
```

## 3. Initialising the sdk

The first thing you need to do before you can use any other functionality of the sdk is to initialise it with your api key and your customer token. This can be done for example the onCreate of the Application class

````java
/**
 * Starting point of the application, also the point where we initialise the sdk
 */
public class MycrocastExampleApplication extends Application {

	// replace with your api key
    private static final String MUSTER_API_KEY = "YOUR_API_KEY";
    
    // replace with your customer token
    private static final String MUSTER_CUSTOMER_TOKEN = "YOUR_CUSTOMER_TOKEN";

	// replace with the maximum number of seconds for the audio delay you want to support
	private static final int MAX_AUDIO_DELAY = 30;

    @Override
    public void onCreate() {
        super.onCreate();

		// initialize with the sdk delay
		Mycrocast.initialize(API_KEY, CUSTOMER_TOKEN, PreferenceManager.getDefaultSharedPreferences(this), MAX_AUDIO_DELAY);
    }
}
````

You are ready to go.

The next steps are: 

- [Getting familiar with the Mycrocast class](Android Mycrocast - Entry.html)
- [Requesting active livestreams, receiving updates for them](Android LiveStreams.html)
- [Playing audio of a livestream](Android Playing a LiveStream.html)
- [Getting and playing Advertisements](Android Advertisement.html)
- [Chat and ChatMessages](Android Chat.html)
- [ErrorTypes and receiving of Errors of the SDK](Android Errors.html)

