import jetbrains.buildServer.configs.kotlin.v2019_2.BuildFeatures
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.DslContext
import jetbrains.buildServer.configs.kotlin.v2019_2.Trigger
import jetbrains.buildServer.configs.kotlin.v2019_2.VcsRoot
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.SshAgent
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.project
import jetbrains.buildServer.configs.kotlin.v2019_2.sequential
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.VcsTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.version
import no.elhub.common.build.configuration.AutoRelease
import no.elhub.common.build.configuration.CodeReview
import no.elhub.common.build.configuration.SonarScan
import no.elhub.common.build.configuration.constants.GlobalTokens
import no.elhub.common.build.configuration.ProjectType
import no.elhub.common.build.configuration.ProjectType.ANSIBLE

version = "2020.2"

project {

    val projectId = "no.elhub.tools:dev-tools"

    params {
        param("teamcity.ui.settings.readOnly", "true")
    }

    val buildChain = sequential {

        buildType(
            SonarScan(
                SonarScan.Config(
                    vcsRoot = DslContext.settingsRoot,
                    type = ANSIBLE,
                    sonarId = projectId,
                    sonarProjectSources = "."
                )
            )
        )

        val githubAuth = SshAgent({
            teamcitySshKey = "teamcity_github_rsa"
            param("secure:passphrase", GlobalTokens.githubSshPassphrase)
        })

        buildType(
            AutoRelease(
                AutoRelease.Config(
                    vcsRoot = DslContext.settingsRoot,
                    type = ANSIBLE,
                    sshAgent = githubAuth
                )
            ) {

                VcsTrigger ()

            })

    }

    buildChain.buildTypes().forEach { buildType(it) }

    buildType(
        CodeReview(
            CodeReview.Config(
                vcsRoot = DslContext.settingsRoot,
                type = ProjectType.ANSIBLE,
                sonarId = projectId
            )
        )
    )

}
