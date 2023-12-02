# OpenDialer

An open, clean and modern dialer for Android.

![Static Badge](https://img.shields.io/badge/24%2B-brightgreen?logo=android&label=API)
[![Latest Version](https://img.shields.io/github/v/release/oxcened/opendialer)](https://github.com/oxcened/opendialer/releases)
[![Discord Server](https://dcbadge.vercel.app/api/server/hKXzFFMTFN?style=flat)](https://discord.gg/hKXzFFMTFN)

[![Repository License](https://img.shields.io/github/license/oxcened/opendialer)](./LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen)](./CONTRIBUTING.md)
[![Build Workflow Status](https://img.shields.io/github/actions/workflow/status/oxcened/opendialer/android.yml)](https://github.com/oxcened/opendialer/actions)
[![Crowdin](https://badges.crowdin.net/opendialer/localized.svg)](https://crowdin.com/project/opendialer)
[![Conventional Commits](https://img.shields.io/badge/Conventional%20Commits-1.0.0-%23FE5196?logo=conventionalcommits&logoColor=white)](https://conventionalcommits.org)

**Table of Contents**

- [Features](#features)
- [Build and Run](#build-and-run)
- [Tech Stack](#tech-stack)
- [Modularization](#modularization)
- [Roadmap](#roadmap)
- [Changelog](#changelog)
- [Contributing](#contributing)
- [License](#license)

## Features

- Recents call log
- Contacts list
- Dial a number to call/message/add to contacts
- In-Call interface
- Call conferences
- Hang up with customized quick answers

## Build and Run

**OpenDialer** uses the Gradle build system and can be imported directly into Android Studio (make sure you are using the latest stable version available [here](https://developer.android.com/studio)). 

Change the run configuration to `app`.

![image](docs/images/android_studio_build.png)

The app contains the usual `debug` and `release` build variants which can be built and run. 

![image](docs/images/android_studio_build_variant.png)

Once you're up and running, you can refer to the learning journeys below to get a better
understanding of which libraries and tools are being used, the reasoning behind the approaches to
UI, testing, architecture and more, and how all of these different pieces of the project fit
together to create a complete app.

## Tech Stack

- JDK 17
- Android API 24+
- Clean Architecture
- MVVM pattern
- ViewModel and LiveData
- Kotlin Coroutines and Flow
- Dagger Hilt

## Modularization

The **OpenDialer** app has been fully modularized based on the [official recommendations](https://developer.android.com/topic/modularization/patterns) and you can find the
description of the modularization strategy used in
[modularization learning journey](./docs/ModularizationLearningJourney.md).

## Roadmap

Development progress is tracked in the [roadmap](https://github.com/users/oxcened/projects/3).

## Changelog

Released changes are documented in the [changelog](./CHANGELOG.md).

## Contributing

If you're interested in contributing, please read the [contributing docs](./CONTRIBUTING.md).

## License

OpenDialer is available under the [Apache 2.0 License](./LICENSE).
