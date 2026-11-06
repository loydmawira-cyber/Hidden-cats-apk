package com.example.hiddencats.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// GENERATED from the same text as PRIVACY_POLICY.md / TERMS_OF_SERVICE.md. Keep them in sync.

data class LegalSection(val heading: String, val paragraphs: List<String>)

data class LegalDoc(
    val title: String,
    val updated: String,
    val intro: String,
    val sections: List<LegalSection>
)

@Composable
fun LegalDocumentView(doc: LegalDoc) {
    Column(modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
        Text(
            "Last updated: ${doc.updated}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(doc.intro, style = MaterialTheme.typography.bodyMedium)
        doc.sections.forEach { section ->
            Spacer(modifier = Modifier.height(14.dp))
            Text(section.heading, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            section.paragraphs.forEach { p ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(p, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

val PrivacyPolicyDoc = LegalDoc(
    title = "Privacy Policy",
    updated = "November 6, 2026",
    intro = "Hidden Cats: Color Logic (\"the Game\", \"the app\") is published by Smart Tech Labs (\"we\", \"us\", \"our\"). This policy explains what information the Game handles, why, who it is shared with, and the choices you have. You can play without creating an account and without giving us your name, email address, or phone number.",
    sections = listOf(
        LegalSection(
            "1. Information we handle",
            listOf(
            "• Game data: your puzzle progress, statistics, daily-challenge results, settings (sound, theme, accessibility options), hint balance, and whether you own Premium or Remove Ads. This is stored on your device.",
            "• Anonymous cloud data: when cloud features are active, the Game uses Firebase Anonymous Authentication to create a random identifier that is not linked to your name or email. Your game records and daily-challenge scores may be stored against that identifier in Google Firebase (Cloud Firestore) so progress and streaks can be saved and synchronised.",
            "• Device and usage information collected automatically by our service providers: advertising ID, IP address (which gives an approximate location), device model, operating system version, app version, ad interactions, and crash or diagnostic data.",
            "• Purchase information: purchases are processed entirely by Google Play. We receive confirmation of what you bought (for example, a purchase token) but never your card or payment details.",
            "• We do not collect your name, email, phone number, contacts, photos, precise location, microphone, or camera data."
            )
        ),
        LegalSection(
            "2. How we use information",
            listOf(
            "• To run the Game, save your progress, and keep daily streaks.",
            "• To verify and restore purchases (Premium, Remove Ads, hint packs).",
            "• To show ads and to measure and limit how often they are shown.",
            "• To protect the Game against abuse and fraud (for example with Firebase App Check) and to find and fix crashes and bugs.",
            "• To respond to your support or deletion requests."
            )
        ),
        LegalSection(
            "3. Advertising",
            listOf(
            "The Game shows ads from Google AdMob. Interstitial ads appear between puzzles. Rewarded video ads are always optional and let you earn a free hint. AdMob and its partners may use the advertising ID and similar information to serve and measure ads, and may personalise them.",
            "• You can reset your advertising ID or opt out of ad personalisation in your Android settings (Settings > Google > Ads).",
            "• Where required (for example in the European Economic Area and the United Kingdom), we ask for your consent before personalised ads are shown.",
            "• Buying Remove Ads or Premium removes interstitial ads. Optional rewarded videos stay available."
            )
        ),
        LegalSection(
            "4. Services we use",
            listOf(
            "• Google Play Services and Google Play Billing (purchases and licensing): https://policies.google.com/privacy",
            "• Firebase (Anonymous Authentication, Cloud Firestore, App Check): https://firebase.google.com/support/privacy",
            "• Google AdMob (advertising): https://policies.google.com/technologies/ads",
            "These providers process data under their own privacy policies and act on our behalf or as independent controllers, depending on the service."
            )
        ),
        LegalSection(
            "5. Sharing",
            listOf(
            "We do not sell your personal information for money. We share information only with the service providers listed above, when required by law or to protect rights and safety, or as part of a business transfer such as a merger or sale of the Game (we would tell you if that changed how your data is handled)."
            )
        ),
        LegalSection(
            "6. Retention and deletion",
            listOf(
            "• Data on your device stays until you delete it in the app, clear the app's storage, or uninstall the Game.",
            "• Cloud data tied to your anonymous identifier is kept until you delete it. You can delete everything at any time from Settings > Data Management > Delete My Data & Account. This clears your device data and requests deletion of your cloud data.",
            "• You can also ask us to delete data by emailing smarttechlab.apps@gmail.com. We aim to complete requests within 30 days. Full instructions: see our Account and Data Deletion page.",
            "• Purchase records are held by Google Play under Google's policies. After deleting your data you can restore purchases from the Shop because they belong to your Google account.",
            "• Ad and diagnostic data held by Google is retained under Google's own policies."
            )
        ),
        LegalSection(
            "7. Your rights",
            listOf(
            "Depending on where you live (for example under the GDPR, UK GDPR, or the California Consumer Privacy Act) you may have the right to access, correct, delete, or restrict the use of your information, to object to certain processing, and to complain to your local data protection authority. Because the Game does not know who you are, we may need the anonymous identifier or other details to locate your data. To use any right, delete your data in the app or email us."
            )
        ),
        LegalSection(
            "8. Children",
            listOf(
            "The Game is not directed to children under 13, and we do not knowingly collect personal information from them. If you are under the age at which you can consent to data processing in your country, please use the Game only with a parent or guardian's permission. If you believe a child has given us personal information, contact us and we will delete it."
            )
        ),
        LegalSection(
            "9. Security",
            listOf(
            "Data sent to our cloud services is encrypted in transit. We restrict access using Firebase security rules and App Check. No method of storage or transmission is completely secure, so we cannot guarantee absolute security."
            )
        ),
        LegalSection(
            "10. International transfers",
            listOf(
            "Google and its partners may process data in countries other than your own. Where required, transfers rely on safeguards such as standard contractual clauses."
            )
        ),
        LegalSection(
            "11. Changes to this policy",
            listOf(
            "We may update this policy as the Game changes. The date at the top shows the latest version. For significant changes we will notify you in the app or on the store listing."
            )
        ),
        LegalSection(
            "12. Contact us",
            listOf(
            "Smart Tech Labs\nEmail: smarttechlab.apps@gmail.com"
            )
        )
    )
)

val TermsOfServiceDoc = LegalDoc(
    title = "Terms of Service",
    updated = "November 6, 2026",
    intro = "These Terms govern your use of Hidden Cats: Color Logic (\"the Game\"), published by Smart Tech Labs (\"we\", \"us\"). By installing or playing the Game you agree to them. If you do not agree, please do not use the Game.",
    sections = listOf(
        LegalSection(
            "1. Eligibility",
            listOf(
            "You must be at least 13 years old (or the age of digital consent in your country, if higher) to use the Game. If you are below the age of majority, you confirm that a parent or guardian allows you to use it and to make any purchases."
            )
        ),
        LegalSection(
            "2. License",
            listOf(
            "We grant you a personal, non-exclusive, non-transferable, revocable license to install and play the Game on devices you own or control, for non-commercial entertainment. The Game, including its puzzles, artwork, sound, text, and code, belongs to Smart Tech Labs or its licensors. Open-source components remain under their own licenses."
            )
        ),
        LegalSection(
            "3. Acceptable use",
            listOf(
            "You agree not to:",
            "• copy, modify, reverse engineer, decompile, or attempt to extract the source code of the Game, except where the law allows it;",
            "• use cheats, bots, modified apps, or other tools to gain unfair advantages, tamper with scores, daily challenges, hints, or purchases;",
            "• interfere with ads, billing, or our servers, or attempt to access data that is not yours;",
            "• use the Game for any unlawful purpose or in a way that harms others.",
            "We may limit or remove access if these rules are broken."
            )
        ),
        LegalSection(
            "4. Purchases and virtual items",
            listOf(
            "• The Game is free to download. Optional purchases are available: Premium (a one-time purchase that unlocks 8x8 and 9x9 boards, unlimited hints, and removes ads), Remove Ads (a one-time purchase), and hint packs (consumable).",
            "• All purchases are processed by Google Play. Prices are shown in the Shop and may vary by region and taxes.",
            "• Hints and other virtual items have no cash value, cannot be transferred or sold, and are not refundable by us except where the law requires. Consumed hints cannot be restored.",
            "• One-time purchases (Premium, Remove Ads) are linked to your Google account. You can restore them from the Shop after reinstalling or on a new device.",
            "• Refunds are handled under Google Play's refund policy."
            )
        ),
        LegalSection(
            "5. Ads",
            listOf(
            "The Game shows ads. Interstitial ads are removed by buying Remove Ads or Premium. Rewarded video ads are optional, and the reward (such as one hint) is granted only after you finish watching. Ad content comes from third parties and we are not responsible for it."
            )
        ),
        LegalSection(
            "6. Your data",
            listOf(
            "Our Privacy Policy explains how data is handled, and you can delete your data in the app at any time. Deleting your data resets your progress and cannot be undone."
            )
        ),
        LegalSection(
            "7. Availability and updates",
            listOf(
            "We may update, change, suspend, or discontinue features of the Game at any time. Some features need an internet connection. We do not guarantee that the Game will be uninterrupted or error-free."
            )
        ),
        LegalSection(
            "8. Disclaimer of warranties",
            listOf(
            "The Game is provided \"as is\" and \"as available\" without warranties of any kind, to the extent allowed by law."
            )
        ),
        LegalSection(
            "9. Limitation of liability",
            listOf(
            "To the extent allowed by law, Smart Tech Labs is not liable for indirect, incidental, or consequential damages, or for loss of data, profits, or progress arising from your use of the Game. Our total liability for any claim is limited to the amount you paid us for the Game in the 12 months before the claim. Nothing in these Terms limits rights you have as a consumer that cannot be limited by law."
            )
        ),
        LegalSection(
            "10. Termination",
            listOf(
            "You may stop using the Game at any time by uninstalling it. We may suspend or end your access if you break these Terms. Sections that by nature should continue (such as ownership and liability limits) survive termination."
            )
        ),
        LegalSection(
            "11. Governing law",
            listOf(
            "These Terms are governed by the laws of the country where Smart Tech Labs is established, without regard to conflict-of-law rules. This does not remove mandatory consumer protections of the country where you live."
            )
        ),
        LegalSection(
            "12. Changes to these Terms",
            listOf(
            "We may update these Terms. The date at the top shows the latest version. Continuing to use the Game after an update means you accept it."
            )
        ),
        LegalSection(
            "13. Contact",
            listOf(
            "Smart Tech Labs\nEmail: smarttechlab.apps@gmail.com"
            )
        )
    )
)
