plugins{
    `java-library`
}

dependencies{
    api(project(":nodes"))
    api("de.eldoria.jacksonbukkit", "paper", "1.2.0")
    api("de.eldoria.util", "items", "2.0.0-DEV")
    api("de.eldoria.util", "jackson-configuration", "2.0.0-DEV")
}
