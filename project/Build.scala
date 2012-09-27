import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "pocktalk"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
		"com.amazonaws" % "aws-java-sdk" % "1.3.11",
    	"postgresql" % "postgresql" % "9.1-901-1.jdbc4",
    	"org.mongodb" % "mongo-java-driver" % "2.7.3",
    	"com.google.code.morphia" % "morphia" % "0.99",    	
    	"com.google.code.morphia" % "morphia-logging-slf4j" % "0.99"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      	// Add your own project settings here      
    	resolvers += "Maven repository" at "http://morphia.googlecode.com/svn/mavenrepo/",
    	resolvers += "MongoDb Java Driver Repository" at "http://repo1.maven.org/maven2/org/mongodb/mongo-java-driver/"
    )

}
