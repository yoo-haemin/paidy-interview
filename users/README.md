# My solution to `User` exercise

## How to use
### Signup
- http: `POST /users`
- body: Json. Example below :
  {
	"userName": "me",
	"emailAddress": "example@example.com",
	"password": "some_strong_password"
  }
- password is optional

### Get by UserName
- http: `GET /users/:userName`, where `:userName` is the user's chosen username

### Update Email
- http: `POST /users/:id/email`, where `:id` is the user's id, as seen from the `id` field from user responses.
- body: a string containing the desired email address

### Update Password
- http: `POST /users/:id/password`, where `:id` is the user's id, as seen from the `id` field from user responses.
- body: a string containing the desired password

### Reset Password
- http: `DELETE /users/:id/password`, where `:id` is the user's id, as seen from the `id` field from user responses.

### Delete user
- http: `DELETE /users/:id`, where `:id` is the user's id, as seen from the `id` field from user responses.

### Block user
- http: `POST /users/:id/block`, where `:id` is the user's id, as seen from the `id` field from user responses.

### Unblock user
- http: `POST /users/:id/unblock`, where `:id` is the user's id, as seen from the `id` field from user responses.

### Get all users
- http: `POST /users
- body: A JSON array containing the representations of all current users.

## Dependencies
### Http4s
- I was using it lately for a personal hobby project
- Sunday's talk has reminded me of a few concepts, like
  1) preferring having parameters (for both type and value) over writing concrete values
  2) preferring the least powerful abstraction that gets the job done

### Circe
- Integrates well with http4s
- Didn't use auto derivation for models - while convenient, it couples internal data description with exteral api's, which is not preferred. 
  - I do know of a [talk](https://www.youtube.com/watch?v=oRLkb6mqvVM) that addresses this issue, but the library he described in the talk is no longer maintained, nor is it compatible with current dependencies (depends on Scalaz and Argonaut). 
  - Even if it was viable, adding a dependency for such a small model would be an overkill.
- I did use auto derivation once ([here](src/main/scala/users/endpoints/UnprevilegedEndpoint.scala)) for decoding signup requests though, since creating a simple case class and relying on auto derivation was less boilerplate than writing a custom decoder.

## Design
### Assumptions
- I treated the given code as given as in library, so I tried building upon them, rather than directly modifying them.
  - I've created an `IO` wrapper for the service layer using `Future`s.
  - There actually was a bug at InMemoryRepository, so I did fix that to make the whole app working. (That might actually warrant a small PR.)
  - I created a getByUserName method at the Algebra, since users not being able to fetch their accounts with their username is most certainly not wanted.

### Goals
- Make it work
- Utilize of functional techniques for modularity and less boilerplate
- Make endpoints simple

### Results
#### Overall
- It works.
- Most of my code is parameterized by `F[_]: Effect`, rather than relying on concrete types like `IO`, `Task` or `Future`
- I have scrapped many boilerplates at the endpoints.
- Endpoints mostly directly maps to the algebra.


#### Endpoints
- I have separated endpoints for ease of maintainability, grouping them with access levels.
- I have reduced boilerplate with a typesafe generateResponse method, which (unsurprisingly) generates responses based on what type (`User`, `List[User]`, `Done`) and value (`Left`/`Right`) the `UserManagement` service provides.

##### Authentication and Authorization
- Although I have grouped the endpoints into access levels based on authorization (admin, user, unprevileged), I didn't actually implement any authentication. The reasons are:
  - The `UserManagement` service does not have a getByUsername method (in constrast to the repo itself). I was unsure of the intention as to why this was the case. 
    - I added this part later, but it was one of the initial reasons.
  - The `User` class didn't contain any information to distinguish between `Admin`s and `User`s
  - It would take too much time to change the given implementations and to add auth middlewares.

## Misc
### Build
- I've updated scala & sbt versions.
- I've added sbt-revolver.
- I've deleted snapshot repository from build.sbt, since it was unused.
