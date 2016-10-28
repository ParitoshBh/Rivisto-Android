#Rivisto - Android Application

This is the repository for development of android app counterpart for Rivisto Desktop ([available here](https://github.com/ParitoshBh/Rivisto)).

#Progress

As of now, the first alpha release for android is being worked upon and application should be available in Google Play Store pretty soon!

#How Do I Get Started?

There are 2 ways to use Rivisto Android app,

1. Use your own Firebase account (for those who want complete control and are willing to do the initial setup).

1. Use a managed (Firebase) account and follow the simple username/password login/setup style (for those with lesser technical bent).

If you intend to use the managed account, then please note that you will not be able to access the database directly (as against using your own Firebase account).

And if you you plan on using your own Firebase account, follow steps mentioned below to get started,

- Setup Firebase Account (i.e. Database) - Rather than confusing you with text instructions, I have made a quick video showing you how to setup a Firebase account.

[![Setup Firebase Account](https://img.youtube.com/vi/O6ALgl_EiVU/0.jpg)](https://www.youtube.com/watch?v=O6ALgl_EiVU)

- In Firebase console, go to 'Rules' tab, and replace them with following,

```
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```


- Download Android application from Google Play Store.

- Enter the account details (from Step 1) by clicking on 'Manual Config' in overflow menu.

That's it! You should be able to use Rivisto for saving notes. And remember, Rivisto acts as a way to manage raw data, keep it organized. If you feel the need to see raw data, simply log back to your Firebase account.

_Tip - To make all of this process easier, first setup desktop app ([as mentioned here](https://github.com/ParitoshBh/Rivisto#how-do-i-get-started)) and then connect android app with desktop app using one tap configure option in android app._

#Feature List

As of now, Rivisto is barebones, with an incomplete UI but fairly stable features,

- Save, Edit, and Delete notes.
- Trash view to restore/permanently delete notes.
- Note tagging by using # with any word in note content (Only first tag is used).
- Reset application in case things are not working the way they should (note that all notes remain intact).
- Note categorization by Tag.
- Searching for note(s).
- Auto generation of note title based on note content.

#Interested In Contributing?

Things are quite easy. Rivisto android app development uses following setup,

- Android Studio (I am using version 2.2.2)
- Firebase account (google-services.json file is additionally required to handle the managed account section of app)

As of now, I am not accepting any new feature pull requests since focus is on testing what's already there, and push the first version of Rivisto, stable enough for daily use. This being said, pull requests for refactored code are highly appreciated and so are tests.

#Issues, Problems, Errors Are Welcome

If you face any problem, create an issue here or create a new post on StackOverflow with _#Rivisto_. I am always on the lookout for improvising (and constructive criticism).
