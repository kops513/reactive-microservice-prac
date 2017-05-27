
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/krishnaraghubanshi/IdeaProjects/reactive-microservice-prac/metric-collector/conf/routes
// @DATE:Sun Jan 08 21:55:05 CST 2017

package controllers;

import router.RoutesPrefix;

public class routes {
  
  public static final controllers.ReverseMetricsCollector MetricsCollector = new controllers.ReverseMetricsCollector(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final controllers.javascript.ReverseMetricsCollector MetricsCollector = new controllers.javascript.ReverseMetricsCollector(RoutesPrefix.byNamePrefix());
  }

}
