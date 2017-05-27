package main.scala.flow

import com.google.inject.AbstractModule
import flow.CustomFlowInitializer
import com.google.inject.name.Names


/**
 * Created by krishnaraghubanshi on 1/6/17.
 */
class Module extends AbstractModule{
  protected def configure: Unit = bind(classOf[CustomFlowInitializer]).asEagerSingleton()

}
