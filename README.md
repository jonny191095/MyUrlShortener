# URL Shortener
A simple URL shortener built using springboot and using a Redis in memory store within a Docker container.

## Overview
Application provides 2 functions - taking a long URL and mapping it to a short URL, and taking a shortened URL and redirecting the user to the mapped long URL.

- <b>/shortener</b> post request take a single URL string request body, validates it, and outputs a shortened URL by way of a hash function. The resulting hash is only 8 characters long and is used as the short URL. The hash function ensures that identical input result in identical outputs, thus ensuring no redundant duplication.

- <b>/{id}</b> get request takes in the short URL as the path variable <b>id</b>, and redirects the user's browser to the mapped long url.

The application has a configurable property <b>"redis.ttl"</b> which defines the time to live of the short URL in seconds (default 86400s - 24 hours).

The application also has rate limiting on the /shortener endpoint to prevent excessive requests.

## Deployment
Currently the application has only been run locally but it is designed to be portable and transferable to the cloud without any internal code changes.
To run locally, a Redis image will need to be running within a Docker container.
For potential future cloud deployment, a service such as AWS ECS would be recommended.

## Future Enhancements
Future developement would include adding a nosql database for longer term data persistence (e.g mongoDB), in this case the Redis store could be used as a cache for younger or more popular URLs.
Developing a simple front-end interface to allow the user to type their long URL into a textbox with it's own validation. In this case a layer of security could be added where only api calls made from the front-end application are accepted.
