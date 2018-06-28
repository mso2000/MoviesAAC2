# Simplified Popular Movies Stage 2 (with AAC)

This is a simplified version of **"Popular Movies Stage 2"** project from [Udacity Android Developer Nanodegree](https://udacity.com/course/android-developer-nanodegree-by-google--nd801) using [Android Architecture Components](https://developer.android.com/topic/libraries/architecture/).

There's no complete UI or images, just plain movie titles are displayed and some buttons are provided to toggle sort criteria and simulate some database manipulation.

The main idea is use the benefits of `LiveData`, to observe database changes and automatically refresh UI (replacing usual `CursorLoader` and `ContentProvider`), and benefits of `ViewModel` to cache this `LiveData` and some app states like movie list and sort criteria.

**OBS:** It's necessary to provide a valid TMDB API key in `gradle.properties` file at project root. You can get one at https://developers.themoviedb.org/3
