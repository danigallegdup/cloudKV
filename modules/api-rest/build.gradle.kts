plugins { application }

dependencies {
    implementation(project(":modules:core"))

    implementation("org.eclipse.jetty:jetty-server:11.0.24")
    implementation("org.eclipse.jetty:jetty-servlet:11.0.24")

    implementation("org.glassfish.jersey.core:jersey-server:3.1.5")
    implementation("org.glassfish.jersey.containers:jersey-container-servlet:3.1.5")
    implementation("org.glassfish.jersey.media:jersey-media-json-binding:3.1.5")

    implementation("org.glassfish.jersey.inject:jersey-hk2:3.1.5")

    implementation("jakarta.json.bind:jakarta.json.bind-api:3.0.0")
    implementation("org.eclipse:yasson:3.0.4")

    // Saxon for XSLT and XQuery support
    implementation("net.sf.saxon:Saxon-HE:12.4")

    // JAX-WS (Metro) for lightweight SOAP stub
    implementation("com.sun.xml.ws:jaxws-rt:4.0.2")
    implementation("jakarta.xml.ws:jakarta.xml.ws-api:4.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
}


application {
    mainClass.set("io.dani.cloudkv.api.App")
}
java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
}