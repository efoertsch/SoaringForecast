package org.soaringforecast.rasp.turnpoints.messages;

import android.content.Intent;

/**
 * Properly formatted intent to send email
 */
public class SendEmail {
    private Intent intent;

    public SendEmail(Intent intent) {
        this.intent = intent;
    }

    public Intent getIntent() {
        return intent;
    }
}
