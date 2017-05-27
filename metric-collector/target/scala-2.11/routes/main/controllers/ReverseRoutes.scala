
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/krishnaraghubanshi/IdeaProjects/reactive-microservice-prac/metric-collector/conf/routes
// @DATE:Sun Jan 08 21:55:05 CST 2017

import play.api.mvc.{ QueryStringBindable, PathBindable, Call, JavascriptLiteral }
import play.core.routing.{ HandlerDef, ReverseRouteContext, queryString, dynamicString }


import _root_.controllers.Assets.Asset

// @LINE:1
package controllers {

  // @LINE:1
  class ReverseMetricsCollector(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:1
    def index(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "metrics")
    }
  
  }


}
