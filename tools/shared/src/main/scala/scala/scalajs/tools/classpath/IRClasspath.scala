/*                     __                                               *\
**     ________ ___   / /  ___      __ ____  Scala.js tools             **
**    / __/ __// _ | / /  / _ | __ / // __/  (c) 2013-2014, LAMP/EPFL   **
**  __\ \/ /__/ __ |/ /__/ __ |/_// /_\ \    http://scala-js.org/       **
** /____/\___/_/ |_/____/_/ | |__/ /____/                               **
**                          |/____/                                     **
\*                                                                      */


package scala.scalajs.tools.classpath

import scala.collection.immutable.{Seq, Traversable}

import scala.scalajs.tools.io._
import scala.scalajs.tools.logging._
import scala.scalajs.tools.optimizer.ScalaJSOptimizer
import scala.scalajs.tools.jsdep.ResolutionInfo

/** A [[CompleteClasspath]] that contains only IR as scalaJSCode */
final class IRClasspath(
    /** The JS libraries the IR code depends on */
    jsLibs: Seq[(VirtualJSFile, ResolutionInfo)],
    /** The IR itself. Ancestor count is used for later ordering */
    val scalaJSIR: Traversable[VirtualScalaJSIRFile],
    requiresDOM: Boolean,
    version: Option[String]
) extends CompleteClasspath(jsLibs, requiresDOM, version) {

  /** Orders and optimizes the contained IR.
   *
   *  Consider using ScalaJSOptimizer for a canonical way to do so. It allows to
   *  persist the resulting file and create a source map.
   */
  override lazy val scalaJSCode: VirtualJSFile = {
    import ScalaJSOptimizer._

    val outName = "temporary-fastOpt.js"

    if (scalaJSIR.nonEmpty) {
      val output = WritableMemVirtualJSFile(outName)
      (new ScalaJSOptimizer).optimizeCP(
          Inputs(this),
          OutputConfig(output),
          NullLogger)
      output
    } else {
      // We cannot run the optimizer without IR, because it will complain about
      // java.lang.Object missing. However, an empty JS file is perfectly valid
      // for no IR at all.
      VirtualJSFile.empty(outName)
    }
  }
}