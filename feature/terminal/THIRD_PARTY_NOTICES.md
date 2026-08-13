# Third-Party Notices — feature:terminal

## Termux terminal-emulator / terminal-view
Source: https://github.com/termux/termux-app
License: GNU General Public License v3.0 only (GPLv3)

This module links against com.termux:terminal-view (which transitively
includes com.termux:terminal-emulator) when running in embedded engine
mode. Because these libraries are GPLv3, any Devora build that includes
this module's embedded engine is a combined work under GPLv3: the
complete corresponding source code of Devora must be made available
under GPLv3 to anyone who receives the compiled application.

If Devora ships without the embedded engine (Termux-app-only mode),
this obligation does not apply, since no GPLv3 code is linked into
the binary.