# Play Console: what to enter

**Publisher:** Smart Tech Labs - **Contact email:** smarttechlab.apps@gmail.com

## 1. Host the pages (free)
GitHub repo > Settings > Pages > Source: *Deploy from a branch* > Branch `main`, folder `/docs`.
Your URLs become `https://<github-username>.github.io/<repo-name>/privacy.html` (and `terms.html`,
`delete-account.html`). Open each in a browser to confirm before pasting into Play Console.

## 2. Fields
| Play Console location | Enter |
|---|---|
| Store listing > Contact email | smarttechlab.apps@gmail.com |
| App content > Privacy policy | `.../privacy.html` |
| App content > Data deletion (if asked) | `.../delete-account.html` |
| App content > App access | All functionality available without login |
| App content > Ads | Yes, the app contains ads |
| App content > Target audience | 13+ age groups only (do NOT select under 13 / Families) |
| App content > Account creation | No sign-up/sign-in (anonymous ID only). Still provide the deletion URL if offered |
| Terms of Service | Not a Play field. Put the `terms.html` link in the store description |

## 3. Data safety form (match the policy)
Follow Google's published disclosures for each SDK, then add yours:
- AdMob: https://developers.google.com/admob/android/privacy/play-data-disclosure
- Firebase: https://firebase.google.com/docs/android/play-data-disclosure

Typical answers once Firestore + AdMob are live:
- Data collected: **Device or other IDs** (advertising ID), **App activity** (game progress, scores), **App info and performance** (crash logs/diagnostics), approximate location from IP (via ads).
- Purposes: App functionality, Advertising, Analytics/fraud prevention.
- Shared with third parties: advertising ID and ad interaction data (AdMob).
- Encrypted in transit: **Yes**. Users can request deletion: **Yes** (in-app + URL).
- Not collected: name, email, phone, precise location, contacts, photos, financial info.

If you add Firebase Analytics or Crashlytics, add them here and to the policy.
Review all answers again before every release.
