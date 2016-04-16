'use strict'
var os = require('os');
let installDependencies = run({
  name: 'npm',
  cwd: '.',
  sh: 'npm install',
  watch: 'package.json'
})

let stylus = run({
  name: 'css',
  cwd: '.',
  sh: 'mkdir -p target/scala-2.11/classes/public && ./node_modules/.bin/stylus src/main/stylus -o target/scala-2.11/classes/public',
  watch: 'src/**/*.styl'
}).dependsOn(installDependencies)

let webpack = run({
  name: 'javascript',
  cwd: '.',
  sh: './node_modules/.bin/webpack --bail',
  watch: ['src/**/*.js', 'src/**/*.jsx']
}).dependsOn(installDependencies)

let sbt = startSbt({
  sh: 'sbt',
  watch: ['build.sbt']
})

let packageDependencies = sbt.run({
  name: 'server deps',
  command: 'assemblyPackageDependency'
})

let publicResources = sbt.run({
  name: '/public',
  command: 'copyResources',
  watch: ['src/main/resources/**']
}).dependsOn(packageDependencies)

let compileServer = sbt.run({
  name: 'scalac',
  command: 'compile',
  watch: ['src/**/*.scala']
}).dependsOn(packageDependencies)

let server = runServer({
  name: 'server',
  httpPort,
  sh: `java -XX:+PrintGCDetails -cp "target/scala-2.11/loop-finatra-js-skel-assembly-0.1-SNAPSHOT-deps.jar${os.platform() === 'win32' ? ';': ':'}target/scala-2.11/classes" org.your.AppServer -http.port=:${httpPort} -doc.root=public`
}).dependsOn(compileServer)

proxy(server, 8080).dependsOn(stylus, webpack, publicResources)
