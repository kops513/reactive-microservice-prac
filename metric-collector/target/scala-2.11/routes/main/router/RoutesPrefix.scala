
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/krishnaraghubanshi/IdeaProjects/reactive-microservice-prac/metric-collector/conf/routes
// @DATE:Sun Jan 08 21:55:05 CST 2017


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
