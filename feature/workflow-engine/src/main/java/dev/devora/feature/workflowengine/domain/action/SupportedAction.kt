package dev.devora.feature.workflowengine.domain.action

/**
 * Built-in actions Devora's Built-in Runner maps to a real local
 * equivalent. Deliberately NOT exhaustive — an action is only added
 * here once it has a real, honest local implementation. Actions with
 * no sane local equivalent (actions/cache — depends on GitHub's
 * remote cache backend; actions/download-artifact — depends on a
 * previous job's uploaded artifact existing on GitHub's servers) are
 * intentionally left out rather than faked. Unlisted actions fail
 * clearly at runtime (see DefaultWorkflowRepository.executeUsesStep).
 */
enum class SupportedAction(val actionPrefix: String) {
    CHECKOUT("actions/checkout"),
    SETUP_JAVA("actions/setup-java"),
    UPLOAD_ARTIFACT("actions/upload-artifact");

    companion object {
        fun match(uses: String): SupportedAction? {
            val nameWithoutVersion = uses.substringBefore("@")
            return entries.find { it.actionPrefix == nameWithoutVersion }
        }
    }
}