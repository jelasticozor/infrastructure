package integration

import jetbrains.buildServer.configs.kotlin.BuildSteps
import jetbrains.buildServer.configs.kotlin.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.buildSteps.script

fun BuildSteps.setupHasuraDatabase(workingDir: String): ScriptBuildStep {
    return script {
        name = "Set Up Hasura Database"
        scriptContent = """
            #! /bin/sh
            set -e
            ./setup_hasura_db.sh
        """.trimIndent()
        this.workingDir = workingDir
        dockerPull = true
        dockerImage = "%system.docker-registry.group%/governmentpaas/psql:latest"
        dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
        dockerRunParameters = "-v /var/run/docker.sock:/var/run/docker.sock"
    }
}