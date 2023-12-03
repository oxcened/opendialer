# Modularization learning journey

In this learning journey you will learn about modularization, and the modularization strategy used
to create the modules in the OpenDialer app.


## Overview

Modularization is the practice of breaking the concept of a monolithic, one-module codebase into
loosely coupled, self contained modules.


### Benefits of modularization

This offers many benefits, including:

**Scalability** - In a tightly coupled codebase, a single change can trigger a cascade of
alterations. A properly modularized project will embrace
the [separation of concerns](https://en.wikipedia.org/wiki/Separation_of_concerns) principle. This
in turn empowers the contributors with more autonomy while also enforcing architectural patterns.

**Enabling work in parallel** - Modularization helps decrease version control conflicts and enables
more efficient work in parallel for developers in larger teams.

**Ownership** - A module can have a dedicated owner who is responsible for maintaining the code and
tests, fixing bugs, and reviewing changes.

**Encapsulation** - Isolated code is easier to read, understand, test and maintain.

**Reduced build time** - Leveraging Gradle’s parallel and incremental build can reduce build times.

**Dynamic delivery** - Modularization is a requirement
for [Play Feature Delivery](https://developer.android.com/guide/playcore/feature-delivery) which
allows certain features of your app to be delivered conditionally or downloaded on demand.

**Reusability** - Proper modularization enables opportunities for code sharing and building multiple
apps, across different platforms, from the same foundation.


### Modularization pitfalls

However, modularization is a pattern that can be misused, and there are some gotchas to be aware of
when modularizing an app:

**Too many modules** - each module has an overhead that comes in the form of increased complexity of
the build configuration. This can cause Gradle sync times to increase, and incurs an ongoing
maintenance cost. In addition, adding more modules increases the complexity of the project’s Gradle
setup, when compared to a single monolithic module. This can be mitigated by making use of
convention plugins, to extract reusable and composable build configuration into type-safe Kotlin
code.

**Not enough modules** - conversely if your modules are few, large and tightly coupled, you end up
with yet another monolith. This means you lose some benefits of modularization. If your module is
bloated and has no single, well defined purpose, you should consider splitting it.

**Too complex** - there is no silver bullet here. In fact it doesn’t always make sense to modularize
your project. A dominating factor is the size and relative complexity of the codebase. If your
project is not expected to grow beyond a certain threshold, the scalability and build time gains
won’t apply.


## Modularization strategy

It’s important to note that there is no single modularization strategy that fits all projects.
However, there are general guidelines that can be followed to ensure you maximize its benefits and
minimize its downsides.

A barebone module is simply a directory with a Gradle build script inside. Usually though, a module
will consist of one or more source sets and possibly a collection of resources or assets. Modules
can be built and tested independently. Due to Gradle's flexibility there are few constraints as to
how you can organize your project. In general, you should strive for low coupling and high cohesion.

* **Low coupling** - Modules should be as independent as possible from one another, so that changes
  to one module have zero or minimal impact on other modules. They should not possess knowledge of
  the inner workings of other modules.

* **High cohesion** - A module should comprise a collection of code that acts as a system. It should
  have clearly defined responsibilities and stay within boundaries of certain domain knowledge.


## Types of modules in OpenDialer

![Diagram showing types of modules and their dependencies in OpenDialer](./images/app_modularization.png "Diagram showing types of modules and their dependencies in OpenDialer")

**Top tip**: A module graph (shown above) can be useful during modularization planning for
visualizing dependencies between modules.

The OpenDialer app contains the following types of modules:

* The `app` module - contains app level and scaffolding classes that bind the rest of the codebase,
  such as `MainActivity`, `MainFragment`, `App` and app-level controlled navigation. A good example of this is
  the navigation setup through `main.xml` and the bottom navigation bar setup
  through `MainFragment`. The `app` module depends on all `feature` modules and
  required `core` modules.

* `feature:` modules - feature specific modules which are scoped to handle a single responsibility
  in the app. These modules can be reused by any app, including test or other flavoured apps, when
  needed, while still keeping it separated and isolated. If a class is needed only by one `feature`
  module, it should remain within that module. If not, it should be extracted into an
  appropriate `core` module. A `feature` module should have no dependencies on other feature
  modules. They only depend on the `core` modules that they require.

* `data:` modules - library modules responsible for business logic and sourcing data.
  Each data module should be responsible for handling data that represents a certain domain.
  It can handle many types of data as long as they are related.
  A data module usually contains a repository, data sources and model classes.
  A `data` module should have no dependencies on other data
  modules. They only depend on the `core` modules that they require.

* `core:` modules - common library modules containing auxiliary code and specific dependencies that
  need to be shared between other modules in the app. These modules can depend on other core
  modules, but they shouldn’t depend on feature nor app modules.

* Miscellaneous modules - these are yet to be created.


## Modules

Using the above modularization strategy, the OpenDialer app has the following modules:

<table>
  <tr>
   <td><strong>Name</strong>
   </td>
   <td><strong>Responsibilities</strong>
   </td>
   <td><strong>Key classes and good examples</strong>
   </td>
  </tr>
  <tr>
   <td><code>app</code>
   </td>
   <td>Brings everything together required for the app to function correctly. This includes UI scaffolding and navigation. 
   </td>
   <td><code>App, MainActivity, MainFragment</code><br>
   App-level controlled navigation via <code>main.xml</code>
   </td>
  </tr>
  <tr>
   <td><code>feature:1,</code><br>
   <code>feature:2</code><br>
   ...
   </td>
   <td>Functionality associated with a specific feature or user journey. Typically contains UI components and ViewModels which read data from other modules.
   </td>
   <td><code>CallsFragment</code><br>
   <code>CallsViewModel</code>
   </td>
  </tr>
  <tr>
   <td><code>data:1,</code><br>
   <code>data:2</code><br>
   ...
   </td>
   <td>Fetching app data from multiple sources, shared by different features.
   </td>
    <td><code>CallsRepository</code><br>
   <code>CallsDataSource</code>
   </td>
   </td>
  </tr>
  <tr>
   <td><code>core:common</code>
   </td>
   <td>Common classes shared between modules.
   </td>
   <td><code>PermissionUtils</code><br>
   <code>DefaultPhoneUtils</code>
   </td>
  </tr>
  <tr>
   <td><code>core:aosp</code>
   </td>
   <td>Utility classes from the Android Open Source Project.
   </td>
   <td><code>QueryFilteringUtil</code><br>
   <code>QueryBoldingUtil</code>
   </td>
  </tr>
</table>
