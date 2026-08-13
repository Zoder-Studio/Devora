package dev.devora.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Devora application entry point.
 *
 * This class only wires the DI graph. It must never contain business
 * logic for build, workflow, or signing decisions — those belong to
 * their respective feature modules (spec section 30).
 */
@HiltAndroidApp
class DevoraApplication : Application()