package dev.devora.feature.terminal.data

import org.junit.Assert.assertEquals
import org.junit.Test

class TermuxContractTest {

    @Test
    fun `run command action matches Termux RUN_COMMAND contract`() {
        assertEquals("com.termux.RUN_COMMAND", TermuxContract.RUN_COMMAND_ACTION)
        assertEquals("com.termux.permission.RUN_COMMAND", TermuxContract.RUN_COMMAND_PERMISSION)
    }

    @Test
    fun `shell binary path matches Termux usr layout`() {
        assertEquals(
            "/data/data/com.termux/files/usr/bin/bash",
            TermuxContract.TERMUX_SHELL_BINARY
        )
    }
}