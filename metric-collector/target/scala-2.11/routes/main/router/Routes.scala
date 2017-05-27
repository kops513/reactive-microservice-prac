
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/krishnaraghubanshi/IdeaProjects/reactive-microservice-prac/metric-collector/conf/routes
// @DATE:Sun Jan 08 21:55:05 CST 2017

package router

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._
import play.core.j._

import play.api.mvc._

import _root_.controllers.Assets.Asset

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:1
  MetricsCollector_0: controllers.MetricsCollector,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:1
    MetricsCollector_0: controllers.MetricsCollector
  ) = this(errorHandler, MetricsCollector_0, "/")

  import ReverseRouteContext.empty

  def withPrefix(prefix: String): Routes = {
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, MetricsCollector_0, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """metrics""", """controllers.MetricsCollector.index()"""),
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:1
  private[this] lazy val controllers_MetricsCollector_index0_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("metrics")))
  )
  private[this] lazy val controllers_MetricsCollector_index0_invoker = createInvoker(
    MetricsCollector_0.index(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.MetricsCollector",
      "index",
      Nil,
      "GET",
      """""",
      this.prefix + """metrics"""
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:1
    case controllers_MetricsCollector_index0_route(params) =>
      call { 
        controllers_MetricsCollector_index0_invoker.call(MetricsCollector_0.index())
      }
  }
}
