import java.text.SimpleDateFormat

tasks.register("updateReleaseInfo") {
//    def file = new File(projectDir, 'src/main/assets/release.properties')
//
//    doFirst {
//        Properties props = new Properties()
//        props.load(new FileInputStream(file))
//
//        def buildNumber = Integer.parseInt(props.getProperty('build_number')) + 1
//        props.setProperty('build_number', String.valueOf(buildNumber))
//
//        def format = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss z')
//        format.setTimeZone(TimeZone.getTimeZone('UTC'))
//        props.setProperty('build_time', format.format(new Date()))
//
//        props.store(new FileOutputStream(file), 'Release properties')
//    }
}


tasks.register<Copy>("uploadRelease") {
//    from fileTree('build/outputs/apk/paid').include('**/*.apk').files
//    into upload_path
}
