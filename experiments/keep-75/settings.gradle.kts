include(
    "gradle-api",
    "kotlin-dsl"
)

for (project in rootProject.children) {
    project.projectDir = file("subprojects/${project.name}")
}
