// SPDX-License-Identifier: Apache-2.0
// Copyright © 2022 Florian Schmaus
package eu.geekplace.beanstalk.core.scala

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutor

object VirtualThread {
  final lazy val executionContext: ExecutionContextExecutor = {
    val virtualThreadExecutorService = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()
    ExecutionContext.fromExecutorService(virtualThreadExecutorService)
  }

  object Implicits {
    implicit final def executionContext: ExecutionContext = VirtualThread.executionContext
  }
}
