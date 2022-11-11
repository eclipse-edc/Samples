# Contributing to the Project

Thank you for your interest in contributing to
the [Eclipse Dataspace Connector](https://projects.eclipse.org/projects/technology.dataspaceconnector)!

## Table of Contents

* [Project Description](#project-description)
* [Code Of Conduct](#code-of-conduct)
* [Eclipse Contributor Agreement](#eclipse-contributor-agreement)
* [How to Contribute](#how-to-contribute)
  * [Create an Issue](#create-an-issue)
  * [Submit a Pull Request](#submit-a-pull-request)
  * [Etiquette for pull requests](#etiquette-for-pull-requests)
  * [Stale issues and PRs](#stale-issues-and-prs)
* [Contact Us](#contact-us)

## Project Description

See the [main repository](https://github.com/eclipse-dataspaceconnector/DataSpaceConnector) for a comprehensive project description.

## Code Of Conduct

See the [Eclipse Code Of Conduct](https://www.eclipse.org/org/documents/Community_Code_of_Conduct.php).

## Eclipse Contributor Agreement

Before your contribution can be accepted by the project, you need to create and electronically sign
a [Eclipse Contributor Agreement (ECA)](http://www.eclipse.org/legal/ecafaq.php):

1. Log in to the [Eclipse foundation website](https://accounts.eclipse.org/user/login/). You will
   need to create an account within the Eclipse Foundation if you have not already done so.
2. Click on "Eclipse ECA", and complete the form.

Be sure to use the same email address in your Eclipse Account that you intend to use when you commit
to GitHub.

## How to Contribute

### Create an Issue

If you have identified a bug or want to formulate a working item that you want to concentrate on,
feel free to create a new issue at our project's corresponding
[GitHub Issues page](https://github.com/eclipse-dataspaceconnector/Samples/issues/new).

Before doing so, please consider searching for potentially suitable
[existing issues](https://github.com/eclipse-dataspaceconnector/Samples/issues?q=is%3Aissue+is%3Aopen).

We also use [GitHub's default label set](https://docs.github.com/en/issues/using-labels-and-milestones-to-track-work/managing-labels)
extended by custom ones to classify issues and improve findability.

### Submit a Pull Request

In addition to the contribution guideline made available in the
[Eclipse project handbook](https://www.eclipse.org/projects/handbook/#contributing),
we would appreciate if your pull request applies to the following points:

* Conform to [Pull-Request Etiquette](#etiquette-for-pull-requests).

* Always apply the following copyright header to specific files in your work replacing the fields
  enclosed by curly brackets "{}" with your own identifying information. (Don't include the curly
  brackets!) Enclose the text in the appropriate comment syntax for the file format.

    ```text
    Copyright (c) {year} {owner}[ and others]
    This program and the accompanying materials are made available under the
    terms of the Apache License, Version 2.0 which is available at
    https://www.apache.org/licenses/LICENSE-2.0
    SPDX-License-Identifier: Apache-2.0
    Contributors:
      {name} - {description}
    ```

* The git commit messages should comply to the following format:
    ```
    <prefix>(<scope>): <description>
    ```

  Use the [imperative mood](https://github.com/git/git/blob/master/Documentation/SubmittingPatches)
  as in "Fix bug" or "Add feature" rather than "Fixed bug" or "Added feature" and
  [mention the GitHub issue](https://docs.github.com/en/issues/tracking-your-work-with-issues/linking-a-pull-request-to-an-issue)
  e.g. `chore(transfer process): improve logging`.

  All committers, and all commits, are bound to
  the [Developer Certificate of Origin.](https://www.eclipse.org/legal/DCO.php)
  As such, all parties involved in a contribution must have valid ECAs. Additionally, commits can
  include a ["Signed-off-by" entry](https://wiki.eclipse.org/Development_Resources/Contributing_via_Git).

* Where code is not self-explanatory, add documentation providing extra clarification.

* PR descriptions should use the current [PR template](.github/PULL_REQUEST_TEMPLATE.md)

* Submit a draft pull request at early-stage and add people previously working on the same code as
  reviewer. Make sure automatic checks pass before marking it as "ready for review":

  * _Intellectual Property Validation_ verifying the [Eclipse CLA](#eclipse-contributor-agreement)
    has been signed as well as commits have been signed-off and
  * _Continuous Integration_ performing various test conventions.

### Etiquette for pull requests

#### As an author

Submitting pull requests should be done while adhering to a couple of simple rules.

- Familiarize yourself with provided contribution guidelines.
- No surprise PRs please. Before you submit a PR, open up an issue outlining your planned work and give
  people time to comment. It may even be advisable to contact committers using the `@mention` feature. Unsolicited PRs
  may get ignored or rejected.
- Create focused PRs: your work should be focused on one particular feature or bug. Do not create broad-scoped PRs that
  solve multiple issues as reviewers may reject those PR bombs outright.
- Provide a clear description and motivation in the PR description in GitHub. This makes the reviewer's life much
  easier. It is also helpful to outline the broad changes that were made, e.g. "Changes the schema of XYZ-Entity:
  the `age` field changed from `long` to `String`".
- If you introduce new 3rd party dependencies, be sure to note them in the PR description and explain why they are necessary.
- Stick to the established code style.
- All tests should be green, especially when your PR is in `"Ready for review"`
- Mark PRs as `"Ready for review"` only when you're prepared to defend your work. By that time you have completed your
  work and shouldn't need to push any more commits other than to incorporate review comments.
- Merge conflicts should be resolved by squashing all commits on the PR branch, rebasing onto `main` and
  force-pushing. Do this when your PR is ready to review.
- If you require a reviewer's input while it's still in draft, please contact the designated reviewer using
  the `@mention` feature and let them know what you'd like them to look at.
- Request a review from one of the [technical committers](#the-technical-committers-as-of-may-18-2022). Requesting a review from
- anyone else is still possible, and sometimes may be advisable, but only committers can merge PRs, so be sure to include them early on.
- Re-request reviews after all remarks have been adopted. This helps reviewers track their work in GitHub.
- If you disagree with a committer's remarks, feel free to object and argue, but if no agreement is reached, you'll have
  to either accept the decision or withdraw your PR.
- Be civil and objective. No foul language, insulting or otherwise abusive language will be tolerated.

#### As a reviewer

- Please complete reviews within two business days or delegate to another committer, removing yourself as a reviewer.
- If you have been requested as reviewer, but cannot do the review for any reason (time, lack of knowledge in particular
  area, etc.) please comment that in the PR and remove yourself as a reviewer, suggesting a stand-in.
- Don't be overly pedantic.
- Don't argue basic principles (code style, architectural decisions, etc.)
- Use the `suggestion` feature of GitHub for small/simple changes.
- The following could serve you as a review checklist:
  - no unnecessary dependencies
  - code style
  - simplicity and "uncluttered-ness" of the code
  - overall focus of the PR
- Don't just wave through any PR. Please take the time to look at them carefully.
- Be civil and objective. No foul language, insulting or otherwise abusive language will be tolerated. The goal is to
  _encourage_ contributions.

#### The technical committers (as of May 18, 2022)

- @MoritzKeppler
- @jimmarino
- @bscholtes1A
- @ndr_brt
- @ronjaquensel
- @juliapampus
- @paullatzelsperger

### Stale issues and PRs

In order to keep our backlog clean we are using a bot that helps us label and eventually close old issues and PRs. The
following table shows the particular timings.

|                        | `stale` after | closed after days `stale` |
|------------------------|---------------|---------------------------|
| Issue without assignee | 14            | 7                         |
| Issue with assignee    | 28            | 7                         |
| PR                     | 7             | 7                         |

Note that updating an issue, e.g. by commenting, will remove the `stale` label again and reset the counters. However,
we ask the community **not to abuse** this feature (e.g. commenting "what's the status?" every X days would certainly
be qualified as abuse). If an issue receives no attention, there usually are reasons for it. It is therefore advisable
to clarify in advance whether any particular feature fits into EDC's planning schedule and roadmap.

## Contact Us

If you have questions or suggestions, do not hesitate to contact the project developers via
the [project's "dev" list](https://dev.eclipse.org/mailman/listinfo/dataspaceconnector-dev). 
