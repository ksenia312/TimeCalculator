package com.xenikii.timecalculator.domain.model

enum class NotificationMode {
    /** Only a notification when a routine starts and when it finishes. No progress, no per-task alerts. */
    START_AND_END,

    /** A persistent progress notification plus an alert for every task, on top of start/finish. */
    EVERY_TASK,
}
