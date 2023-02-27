package integration

import common.jelastic.createEnvironment
import common.templates.NexusDockerLogin
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.DslContext
import jetbrains.buildServer.configs.kotlin.buildSteps.script

class Up(
    dockerTag: String,
) : BuildType({
    templates(
        NexusDockerLogin
    )

    name = "Up"

    vcs {
        root(DslContext.settingsRoot)
    }

    failureConditions {
        executionTimeoutMin = 120
    }

    val databaseFolder = "./database"

    val stateFile = "./state.json"

    steps {
        // TODO: we publish sensitive data as environment variables (e.g. passwords, api keys); we should fix that
        // --> maybe define a dedicated vault for the jelasticozor engine in a separate environment?
        createEnvironment(
            manifestUrl = "https://raw.githubusercontent.com/jelastic-jps/postgres/v2.0.0/manifest.yaml",
            envPropsQueries = listOf(
                Pair("DATABASE_ENV_NAME", "${'$'}{env.envName}"),
                Pair("DATABASE_URL", "${'$'}{nodes.sqldb.master.url}"),
                Pair("DATABASE_PORT", "5432"),
                Pair("DATABASE_ADMIN_USER", "webadmin"),
                Pair("DATABASE_ADMIN_PASSWORD", "${'$'}{nodes.sqldb.password}")
            ),
            jsonSettingsFile = "settings.json",
            dockerToolsTag = dockerTag,
            workingDir = databaseFolder,
            region = "new",
        )
        updateState(
            envVar = "DATABASE_ENV_NAME",
            stateFile = stateFile,
            workingDir = "./"
        )
//        script {
//            name = "Publish Database Hostname"
//            scriptContent = """
//                #! /bin/sh
//
//                echo "##teamcity[setParameter name='env.DATABASE_HOSTNAME' value='${'$'}{DATABASE_URL#https://}']"
//            """.trimIndent()
//        }
//        createFusionAuthDatabase(workingDir = databaseFolder)
//        createEnvironment(
//            manifestUrl = "https://raw.githubusercontent.com/jelastic-jps/kubernetes/v1.25.4/manifest.jps",
//            envPropsQueries = listOf(
//                Pair("KUBERNETES_ENV_NAME", "${'$'}{env.envName}"),
//                Pair("KUBERNETES_API_TOKEN", "${'$'}{globals.token}"),
//                Pair("FQDN", "${'$'}{env.domain}"),
//            ),
//            jsonSettingsFile = "settings.json",
//            dockerToolsTag = dockerTag,
//            workingDir = "./kubernetes",
//            region = "new",
//        )
//        updateState(
//            envVar = "KUBERNETES_ENV_NAME",
//            stateFile = stateFile,
//            workingDir = "./"
//        )
//        createEnvironment(
//            envName = "%env.KUBERNETES_ENV_NAME%",
//            manifestUrl = "https://raw.githubusercontent.com/jelasticozor/deployment-infrastructure/main/ssl.yaml",
//            dockerToolsTag = dockerTag,
//        )
//        exposeKubernetesApiServer(
//            envName = "%env.KUBERNETES_ENV_NAME%",
//            envPropsQueries = listOf(
//                Pair("KUBERNETES_API_URL", "https://${'$'}{env.domain}/api"),
//            ),
//            dockerToolsTag = dockerTag
//        )
//        script {
//            name = "Wait For Kubernetes API"
//            scriptContent = """
//                #! /bin/sh
//
//                for i in ${'$'}(seq 1 120) ; do
//                    status_code=${'$'}(curl -s -o /dev/null -w "%{http_code}" ${'$'}KUBERNETES_API_URL/version)
//                    echo "status code: ${'$'}status_code"
//                    if [ "${'$'}status_code" = "200" ] ; then
//                        break
//                    fi
//                    sleep 1
//                done
//
//                if [ "${'$'}i" = "120" ] ; then
//                  exit 1
//                fi
//            """.trimIndent()
//        }
//        installHelmCharts(
//            workingDir = ".",
//            dockerToolsTag = dockerTag,
//        )
//        hideKubernetesApiServer(
//            envName = "%env.KUBERNETES_ENV_NAME%",
//            dockerToolsTag = dockerTag,
//        )
    }

    artifactRules = """
        $stateFile
    """.trimIndent()
})